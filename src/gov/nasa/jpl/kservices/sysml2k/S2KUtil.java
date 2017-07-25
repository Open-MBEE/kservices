package gov.nasa.jpl.kservices.sysml2k;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class S2KUtil {
  /// Private members
  
  /**
   * Set of character that cannot be used in a K name.
   */
  protected static final Set<Character> allowedNameChars = makeAllowedNameChars();
  private static Set<Character> makeAllowedNameChars() {
    Set<Character> output = new LinkedHashSet<Character>();
    for (char c = 'a'; c <= 'z'; ++c) {
      output.add(c);
    }
    for (char c = 'A'; c <= 'Z'; ++c) {
      output.add(c);
    }
    for (char c = '0'; c <= '9'; ++c) {
      output.add(c);
    }
    output.add('_');
    return output;
  }
  
  /**
   * Set of keywords that are disallowed as identifiers in K.
   */
  protected static final Set<String> protectedKeywords = makeProtectedKeywords();
  private static Set<String> makeProtectedKeywords() {
    Set<String> output = new LinkedHashSet<String>();
    String[] keywords = readResource("/keywords.txt").split("\n");
    
    for (String kw : keywords) {
      output.add(kw); // maintain case sensitivity by not doing toLowerCase here.
    }
    
    return output;
  }

  /**
   * Set of keywords that are disallowed as identifiers in K.
   */
  protected static final Map<String,String> keywordDictionary = makeKeywordDictionary();
  private static Map<String,String> makeKeywordDictionary() {
    Map<String,String> output = new LinkedHashMap<String,String>();
    String[] keyAndTranslation = readResource("/keywordDictionary.txt").split("\n");
    
    for (String kt : keyAndTranslation) {
      String[] keyTranslation = kt.split("=");
      // use case-insensitive keys, but case-sensitive translations
      output.put(keyTranslation[0].toLowerCase(), keyTranslation[1]);
    }
    
    return output;
  }
  

  /// Public methods
  
  public static String streamToString(InputStream stream) {
    try (Scanner s = new Scanner(stream)) {
      String output = s.useDelimiter("\\Z").next();
      return output;
    }
  }
  public static List<Template> readTemplateFile(InputStream templateFile) throws S2KParseException {
    String[] templateStrings = streamToString(templateFile).split("\n([ \t]*\n){2,}");
    List<Template> templates = new ArrayList<Template>(templateStrings.length);
    
    for (String templateStr : templateStrings) {
      templates.add( new Template( templateStr ));
    }
    
    return templates;
  }

  /// Protected methods
  
  protected static String readResource(String resourceName) {
    return streamToString( S2KUtil.class.getResourceAsStream(resourceName) );
  }
  
  protected static List<Template> readTemplateFile(String resourceName) throws S2KParseException {
    return readTemplateFile( S2KUtil.class.getResourceAsStream(resourceName) );
  }
  
  /**
   * Prints and returns its argument.
   * Useful for inspecting calculations in place.
   * @param thing The thing to be printed
   * @return thing, unchanged
   */
  protected static <T> T peekPrint(T thing) {
    System.out.printf("PEEK: %s%n", thing); //DEBUG
    return thing;
  }
  
  /**
   * "Cleans" a name for use in K.
   * Prepends __ to keywords and replaces illegal characters with _
   * @param name The identifier to be sanitized.
   * @return new name, transformed as necessary to make a legal identifier
   */
  protected static String ksanitize(String name) {
    String output = cleanChars(name);
    return ( protectedKeywords.contains(output) ? output : "__" + output );
  }

  /**
   * Attempts to replace given identifier with native K construct.
   * @param name The original identifier
   * @return The closest K keyword, if available, or else the original name, using legal k characters.
   */
  protected static String knative(String name) {
    // first, check for the most precise translation available:
    String explicitTranslation = keywordDictionary.get(name.toLowerCase());
    if (explicitTranslation != null) {
      return explicitTranslation;
    }
    
    // else, check if this is equal to a keyword modulo case:
    for (String keyword : protectedKeywords) {
      if (name.equalsIgnoreCase(keyword)) {
        return keyword;
      }
    }
    
    // else, we have no "close" matches:
    return cleanChars(name);
  }
  
  /// Private helpers
  private static String cleanChars(String name) {
    String output = "";
    for (Character c : name.toCharArray()) {
      if (allowedNameChars.contains(c)) {
        output += c;
      } else {
        output += "_";
      }
    }
    return output;
  }
}

