package gov.nasa.jpl.kservices.sysml2k;

import java.util.stream.Stream;

@SuppressWarnings("serial")
public class MatchRegistrar extends Registrar<Template,TemplateMatch> {
  public MatchRegistrar merge(MatchRegistrar other) {
    MatchRegistrar output = new MatchRegistrar();
    super.merge(other).forEach( (template, templateMatch) -> output.put(template, templateMatch) );
    return output;
  }
  
  public Stream<TemplatePair> instantiationStream() {
    return this.pairingStream()
        .map( entry -> new TemplatePair(entry.getKey(), entry.getValue()) )
        .sorted( (templatePair1, templatePair2) -> 
            -templatePair1.template.getContainmentDepth().compareTo( templatePair2.template.getContainmentDepth() ));
  }
  
  /// Public sub-classes
  
  public static class TemplatePair {
    public Template template;
    public TemplateMatch templateMatch;
    
    public TemplatePair(Template template, TemplateMatch templateMatch) {
      this.template = template;
      this.templateMatch = templateMatch;
    }
  }
}
