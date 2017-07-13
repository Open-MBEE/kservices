package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class TranslationDescription extends LinkedHashMap<String, TranslationDescription.TranslationPair> {
  
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
  }
}
