package gov.nasa.jpl.kservices.sysml2k;

import java.util.HashMap;

@SuppressWarnings("serial")
public class TranslationDescription extends HashMap<String, TranslationDescription.TranslationPair> {
  public static class TranslationPair {
    public TemplateDataSource templateDataSource;
    public Template template;
    
    public TranslationPair(TemplateDataSource templateDataSource, Template template) {
      this.templateDataSource = templateDataSource;
      this.template = template;
    }
  }
}
