package gov.nasa.jpl.kservices.sysml2k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      String input = S2KUtil.readResource("/shapes-project2.json");
//      String input = S2KUtil.readResource("/shapes-project.json");
//      String input = S2KUtil.readResource("/project.json");
      JSONObject source = new JSONObject(input);
      String test_output = translator.translate(source);
      System.out.println(test_output);
//      System.out.println(translator.translationDescription.toJSON(false).toString(2));

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
                matchRegistrar.register(translationPair.template, match) )); // associate each match individually with its template
    
    // Note: we use the collection forEach, not stream version, to maintain order.
    matchRegistrar.instantiationOrder()
        .forEach( templatePair -> templatePair.template.instantiate(templatePair.templateMatch, instantiationRegistrar) ); // instantiate every match, loading the instantiationRegistrar
    
    Set<String> realRefs = matchRegistrar.pairingStream()
        .map( entry -> entry.getKey().getReferenceName(entry.getValue()) )
        .collect( Collectors.toSet() );
    
    return Stream.concat(
            Stream.of( instantiationRegistrar.getTopLevelReference() ), // one explicit top-level reference, plus...
            matchRegistrar.pairingStream()
                .map( Map.Entry::getValue )
                .map( TemplateMatch::getParentReference )
                .filter( Optional::isPresent )
                .map( Optional::get )
                .filter( ref -> !realRefs.contains(ref) )) // anything that isn't a real reference is top-level, and needs to be collected
        .distinct()
        .map( instantiationRegistrar::get )
        .flatMap( Collection::stream )
        .collect( Collectors.joining("\n\n") );
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Translator")
        .put("translationDescription", translationDescription.toJSON())
        .put("libraries", libraries);
  }
}
