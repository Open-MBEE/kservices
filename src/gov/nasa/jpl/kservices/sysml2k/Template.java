package gov.nasa.jpl.kservices.sysml2k;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Template {
  private static final String CHILD_TEMPLATE_MODIFIER = "_WITHIN_"; //TODO: abstract these constants to a config file, except maybe for NOT_ESCAPED and below
  private static final String TRIGGER_FLAG = "!";
  private static final String RECUR_FLAG   = "@";
  private static final String NECESS_FLAG  = "&";
  private static final String LONG_FLAG    = "+";
  private static final String NOT_ESCAPED = "(?<!\\\\)(?:\\\\{2})*"; // not preceded by an odd number of slashes. Stolen from maksymiuk (https://stackoverflow.com/questions/6525556/regular-expression-to-match-escaped-characters-quotes)
  private static final Function<String, String> matchFieldRegex = name -> NOT_ESCAPED + "%(?<flags>\\W*?)" + name + "\\$(?<mods>[\\w\\-#+0,(]+)"; //Non-escaped %, name expression, $, assumed valid Java format codes
  private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile( matchFieldRegex.apply("(?<name>\\w+)") );
  
  private String name;
  private String stringForm;
  private List<Field> fields;
  private Field triggerField;
  private Pattern regex; // to memoize the regex for matching instantiated templates
  //TODO: change this from a regex-based solution to a parser-based one.
  
  
  /// Public Methods

  public Template(String stringForm) throws S2KParseException {
    try {
      String[] nameBody = stringForm.split("\n", 2);
      this.name         = nameBody[0];
      this.stringForm   = nameBody[1];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new S2KParseException("Invalid Template string form.", e);
    }
    
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

  public Template asChildOf(Template parent) {
    Template output     = new Template(this);
    output.name         = this.name + CHILD_TEMPLATE_MODIFIER + parent.name;
    return output;
  }
  
  public String getName() {
    return name;
  }
  
  public String getTriggerName() {
    return this.triggerField.name;
  }
  
  public List<Field> getFields() {
    return fields;
  }

  public Integer getContainmentDepth() {
    return this.name.split(CHILD_TEMPLATE_MODIFIER).length - 1;
  }
  
  /**
   * Extracts the name of the parent template.
   * @return Parent template's name, or this one's name if it's top-level.
   */
  public String getParentTemplateName() {
    if (!this.name.contains(CHILD_TEMPLATE_MODIFIER)) {
      return this.name;
    } else {
      return this.name.split(CHILD_TEMPLATE_MODIFIER, 2)[1];
    }
  }
  
  
  /**
   * Represents all parts of the target code that match the specified template.
   * @param target The target code to look in
   * @return A MatchRegistrar, with each TemplateMatch representing one instance of the template in the target.
   */
  public MatchRegistrar matchToTarget(String target, Collection<Template> allTemplates) {
    if (regex == null) {
      regex = this.asRegex();
    }
    
    MatchRegistrar output = new MatchRegistrar();
    
    Matcher targetMatcher = regex.matcher(target);
    while (targetMatcher.find()) {
      output = output.merge( TemplateMatch.fromMatcher(targetMatcher, this, allTemplates) );
    }
    return output;
  }
  
  /**
   * Uses the dataSource given to find all instances of this template in the source.
   * @param dataSource The TemplateDataSource describing how to retrieve information for this template.
   * @param source The source model to draw information from.
   * @return A Collection of TemplateMatches, each representing an instance of the template in the source.
   */
  public Collection<TemplateMatch> matchToSource(TemplateDataSource dataSource, JSONObject source) {
    if (!dataSource.containsKey( getTriggerName() )) {
      return Collections.emptyList();
    }
    Map<Path, Object> triggerMap = dataSource.get( getTriggerName() ).access(source);
    
    return triggerMap.entrySet().stream()
        .map( triggerEntry -> {
          TemplateMatch match = new TemplateMatch();
          match.put(getTriggerName(), triggerEntry.getValue().toString());
          dataSource.keySet().stream()
            .filter( fieldName -> !fieldName.equals(getTriggerName()) ) // for any field but the triggering field...
            .forEach( fieldName -> {
              Map<Path, Object> relativeLookup = dataSource.get(fieldName).access(source,
                  triggerEntry.getKey().withoutLeaves().access(source).values().iterator().next()); // access that path, with this trigger's containing object as the reference object
              relativeLookup.entrySet().stream()
                .map( pathValue -> new AbstractMap.SimpleEntry<>(pathValue.getKey().distance(triggerEntry.getKey()), pathValue.getValue()) ) // compute the distances to the triggering path
                .min( (distanceValue1, distanceValue2) -> distanceValue1.getKey().compareTo(distanceValue2.getKey()) ) // choose the "closest" path
                .ifPresent( distanceValue -> {
                  match.put(fieldName, distanceValue.getValue().toString()); // if we have a path, add it to the match
                });
            });
          return match;
        })
        .collect( Collectors.toList() );
  }
  
  /**
   * Builds the target code, and registers it with templateRegistrar.
   * @param templateMatch The TemplateMatch that describes information for this template.
   * @param templateRegistrar The TemplateRegistrar for this translation.
   */
  public void instantiate(TemplateMatch templateMatch, InstantiationRegistrar templateRegistrar) {
    String formatStr = GENERAL_FIELD_PATTERN.matcher(stringForm).replaceAll("%${mods}"); // replace all fields with Java String.format codes
    String[] fieldValues = new String[fields.size()];
    // Note: iteration order will be in the order of the template because of list structure.
    int i = 0;
    for (Field field : fields) {
      if (field.isRecursive) {
        fieldValues[i] = templateRegistrar.getOrDefault( templateMatch.get( getTriggerName() ), new LinkedList<String>() ).stream().collect( Collectors.joining("\n") );
      } else if (templateMatch.containsKey(field.name) || !field.isNecessary) {
        fieldValues[i] = templateMatch.getOrDefault(field.name, "");
      } else {
        return; // quit early if a necessary field isn't available
      }
      ++i;
    }
    Optional<String> reference = templateMatch.getParentReference();
    templateRegistrar.register( reference.orElseGet( templateRegistrar::getTopLevelReference ), String.format(formatStr, (Object[])fieldValues));
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
  
  private Template(Template other) {
    this.stringForm   = other.stringForm;
    this.fields       = other.fields;
    this.triggerField = other.triggerField;
    this.regex        = other.regex;
  }
  
  private Pattern asRegex() {
    String tempStrForm = Pattern.quote(stringForm).replaceAll("\\s+", patternUnquote("\\\\s+"));
    for (Field field : fields) {
      // replace the java-style naming pattern with a regex to capture and name the match in an instantiated template
      tempStrForm = tempStrForm.replaceAll(
          matchFieldRegex.apply( field.name ),
          patternUnquote( field.toRegexStr() ));
    }
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

  /// Public Sub-classes
  
  public static class Field {
    public String name;
    public boolean isTrigger;
    public boolean isLong;
    public boolean isRecursive;
    public boolean isNecessary;
    
    public Field(Matcher sourceMatcher) {
      this( sourceMatcher.group("name"),
            sourceMatcher.group("flags").contains(TRIGGER_FLAG),
            sourceMatcher.group("flags").contains(LONG_FLAG),
            sourceMatcher.group("flags").contains(RECUR_FLAG),
            sourceMatcher.group("flags").contains(NECESS_FLAG));
    }
    public Field(String name) {
      this(name, false, false, false, false);
    }
    public Field(String name, boolean isTrigger, boolean isLong, boolean isRecursive, boolean isNecessary) {
      this.name = name;
      this.isTrigger   = isTrigger;
      this.isLong      = isLong || isRecursive; // a recursive field must be a long one, because it contains an entire template.
      this.isRecursive = isRecursive;
      this.isNecessary = isNecessary || isTrigger; // a trigger is, by definition, necessary
    }
    
    public String toRegexStr() {
      // Explanation: if long type, use a non-greedy "match-anything" pattern
      // otherwise, assume we're looking for an identifier, and use a greedy, non-empy "match-word" pattern
      return String.format("(?<%s>%s)", name, (isLong ? ".*?" : "\\\\w+"));
    }

    public String toString() {
      return (isTrigger   ? TRIGGER_FLAG : "") +
             (isLong      ? LONG_FLAG    : "") +
             (isRecursive ? RECUR_FLAG   : "") +
             (isNecessary ? NECESS_FLAG  : "") +
             name;
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "Field")
          .put("name", name)
          .put("isTrigger", isTrigger)
          .put("isLong", isLong)
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
          jsonObj.getBoolean("isLong"),
          jsonObj.getBoolean("isRecursive"),
          jsonObj.getBoolean("isNecessary"));
    }
  }

}
