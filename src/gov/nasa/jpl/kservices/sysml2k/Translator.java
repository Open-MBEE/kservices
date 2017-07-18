package gov.nasa.jpl.kservices.sysml2k;

import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;


public class Translator {
  private TranslationDescription translationDescription;
  
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
          new JSONObject( S2KUtil.readResource("/standardTranslationDescription.json") ));
    } catch (JSONException e) {
      throw new S2KParseException("Could not parse JSON as a TranslationDescription.", e);
    }
  }
  
  public Translator(TranslationDescription translationDescription) {
    this.translationDescription = translationDescription;
  }
  
  public String translate(JSONObject source) {
    return translationDescription.entrySet().stream()
        .flatMap( templateEntry ->
            templateEntry.getValue().template.instantiate(templateEntry.getValue().templateDataSource, source).stream() )
        .filter( s -> !s.isEmpty() )
        .collect( Collectors.joining("\n\n") );
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Translator")
        .put("translationDescription", translationDescription);
  }
}
