package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class S2KLearner {
  private static Scanner systemInScanner = new Scanner(System.in);
  private static Collection<Template> standardTemplates = null; 
  private static final Set<String> tagBlacklist = makeTagBlacklist();
  private static Set<String> makeTagBlacklist() {
    Set<String> output = new HashSet<String>();
    
    output.add("documentation");
    
    return output;
  }
  
  
  /// Public Methods
  public static void main(String[] args) {
    String input  = S2KUtil.readResource("/shapes-project.json"),
           output = S2KUtil.readResource("/shapes-project.k");
    
    List<Example> examples = new LinkedList<Example>();
    examples.add( new Example(input, output) );
    
    try {
      TranslationDescription result = learnTranslation(examples, true);
      
      Translator translator = new Translator(result);
      
      // test the tranlsator on the input used for learning, as a baseline
      String test_output = translator.translate( new JSONObject(input) );
      System.out.println(test_output);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  public static TranslationDescription learnTranslation(Collection<Example> examples) throws S2KException {
    return learnTranslation(examples, false);
  }
  public static TranslationDescription learnTranslation(Collection<Example> examples, boolean interactive) throws S2KException {
    if (standardTemplates == null) {
      standardTemplates = S2KUtil.readTemplateFile("/templates.k");
    }
    return innerLearnTranslation(standardTemplates, examples, interactive);
  }
  public static TranslationDescription learnTranslation(Collection<String> templateStrings, Collection<Example> examples) throws S2KException {
    return learnTranslation(templateStrings, examples, false);
  }
  public static TranslationDescription learnTranslation(Collection<String> templateStrings, Collection<Example> examples, boolean interactive) throws S2KException {
    List<Template> templates = templateStrings.stream()
        .map( t -> {
          try {
            return new Template(t);
          } catch (S2KParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
          }
        } )
        .collect( Collectors.toList() );
    return innerLearnTranslation(templates, examples, interactive);
  }
  
  public static class Example {
    public String input;
    public String output;
    
    public Example(String input, String output) {
      this.input  = input;
      this.output = output;
    }
  }
  
  /// Private Utilities
  
  private static boolean isNull(Object x) {
    return (x == null) || (x == JSONObject.NULL);
  }

  private static TranslationDescription innerLearnTranslation(Collection<Template> templates, Collection<Example> examples, boolean interactive) throws S2KException {
    TranslationDescription output = new TranslationDescription();
    
    for (Template template : templates) {
      /* Explanation of the stream work below:
       * create a list of data sources, each one a guess based on one example
       * turn input into a JSONObject by their parser
       * use the template matcher to parse the output for examples of this template
       * use matchElements to collate the Matches into a single DataSource
       * merge those DataSources to build the best approximation that we can
       */
      TemplateDataSource dataSource = examples.stream()
          .map( example -> matchElements( new JSONObject(example.input), template.match(example.output, templates) ) )
          .reduce( TemplateDataSource::merge )
          .orElseThrow(() -> new S2KException("Could not learn from given inputs."));
      
      if (interactive) {
        dataSource = getFeedback(template, dataSource);
      }
      
      output.put(template.getName(), new TranslationDescription.TranslationPair(dataSource, template));
    }
    
    return output;
  }
  
  private static TemplateDataSource getFeedback(Template template, TemplateDataSource dataSource) {
    TemplateDataSource output = new TemplateDataSource();
    
    System.out.printf("\nWorking on template: %s\n\n", template);
    
    dataSource.forEach( (name, path) -> {
      Path newPath = null;
      while (newPath == null) {
        try {
          System.out.printf("\nPath found for field `%s`: %s\n", name, path);
          System.out.println("Please enter replacement path, or leave empty to accept as is.");
          String pathStr = systemInScanner.nextLine().trim();
          newPath = (pathStr.equals("") ? path : Path.fromPathStr(pathStr));
        } catch (S2KParseException e) {
          System.out.println("Invalid path. Please enter a new path, or leave empty to accept as is.");
        }
      }
      output.put(name, newPath);
    });
    
    return output;
  }
  
  // All of these try to find the path for a single field
  private static Optional<Path> matchElement(JSONObject jsonObj, Collection<String> matchValues, Path node) {
    for (Object key : jsonObj.keySet()) {
      try {
        String keyStr = (String)key;
        if (tagBlacklist.contains(keyStr)) {
          // this isn't a tag we should actually be looking at.
          continue;
        }
        Object val = jsonObj.get(keyStr);
        matchElement(val, matchValues, new Path(keyStr)).ifPresent( node::addBranch );
      } catch (ClassCastException e) {
        // silently ignore the bad key
      }
    }
    if (node.isLeaf()) {
      // we didn't actually find any values on this branch, so prune it.
      return Optional.empty();
    } else {
      String type = jsonObj.optString("type");
      if (type == null || type.equals("")) {
        // use a general path
        return Optional.of(node);
      } else {
        // use a path that filters by type
        return Optional.of( new Path("type", type, node) );
      }
    }
  }
  private static Optional<Path> matchElement(JSONArray jsonObj, Collection<String> matchValues, Path node) {
    for (int i = 0; i < jsonObj.length(); ++i) {
      matchElement(jsonObj.get(i), matchValues, new Path(i)).ifPresent( node::addBranch );
    }
    if (node.isLeaf()) {
      // didn't actually find a value, prune this branch
      return Optional.empty();
    } else {
      return Optional.of(node);
    }
  }
  private static Optional<Path> matchElement(Object jsonObj, Collection<String> matchValues, Path node) {
    if (isNull(jsonObj)) {
      return Optional.empty();
      
    } else if (jsonObj instanceof JSONObject) {
      return matchElement((JSONObject)jsonObj, matchValues, node);
      
    } else if (jsonObj instanceof JSONArray) {
      return matchElement((JSONArray)jsonObj, matchValues, node);
      
    } else if (matchValues.contains(S2KUtil.ksanitize(jsonObj.toString()))) {
      return Optional.of(node);
      
    } else {
      return Optional.empty();
    }
  }
  
  /**
   * Attempts to link the source model to the matches for a Template.
   * @param jsonObj The source model to search in
   * @param matches Result of a matchTemplate call
   * @return A list of matching elements' paths, sorted by best match.
   */
  private static TemplateDataSource matchElements(JSONObject jsonObj, List<Template.Match> matches) {
    TemplateDataSource output = new TemplateDataSource();
    collateFieldValues(matches).forEach( (fieldName, matchValues) -> {
      matchElement(jsonObj, matchValues, new Path()).ifPresent( path -> {
        path.simplify();
        output.put(fieldName, path);
      });
    });
    return output;
  }
  
  /**
   * Groups all the values for a field together.
   * @param matches All the matches found for a template.
   * @return A map from field names to a collection of values for that field.
   */
  private static Map<String, Collection<String>> collateFieldValues(List<Template.Match> matches) {
    Map<String, Collection<String>> output = new HashMap<String, Collection<String>>();
    if (matches.isEmpty()) {
      return output;
    }
    for (String field : matches.get(0).keySet()) {
      output.put(field, new HashSet<String>());
    }
    for (Template.Match match : matches) {
      for (String field : match.keySet()) {
        output.get(field).add( match.get(field) );
      }
    }
    return output;
  }
}