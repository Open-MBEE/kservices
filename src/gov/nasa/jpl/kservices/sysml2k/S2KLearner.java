package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
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
      System.out.println(result.toJSON().toString(2));
      Translator translator = new Translator(result);
      
      // test the tranlsator on the input used for learning, as a baseline
      String test_output = translator.translate( new JSONObject(input) );
      System.out.println(test_output);
    } catch (Exception e1) {
      e1.printStackTrace();
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
  
  /// Private Utilities
  
  private static boolean isNull(Object x) {
    return (x == null) || (x == JSONObject.NULL);
  }

  private static TranslationDescription innerLearnTranslation(Collection<Template> templates, Collection<Example> examples, boolean interactive) throws S2KException {
    try {
      TranslationDescription output = new TranslationDescription();
      
      examples.forEach( example -> {// each example is handled separately at first
        JSONObject source = new JSONObject(example.input);
        templates.stream()
            .map( template -> template.matchToTarget(example.output, templates) ) // individually match each template (and its recursive sub-templates) to the target code
            .reduce( MatchRegistrar::merge ) // merge all instances of all templates into a single MatchRegistrar
            .ifPresent( matchRegistrar -> 
              matchRegistrar.entrySet().stream()
                  .sorted( (templateEntry1, templateEntry2) -> // get the shallow templates first, i.e., parents before their children 
                      templateEntry1.getKey().getContainmentDepth().compareTo( templateEntry2.getKey().getContainmentDepth() ))
                  .forEachOrdered( templateEntry -> {// then look up each template individually, regardless of recursive level
                    TranslationDescription.TranslationPair referencePair = null;
                    if (templateEntry.getKey().getContainmentDepth() > 0) { // but if it is recursive, additionally constrain the template
                      referencePair = output.get(templateEntry.getKey().getParentTemplateName());
                      // TODO: figure out how to properly do that constraining
                    }
                    TemplateDataSource dataSource = matchElements( source, templateEntry.getValue() );
                    output.putMerge( dataSource, templateEntry.getKey() ); // and merge the results into output incrementally
                  }));
      });
      
      if (interactive) {
        output.forEach( (templateName, translationPair) ->
          translationPair.templateDataSource = getFeedback(translationPair.template, translationPair.templateDataSource) );
      }
      
      return output;
    } catch (JSONException e) {
      throw new S2KParseException("Could not parse example inputs.", e);
    }
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
  private static Optional<Path> matchElement(JSONObject jsonObj, Collection<String> matchValues, Path node, Object reference) {
    for (Object key : jsonObj.keySet()) {
      try {
        String keyStr = (String)key;
        if (tagBlacklist.contains(keyStr)) {
          // this isn't a tag we should actually be looking at.
          continue;
        }
        Object val = jsonObj.get(keyStr);
        matchElement(val, matchValues, new Path(keyStr), reference).ifPresent( node::addBranch );
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
  private static Optional<Path> matchElement(JSONArray jsonObj, Collection<String> matchValues, Path node, Object reference) {
    for (int i = 0; i < jsonObj.length(); ++i) {
      matchElement(jsonObj.get(i), matchValues, new Path(i), reference).ifPresent( node::addBranch );
    }
    if (node.isLeaf()) {
      // didn't actually find a value, prune this branch
      return Optional.empty();
    } else {
      return Optional.of(node);
    }
  }
  private static Optional<Path> matchElement(Object jsonObj, Collection<String> matchValues, Path node, Object reference) {
    if (isNull(jsonObj)) {
      return Optional.empty();
      
    } else if (jsonObj instanceof JSONObject) {
      return matchElement((JSONObject)jsonObj, matchValues, node, reference);
      
    } else if (jsonObj instanceof JSONArray) {
      return matchElement((JSONArray)jsonObj, matchValues, node, reference);
      
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
  private static TemplateDataSource matchElements(JSONObject jsonObj, Collection<TemplateMatch> matches) {
    TemplateDataSource output = new TemplateDataSource();
    collateFieldValues(matches).forEach( (fieldName, matchValues) -> {
      matchElement(jsonObj, matchValues, new Path(), jsonObj).ifPresent( path -> {
        path.simplify();
        output.put(fieldName, path);
      });
    });
    return output;
  }
  
  /**
   * Groups all the values for a field together.
   * @param matches All the matches found for a template.
   * @return A Registrar from field names to values for that field.
   */
  private static Registrar<String,String> collateFieldValues(Collection<TemplateMatch> matches) {
    Registrar<String,String> output = new Registrar<>();
    
    matches.forEach( match -> match.forEach( output::register ));
    
    return output;
  }

  private static TemplateDataSource constrainByReference(TemplateDataSource toBeConstrained, TranslationDescription.TranslationPair referencePair, JSONObject source) {
    TemplateDataSource output = new TemplateDataSource();
    Path referencePath = referencePair.templateDataSource.get( referencePair.template.getTriggerName() ).withoutLeaves();
    toBeConstrained.forEach( (fieldName, path) -> {
      output.put(fieldName, constrainPathByReference(path, referencePath, source));
    });
    return output;
  }
  
  private static Path constrainPathByReference(Path toBeConstrained, Path reference, JSONObject source) {
    // TODO: Implement this
    throw new UnsupportedOperationException("Not implemented yet.");
  }
  
  /// Public sub-classes
  
  public static class Example {
    public String input;
    public String output;
    
    public Example(String input, String output) {
      this.input  = input;
      this.output = output;
    }
  }
  
}