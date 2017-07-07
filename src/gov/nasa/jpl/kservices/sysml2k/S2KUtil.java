package gov.nasa.jpl.kservices.sysml2k;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

public class S2KUtil {
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
  
  private static String streamToString(InputStream stream) {
    try (Scanner s = new Scanner(stream)) {
      String output = s.useDelimiter("\\Z").next();
      return output;
    }
  }
  /**
   * Set of keywords that are disallowed as identifiers in K.
   */
  protected static final HashSet<String> protectedKeywords = makeProtectedKeywords();
  private static HashSet<String> makeProtectedKeywords() {
    HashSet<String> output = new HashSet<String>();
    String[] keywords = streamToString( S2KUtil.class.getResourceAsStream("/keywords.txt") ).split("\n");
    
    for (String kw : keywords) {
      output.add(kw.toLowerCase());
    }
    
    return output;
  }
  
  public static ReadContext readJSONfile(InputStream json) {
    return JsonPath.parse(json);
  }
  
  public static List<Template> readTemplateFile(InputStream templateFile) {
    String[] templateStrings = streamToString(templateFile).split("\n\\s*\n");
    List<Template> templates = new ArrayList<Template>(templateStrings.length);
    
    for (int i = 0; i < templateStrings.length; ++i) {
      templates.set(i, new Template( templateStrings[i] ));
    }
    
    return templates;
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
