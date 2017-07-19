package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class TranslationDescription extends LinkedHashMap<String, TranslationDescription.TranslationPair> {
  
  public void putMerge(TemplateDataSource templateDataSource, Template template) {
    if (this.containsKey(template.getName())) {
      TranslationPair modifying = this.get( template.getName() );
      modifying.templateDataSource = modifying.templateDataSource.merge( templateDataSource );
    } else {
      this.put( template.getName(), new TranslationPair(templateDataSource, template) );
    }
  }
  public void putMerge(TranslationPair translationPair) {
    putMerge(translationPair.templateDataSource, translationPair.template);
  }
  
  public String toString() {
    return String.format("{\n%s\n}", 
        this.entrySet().stream()
            .map( e -> String.format("%s=\n%s", e.getKey(), e.getValue()) )
            .collect( Collectors.joining("\n},\n{") ));
  }

  public JSONObject toJSON() {
    JSONObject output = new JSONObject().put("_type", "TranslationDescription");
    this.forEach( (key, translationPair) -> {
      output.put(key, translationPair.toJSON());
    });
    return output;
  }
  
  public static TranslationDescription fromJSON(JSONObject jsonObj) throws S2KParseException {
    if (!jsonObj.getString("_type").equals("TranslationDescription")) {
      throw new S2KParseException("Could not parse JSON as a TranslationDescription.");
    }
    TranslationDescription output = new TranslationDescription();
    for (Object key : jsonObj.keySet()) {
      if (!key.equals("_type")) {
        output.put((String) key, TranslationPair.fromJSON( jsonObj.getJSONObject((String) key) ));
      }
    }
    return output;
  }
  
  public static class TranslationPair {
    public TemplateDataSource templateDataSource;
    public Template template;
    
    public TranslationPair(TemplateDataSource templateDataSource, Template template) {
      this.templateDataSource = templateDataSource;
      this.template = template;
    }
      
    public String toString() {
      return String.format("%s -> %s", templateDataSource, template);
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "TranslationPair")
          .put("templateDataSource", templateDataSource.toJSON() )
          .put("template", template.toJSON() );
    }
    
    public static TranslationPair fromJSON(JSONObject jsonObj) throws S2KParseException {
      if (!jsonObj.getString("_type").equals("TranslationPair")) {
        throw new S2KParseException("Could not parse JSON as a TranslationPair.");
      }
      return new TranslationPair(
          TemplateDataSource.fromJSON(jsonObj.getJSONObject("templateDataSource")),
          Template.fromJSON(jsonObj.getJSONObject("template")));
    }
  }
}
