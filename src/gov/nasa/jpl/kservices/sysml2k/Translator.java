package gov.nasa.jpl.kservices.sysml2k;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;


public class Translator {
  private static final String DEFAULT_TRANSLATION_DESCRIPTION = "/standardTranslationDescription.json";
  private static final Collection<String> DEFAULT_LIBRARIES = Arrays.asList("/sysml.json");
  
  private TranslationDescription translationDescription;
  private Collection<JSONObject> libraries;
  
  public static void main(String[] args) {
    try {
      Translator translator = new Translator();
      String input = S2KUtil.readResource("/shapes-project.json");
      JSONObject source = new JSONObject(input);
      String test_output = translator.translate(source);
      System.out.println(test_output);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Translator() throws S2KParseException {
    try {
      this.translationDescription = TranslationDescription.fromJSON(
          new JSONObject( S2KUtil.readResource(DEFAULT_TRANSLATION_DESCRIPTION) ));
      
      this.libraries = DEFAULT_LIBRARIES.stream()
          .map( S2KUtil::readResource )
          .map( JSONObject::new )
          .collect( Collectors.toList() );
      
    } catch (JSONException e) {
      throw new S2KParseException("Could not parse default translation description and library files.", e);
    }
  }
  
  public Translator(TranslationDescription translationDescription) {
    this.translationDescription = translationDescription;
  }
  
  public String translate(JSONObject source) {
    // need "global" registrars, w.r.t. the templates, so they can communicate after a fashion.
    MatchRegistrar matchRegistrar = new MatchRegistrar();
    InstantiationRegistrar instantiationRegistrar = new InstantiationRegistrar();
    translationDescription.values().stream()
        .forEach( translationPair -> 
            translationPair.template.matchToSource(translationPair.templateDataSource, source, libraries).forEach( match ->
                matchRegistrar.register(translationPair.template, match) )); // associate each map individually with its template
    matchRegistrar.instantiationStream()
        .forEachOrdered( templatePair -> templatePair.template.instantiate(templatePair.templateMatch, instantiationRegistrar) ); // instantiate every match, deepest reference first, loading the templateRegistrar
    return instantiationRegistrar.get( instantiationRegistrar.getTopLevelReference() ).stream().collect( Collectors.joining("\n\n") ); // access and join the top-level declarations
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Translator")
        .put("translationDescription", translationDescription.toJSON())
        .put("libraries", libraries);
  }
}
