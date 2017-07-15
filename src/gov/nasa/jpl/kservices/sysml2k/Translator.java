package gov.nasa.jpl.kservices.sysml2k;

import java.util.Map;

import org.json.JSONObject;


public class Translator {
  private TranslationDescription translationDescription;
  
  public Translator(TranslationDescription translationDescription) {
    this.translationDescription = translationDescription;
  }
  
  public String translate(JSONObject source) {
    String output = "";
    
    for (Map.Entry<String, TranslationDescription.TranslationPair> templateEntry : translationDescription.entrySet()) {
      Template template = templateEntry.getValue().template;
      TemplateDataSource dataSource = templateEntry.getValue().templateDataSource;
      if (dataSource.containsKey(template.getTriggerName())) {
        Path triggerPath = dataSource.get( template.getTriggerName() );
        for (Object triggerValue : triggerPath.access(source)) {
          Template.Match match = template.new Match();
          match.put(template.getTriggerName(), triggerValue.toString());
          // TODO: find the other values, not just the triggering value.
          output += template.instantiate(match) + "\n\n";
        }
      }
    }
    
    return output;
  }
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Translator")
        .put("translationDescription", translationDescription);
  }
}
