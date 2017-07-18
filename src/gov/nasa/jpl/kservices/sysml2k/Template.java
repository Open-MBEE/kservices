package gov.nasa.jpl.kservices.sysml2k;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Template {
  private static final String TRIGGER_FLAG = "!";
  private static final String RECUR_FLAG   = "@";
  private static final String NECESS_FLAG  = "&";
  private static final String NOT_ESCAPED = "(?<!\\\\)(?:\\\\{2})*"; // not preceded by an odd number of slashes. Stolen from maksymiuk (https://stackoverflow.com/questions/6525556/regular-expression-to-match-escaped-characters-quotes)
  private static final Function<String, String> matchFieldRegex = s -> NOT_ESCAPED + "%" + s + "\\$(?<mods>[\\w\\-#+0,(]+)"; //Non-escaped %, name expression, $, assumed valid Java format codes
  private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile( matchFieldRegex.apply(String.format("(?<trigger>%s)?(?<necess>%s)?(?<recur>%s)?(?<name>\\w+)", TRIGGER_FLAG, NECESS_FLAG, RECUR_FLAG)) );
  
  private String name;
  private String stringForm;
  private List<Field> fields;
  private Field triggerField;
  private Pattern regex; // to memoize the regex for matching instantiated templates
  //TODO: change this from a regex-based solution to a parser-based one.
  
  
  /// Public Methods

  public Template(String stringForm) throws S2KParseException {
    String[] nameBody = stringForm.split("\n", 2);
    this.name         = nameBody[0];
    this.stringForm   = nameBody[1];
    this.triggerField = null;
    
    Matcher fieldMatcher = GENERAL_FIELD_PATTERN.matcher(stringForm);
    fields = new ArrayList<Field>( fieldMatcher.groupCount() );
    
    // NOTE: by using a list for fields, we preserve order. This is important for later instantiation
    while (fieldMatcher.find()) {
      Field newField = new Field( fieldMatcher );
      fields.add( newField );
      if (newField.isTrigger) {
        if (triggerField == null) {
          triggerField = newField;
        } else {
          throw new S2KParseException("Template " + name + " instantiated with more than one trigger field.");
        }
      }
    }
    
    if (triggerField == null) {
      throw new S2KParseException("Template " + name + " instantiated with no trigger field.");
    }
  }

  /**
   * Represents all parts of the target code that match the specified template.
   * @param target The target code to look in
   * @return A list of Matches, each representing one instance of the template in the target.
   */
  public List<Match> match(String target, Collection<Template> allTemplates) {
    if (regex == null) {
      regex = this.asRegex();
    }
    
    List<Match> output = new LinkedList<Match>();
    Matcher targetMatcher = regex.matcher(target);
    while (targetMatcher.find()) {
      output.add(new Match(targetMatcher, allTemplates));
    }
    return output;
  }
  
  public String getName() {
    return name;
  }
  
  public String getTriggerName() {
    return this.triggerField.name;
  }
  
  public Collection<String> instantiate(TemplateDataSource dataSource, JSONObject source) {
    if (!dataSource.containsKey( getTriggerName() )) {
      return Collections.emptyList();
    }
    Map<Path, Object> triggerMap = dataSource.get( getTriggerName() ).access(source);
    
    return triggerMap.entrySet().stream().map( triggerEntry -> {
      Match match = new Match();
      match.put(getTriggerName(), triggerEntry.getValue().toString());
      fields.stream()
        .filter( field -> !field.name.equals(getTriggerName()) ) // for any field but the triggering field...
        .filter( field -> dataSource.containsKey(field.name) )
        .forEach( field -> {
          Map<Path, Object> relativeLookup = dataSource.get(field.name).access(source,
              triggerEntry.getKey().withoutLeaves().access(source).values().iterator().next()); // access that path, with this trigger's containing object as the reference object
          relativeLookup.entrySet().stream()
            .map( pathValue -> new AbstractMap.SimpleEntry<>(pathValue.getKey().distance(triggerEntry.getKey()), pathValue.getValue()) ) // compute the distances to the triggering path
            .min( (distanceValue1, distanceValue2) -> distanceValue1.getKey().compareTo(distanceValue2.getKey()) ) // choose the "closest" path
            .ifPresent( distanceValue -> {
              match.put(field.name, distanceValue.getValue().toString()); // if we have a path, add it to the match
            });
        });
      return match;
    })
    .map( this::instantiate ) // instantiate each match, returning a String
    .collect( Collectors.toList() );
  }
  
  public String toString() {
    return name + "\n" + stringForm;
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Template")
        .put("name", name)
        .put("stringForm", stringForm)
        .put("fields", fields.stream().map( Field::toJSON ).collect( Collectors.toList() ))
        .put("triggerField", triggerField.toJSON())
        .put("regex", regex.pattern());
  }
  
  public static Template fromJSON(JSONObject jsonObj) throws S2KParseException {
    try {
      if (!jsonObj.getString("_type").equals("Template")) {
        throw new S2KParseException("Could not parse JSON as a Template.");
      }
      // rebuild the input format to reuse the constructor
      Template output = new Template( jsonObj.getString("name") + "\n" + jsonObj.getString("stringForm") );
      output.fields = new LinkedList<Field>();
      JSONArray fieldArr = jsonObj.getJSONArray("fields");
      for (int i = 0; i < fieldArr.length(); ++i) {
        output.fields.add( Field.fromJSON( fieldArr.getJSONObject(i) ));
      }
      output.triggerField = Field.fromJSON( jsonObj.getJSONObject("triggerField") );
      output.regex = Pattern.compile( jsonObj.getString("regex") );
      return output;
      
    } catch (JSONException e) {
      throw new S2KParseException("Could not parse JSON as a Template.", e);
    }
  }
  
  /// Private Helpers
  
  private Pattern asRegex() {
    String tempStrForm = Pattern.quote(stringForm).replaceAll("\\s+", patternUnquote("\\\\s+"));
    for (Field field : fields) {
      // replace the java-style naming pattern with a regex to capture and name the match in an instantiated template
      tempStrForm = tempStrForm.replaceAll(
          matchFieldRegex.apply( field.toString() ),
          patternUnquote( field.toRegexStr() ));
    }
    System.out.printf("DEBUG[Template.java:asRegex]: tempStrForm: %s%n", tempStrForm); //DEBUG
    return Pattern.compile(tempStrForm, Pattern.DOTALL);
  }
  
  /**
   * The inverse of Pattern.quote, to be used only in quote'd strings.
   * @param regex The regex string to be unquote'd
   * @return s preceded by \E and terminated by \Q
   */
  private String patternUnquote(String regex) {
    return "\\\\E" + regex + "\\\\Q";
  }

  private String instantiate(Match fieldMatches) {
    String formatStr = GENERAL_FIELD_PATTERN.matcher(stringForm).replaceAll("%${mods}"); // replace all fields with Java String.format codes
    String[] fieldValues = new String[fields.size()];
    // Note: iteration order will be in the order of the template because of list structure.
    int i = 0;
    for (Field field : fields) {
      if (fieldMatches.containsKey(field.name) || !field.isNecessary) {
        fieldValues[i] = fieldMatches.getOrDefault(field.name, "");
        ++i;
      } else {
        return "";
      }
    }
    return String.format(formatStr, (Object[])fieldValues);
  }
  
  /// Public Sub-classes
  
  @SuppressWarnings("serial")
  public class Match extends LinkedHashMap<String, String> {
    public Match() {
      super();
    }
    public Match(Matcher res, Collection<Template> allTemplates) {
      super();
      for (Field field : fields) {
        if (field.isRecursive) {
          for (Template template : allTemplates) {
            List<Match> innerMatches = template.match(res.group(field.name), allTemplates);
            // TODO: figure out what to do with recursive matches
          }
        } else {
          this.put(field.name, res.group(field.name));
        }
      }
    }
    
    public JSONObject toJSON() {
      return new JSONObject(this).put("_type", "Match");
    }
  }
  
  public static class Field {
    public String name;
    public boolean isTrigger;
    public boolean isRecursive;
    public boolean isNecessary;
    
    public Field(Matcher sourceMatcher) {
      this( sourceMatcher.group("name"),
           (sourceMatcher.group("trigger") != null),   // true if `trigger` group was successful
           (sourceMatcher.group("recur")   != null),   // true if `recur` group was successful
           (sourceMatcher.group("necess")  != null) ); // true if `necessary` group was successful
    }
    public Field(String name) {
      this(name, false, false, false);
    }
    public Field(String name, boolean isTrigger, boolean isRecursive, boolean isNecessary) {
      this.name = name;
      this.isTrigger   = isTrigger;
      this.isRecursive = isRecursive;
      this.isNecessary = isNecessary || isTrigger; // a trigger is, by definition, necessary
    }
    
    public String toRegexStr() {
      // Explanation: if recursive, use a non-greedy "match-anything" pattern
      // otherwise, assume we're looking for a keyword, and use a greedy, non-empy "match-word" pattern
      return String.format("(?<%s>%s)", name, (isRecursive ? ".*?" : "\\\\w+"));
    }

    public String toString() {
      return (isTrigger   ? TRIGGER_FLAG : "") +
             (isRecursive ? RECUR_FLAG   : "") +
             name;
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "Field")
          .put("name", name)
          .put("isTrigger", isTrigger)
          .put("isRecursive", isRecursive)
          .put("isNecessary", isNecessary);
    }
    
    public static Field fromJSON(JSONObject jsonObj) throws S2KParseException {
      if (!jsonObj.getString("_type").equals("Field")) {
        throw new S2KParseException("Could not parse JSON as a Field.");
      }
      return new Field(
          jsonObj.getString("name"),
          jsonObj.getBoolean("isTrigger"),
          jsonObj.getBoolean("isRecursive"),
          jsonObj.getBoolean("isNecessary"));
    }
  }

}
