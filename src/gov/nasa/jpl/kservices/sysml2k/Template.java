package gov.nasa.jpl.kservices.sysml2k;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
  private static final Function<String, String> matchFieldRegex = name -> "(?:(?<=\n)(?<indent>[ \t]*))?" + NOT_ESCAPED + "%(?<flags>\\W*?)" + name + "(?:\\$(?<mods>[\\w\\-#+0,(]+))?"; //Non-escaped %, name expression, $, assumed valid Java format codes
  private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile( matchFieldRegex.apply("(?<name>\\w+)") );
  
  private String name;
  private String stringForm;
  private String recursiveIndent;
  private Map<String,Field> fields;
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
    fields = new LinkedHashMap<String,Field>( fieldMatcher.groupCount() );
    Boolean foundRecursive = false;
    
    while (fieldMatcher.find()) {
      Field newField = new Field( fieldMatcher );
      this.putField( newField );
      if (newField.isTrigger) {
        if (triggerField == null) {
          triggerField = newField;
        } else {
          throw new S2KParseException("Template " + name + " instantiated with more than one trigger field.");
        }
      }
      if (newField.isRecursive) {
        if (foundRecursive) {
          throw new S2KParseException("Template " + name + " instantiated with more than one recursive field.");
        }
        foundRecursive = true;
        this.recursiveIndent = fieldMatcher.group("indent"); // if no match, leaves recursiveIndent as null
      }
    }
    
    if (triggerField == null) {
      throw new S2KParseException("Template " + name + " instantiated with no trigger field.");
    }
    if (recursiveIndent == null) {
      this.recursiveIndent = "";
    }
  }

  /**
   * Builds an inner template. 
   * @param parent The Template in which this Template is contained.
   * @return A copy of this template, with appropriate modifications to be contained in the given parent.
   */
  public Template asChildOf(Template parent) {
    Template output     = new Template(this);
    output.name         = this.name + CHILD_TEMPLATE_MODIFIER + parent.name;
    return output;
  }
  
  /**
   * @return This Template's name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return Trigger Field's name.
   */
  public String getTriggerName() {
    return this.triggerField.name;
  }
  
  /**
   * @return A Collection of Fields used in this Template.
   */
  public Collection<Field> getFields() {
    return fields.values();
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
  public Collection<TemplateMatch> matchToSource(TemplateDataSource dataSource, JSONObject source, Collection<JSONObject> libraries) {
    if (!dataSource.containsKey( getTriggerName() )) {
      return Collections.emptyList();
    }
    Map<Path, Object> triggerMap = dataSource.get( getTriggerName() ).access(source, libraries);
    
    return triggerMap.entrySet().stream()
        .map( triggerEntry -> {
          TemplateMatch match = new TemplateMatch();
          match.put(getTriggerName(), triggerEntry.getValue().toString());
          Object triggerObject = triggerEntry.getKey().withoutLeaves().access(source, libraries).values().iterator().next();
          dataSource.keySet().stream()
            .filter( fieldName -> !fieldName.equals(getTriggerName()) ) // for any field but the triggering field...
            .forEach( fieldName -> {
              // access that path, with this trigger's reference object
              Map<Path, Object> relativeLookup = dataSource.get(fieldName).access(source, triggerObject, libraries);
              relativeLookup.entrySet().stream()
                .map( pathValue -> new AbstractMap.SimpleEntry<>(pathValue.getKey().distance(triggerEntry.getKey()), pathValue.getValue()) ) // compute the distances to the triggering path
                .min( (distanceValue1, distanceValue2) -> distanceValue1.getKey().compareTo(distanceValue2.getKey()) ) // choose the "closest" path
                .map( Map.Entry::getValue )
                .ifPresent( value -> match.put(fieldName, value.toString()) ); // if we have a path, add it to the match
            });
          return match;
        })
        .filter( this::isValidMatch )
        .collect( Collectors.toList() );
  }
  
  /**
   * Builds the target code, and registers it with templateRegistrar.
   * @param templateMatch The TemplateMatch that describes information for this template.
   * @param instantiationRegistrar The InstantiationRegistrar for this translation.
   */
  public void instantiate(TemplateMatch templateMatch, InstantiationRegistrar instantiationRegistrar) {
    Matcher fieldMatcher = GENERAL_FIELD_PATTERN.matcher(stringForm);
    List<String> fieldValues = new LinkedList<String>();
    // matcher will iterate forward through the stringForm, so we can guarantee order of fields
    while (fieldMatcher.find()) {
      Field field = fields.get(fieldMatcher.group("name"));
      if (field.isRecursive) {
        fieldValues.add( indent( 
            instantiationRegistrar.get( getReferenceName(templateMatch) ).stream()
                .collect( Collectors.joining("\n") )));
      } else {
        Optional<String> value = instantiateField(field, templateMatch);
        if (value.isPresent()) {
          fieldValues.add( value.get() );
        } else {
          return; // quit early if a necessary field isn't available
        }
      }
    }
    String formatStr = GENERAL_FIELD_PATTERN.matcher(stringForm).replaceAll("%${mods}"); // replace all fields with Java String.format codes
    
    Optional<String> reference = templateMatch.getParentReference();
    instantiationRegistrar.register(
        reference.orElseGet( instantiationRegistrar::getTopLevelReference ),
        String.format(formatStr, fieldValues.toArray()));
  }

  /**
   * Returns the name of this instance in the target code.
   * @param referenceMatch The TemplateMatch corresponding to the instantiation of the Template being referenced.
   * @return The name of that instantiation, as given in the target code.
   */
  public String getReferenceName(TemplateMatch referenceMatch) {
    return instantiateField(triggerField, referenceMatch).orElse("");
  }
  
  public String toString() {
    return name + "\n" + stringForm;
  }
  
  public JSONObject toJSON() {
    return toJSON(true);
  }
  public JSONObject toJSON(boolean strict) {
    if (strict) {
      return new JSONObject()
          .put("_type", "Template")
          .put("name", name)
          .put("stringForm", stringForm)
          .put("recursiveIndent", recursiveIndent)
          .put("fields", fields.values().stream()
              .map( Field::toJSON )
              .collect( Collectors.toList() ))
          .put("triggerField", triggerField.toJSON())
          .put("regex", regex.pattern());
    } else {
      return new JSONObject()
          .put("template", name + "\n" + stringForm)
          .put("fields", fields.values().stream()
              .map( field -> field.toJSON(strict) )
              .collect( Collectors.toList() ));
    }
  }
  
  public static Template fromJSON(JSONObject jsonObj) throws S2KParseException {
    try {
      if (!jsonObj.optString("_type", "Template").equals("Template")) {
        throw new S2KParseException("Could not parse JSON as a Template.");
      }
      
      Template output;
      if (!jsonObj.optString("template").isEmpty()) {
        // non-strict form
        output = new Template(jsonObj.getString("template")); // grab most of our info from the string format, assume there were no overrides
        output.fields = new LinkedHashMap<String,Field>();
        JSONArray fieldArr = jsonObj.optJSONArray("fields");
        if (fieldArr != null) {
          for (int i = 0; i < fieldArr.length(); ++i) {
            output.putField( Field.fromJSON( fieldArr.get(i) ));
          }
        }
      } else {
        // strict-form:
        output = new Template(); // we'll set all the fields manually
        output.name = jsonObj.getString("name");
        output.stringForm = jsonObj.getString("stringForm");
        output.recursiveIndent = jsonObj.getString("recursiveIndent");
        output.fields = new LinkedHashMap<String,Field>();
        JSONArray fieldArr = jsonObj.getJSONArray("fields");
        for (int i = 0; i < fieldArr.length(); ++i) {
          output.putField( Field.fromJSON( fieldArr.getJSONObject(i) ));
        }
        output.triggerField = Field.fromJSON( jsonObj.getJSONObject("triggerField") );
        output.regex = Pattern.compile( jsonObj.getString("regex") );
      }
      return output;
      
    } catch (JSONException e) {
      throw new S2KParseException("Could not parse JSON as a Template.", e);
    }
  }
  
  /// Private Helpers
  
  private Template() {
  }
  
  // shallow copy constructor
  private Template(Template other) {
  this.stringForm   = other.stringForm;
  this.fields       = other.fields;
  this.triggerField = other.triggerField;
  this.regex        = other.regex;
  }
  
  private void putField(Field field) {
    fields.put(field.name, field);
  }
  
  private Boolean isValidMatch(TemplateMatch templateMatch) {
    return this.fields.values().stream()
        .filter( field -> field.isNecessary && !field.isRecursive )
        .map( field -> field.name )
        .allMatch( templateMatch::containsKey );
  }
  
  private String indent(String recursiveContent) {
    return recursiveIndent + recursiveContent.replaceAll("\n", "\n" + recursiveIndent);
  }
  
  private Pattern asRegex() {
    String tempStrForm = Pattern.quote(stringForm).replaceAll("\\s+", patternUnquote("\\\\s+"));
    for (Field field : fields.values()) {
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

  private Optional<String> instantiateField(Field field, TemplateMatch templateMatch) {
    if ( ( templateMatch.containsKey(field.name)   &&
                 !templateMatch.get(field.name).isEmpty() ) ||
               !field.isNecessary) {
      String basicValue = templateMatch.getOrDefault(field.name, "");
      return Optional.of( field.isLong ? basicValue : S2KUtil.knative(basicValue) );
    } else {
      return Optional.empty();
    }
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
    
    public static Field fromString(String fieldStr) throws S2KParseException {
      try {
        Matcher matcher = GENERAL_FIELD_PATTERN.matcher(fieldStr);
        matcher.find();
        return new Field( matcher );
      } catch (Exception e) {
        throw new S2KParseException("Could not parse string as a Field.");
      }
    }
    
    public String toRegexStr() {
      // Explanation: if long type, use a non-greedy "match-anything" pattern
      // otherwise, assume we're looking for an identifier, and use a greedy, non-empy "match-word" pattern
      return String.format("(?<%s>%s)", name, (isLong ? ".*?" : "\\\\w+"));
    }

    public String toString() {
      return "%" +
             (isTrigger   ? TRIGGER_FLAG : "") +
             (isLong      ? LONG_FLAG    : "") +
             (isRecursive ? RECUR_FLAG   : "") +
             (isNecessary ? NECESS_FLAG  : "") +
             name;
    }
    
    public Object toJSON() {
      return toJSON(true);
    }
    public Object toJSON(boolean strict) {
      if (strict) {
        return new JSONObject()
            .put("_type", "Field")
            .put("name", name)
            .put("isTrigger", isTrigger)
            .put("isLong", isLong)
            .put("isRecursive", isRecursive)
            .put("isNecessary", isNecessary);
      } else {
        return this.toString();
      }
    }
    
    public static Field fromJSON(Object jsonObj) throws S2KParseException {
      try {
        if (jsonObj instanceof String) {
          return Field.fromString((String) jsonObj);
        } // else:
        
        JSONObject trueJsonObj = (JSONObject) jsonObj;
        if (!trueJsonObj.getString("_type").equals("Field")) {
          throw new S2KParseException("Could not parse JSON as a Field.");
        }
        return new Field(
            trueJsonObj.getString("name"),
            trueJsonObj.optBoolean("isTrigger", false),
            trueJsonObj.optBoolean("isLong", false),
            trueJsonObj.optBoolean("isRecursive", false),
            trueJsonObj.optBoolean("isNecessary", false));
      } catch (JSONException e) {
        throw new S2KParseException("Could not parse JSON as a Field.");
      }
    }
  }

}
