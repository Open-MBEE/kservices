package gov.nasa.jpl.kservices.sysml2k;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

class Template {
  private static final String TRIGGER_FLAG = "!";
  private static final String RECUR_FLAG   = "@";
  private static final String NOT_ESCAPED = "(?<!\\\\)(?:\\\\{2})*"; // not preceded by an odd number of slashes. Stolen from maksymiuk (https://stackoverflow.com/questions/6525556/regular-expression-to-match-escaped-characters-quotes)
  private static final Function<String, String> matchFieldRegex = s -> NOT_ESCAPED + "%" + s + "\\$(?<mods>[\\w\\-#+0,(]+)"; //Non-escaped %, name expression, $, assumed valid Java format codes
  private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile( matchFieldRegex.apply(String.format("(?<trigger>%s)?(?<recur>%s)?(?<name>\\w+)", TRIGGER_FLAG, RECUR_FLAG)) );
  
  private String name;
  private String stringForm;
  private List<Field> fields;
  private Field triggerField;
  private Pattern regex; // to memoize the regex for matching instantiated templates
  //TODO: make this whitespace-agnostic
  
  
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
  
  public String instantiate(Match fieldMatches) {
    String formatStr = GENERAL_FIELD_PATTERN.matcher(stringForm).replaceAll("%${mods}"); // replace all fields with Java String.format codes
    String[] fieldValues = new String[fields.size()];
    // Note: iteration order will be in the order of the template because of list structure.
    int i = 0;
    for (Field field : fields) {
      fieldValues[i] = fieldMatches.getOrDefault(field.name, "");
      ++i;
    }
    return String.format(formatStr, (Object[])fieldValues);
  }
  
  public String toString() {
    return name + "\n" + stringForm;
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Template")
        .put("name", name)
        .put("stringForm", stringForm)
        .put("fieldNames", fields)
        .put("regex", regex.pattern());
  }
  
  /// Private Helpers
  
  private Pattern asRegex() {
    String tempStrForm = Pattern.quote(stringForm);
    for (Field field : fields) {
      // replace the java-style naming pattern with a regex to capture and name the match in an instantiated template
      tempStrForm = tempStrForm.replaceAll(
          matchFieldRegex.apply( field.toString() ),
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
  
  public class Field {
    public String name;
    public boolean isTrigger;
    public boolean isRecursive;
    
    public Field(Matcher sourceMatcher) {
      this( sourceMatcher.group("name"),
           (sourceMatcher.group("trigger") != null),   // true if `trigger` group was successful
           (sourceMatcher.group("recur")   != null) ); // true if `recur` group was successful
    }
    public Field(String name) {
      this(name, false, false);
    }
    public Field(String name, boolean isTrigger, boolean isRecursive) {
      this.name = name;
      this.isTrigger   = isTrigger;
      this.isRecursive = isRecursive;
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
          .put("isRecursive", isRecursive);
    }
  }

}
