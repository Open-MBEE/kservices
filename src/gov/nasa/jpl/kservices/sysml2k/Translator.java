package gov.nasa.jpl.kservices.sysml2k;

import org.json.JSONObject;

public class Translator {
  private TranslationDescription translationDescription;
  
  public Translator(TranslationDescription translationDescription) {
    this.translationDescription = translationDescription;
  }
  
  public String translate(JSONObject source) {
    
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Translator")
        .put("translationDescription", translationDescription);
  }
}
