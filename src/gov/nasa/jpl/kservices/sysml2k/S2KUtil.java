package gov.nasa.jpl.kservices.sysml2k;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class S2KUtil {
  /// Private members
  
  /**
   * Set of character that cannot be used in a K name.
   */
  protected static final HashSet<Character> allowedNameChars = makeAllowedNameChars();
  private static HashSet<Character> makeAllowedNameChars() {
    HashSet<Character> output = new HashSet<Character>();
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
  protected static final HashSet<String> protectedKeywords = makeProtectedKeywords();
  private static HashSet<String> makeProtectedKeywords() {
    HashSet<String> output = new HashSet<String>();
    String[] keywords = readResource("/keywords.txt").split("\n");
    
    for (String kw : keywords) {
      output.add(kw.toLowerCase());
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
  public static List<Template> readTemplateFile(InputStream templateFile) {
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
  
  protected static List<Template> readTemplateFile(String resourceName) {
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
    String output = "";
    for (Character c : name.toCharArray()) {
      if (allowedNameChars.contains(c)) {
        output += c;
      } else {
        output += "_";
      }
    }
    if (protectedKeywords.contains(output.toLowerCase())) {
      output = "__" + output;
    }
    return output;
  }
}

