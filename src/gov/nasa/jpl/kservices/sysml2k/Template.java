package gov.nasa.jpl.kservices.sysml2k;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Template {
  private static final String notEscaped = "(?<!\\\\)(?:\\\\{2})*"; // not preceded by an odd number of slashes. Stolen from maksymiuk (https://stackoverflow.com/questions/6525556/regular-expression-to-match-escaped-characters-quotes)
  private static final Function<String, String> matchFieldRegex = s -> notEscaped + "%" + s + "\\$[\\w\\-#+0,(]+(?: [\\w\\-#+0,(]+)?"; //Non-escaped %, name expression, $, assumed valid Java format codes
  private static final Function<String, String> fieldToRegex    = s -> String.format("(?<%s>.*?)", s);
  private static final Pattern generalFieldPattern = Pattern.compile( matchFieldRegex.apply("(\\w+)") );
  
  private String name;
  private String stringForm;
  private List<String> fieldNames;
  private Pattern regex; // to memoize the regex for matching instantiated templates
  
  /// Public Methods

  public Template(String stringForm) {
    String[] nameBody = stringForm.split("\n", 2);
    this.name = nameBody[0];
    this.stringForm = nameBody[1];
    Matcher fieldMatcher = generalFieldPattern.matcher(stringForm);
    fieldNames = new ArrayList<String>( fieldMatcher.groupCount() );
    while (fieldMatcher.find()) {
      fieldNames.add( fieldMatcher.group(1) );
    }
  }

  /**
   * Represents all parts of the target code that match the specified template.
   * @param target The target code to look in
   * @return A list of Matches, each representing one instance of the template in the target.
   */
  public List<Match> match(String target) {
    if (regex == null) {
      regex = this.asRegex();
    }
    
    List<Match> output = new LinkedList<Match>();
    Matcher targetMatcher = regex.matcher(target);
    while (targetMatcher.find()) {
      output.add(new Match(targetMatcher));
    }
    return output;
  }
  
  public String getName() {
    return name;
  }
  
  public String toString() {
    return name + "\n" + stringForm;
  }
  
  /// Private Helpers
  
  private Pattern asRegex() {
    String tempStrForm = Pattern.quote(stringForm);
    for (String fieldName : fieldNames) {
      // replace the java-style naming pattern with a regex to capture and name the match in a given template
      tempStrForm = tempStrForm.replaceAll(
          matchFieldRegex.apply(fieldName),
          patternUnquote( fieldToRegex.apply(fieldName) ));
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
  public class Match extends HashMap<String, String> {
    public Match(Matcher res) {
      for (String fieldName : fieldNames) {
        this.put(fieldName, res.group(fieldName));
      }
    }
  }
}
