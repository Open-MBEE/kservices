package gov.nasa.jpl.kservices.sysml2k;

import java.util.HashMap;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class TranslationDescription extends HashMap<String, TranslationDescription.TranslationPair> {
  
  public String toString() {
    return String.format("{\n%s\n}", 
        this.entrySet().stream()
            .map( e -> String.format("%s=\n%s", e.getKey(), e.getValue()) )
            .collect( Collectors.joining("\n,\n") ));
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
  }
}
