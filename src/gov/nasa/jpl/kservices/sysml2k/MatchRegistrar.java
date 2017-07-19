package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Stream;

@SuppressWarnings("serial")
public class MatchRegistrar extends LinkedHashMap<Template, Collection<TemplateMatch>> {
  public void merge(MatchRegistrar other) {
    other.forEach( (template, matches) -> 
        matches.forEach( match -> this.register(template, match) ));
  }
  
  public void register(Template template, TemplateMatch match) {
    if (!this.containsKey(template)) {
      this.put(template, new LinkedList<TemplateMatch>());
    }
    this.get(template).add(match);
  }
  
  public Stream<TemplatePair> instantiationStream() {
    return this.entrySet().stream()
        .sorted( (templateMatches1, templateMatches2) -> // sort by descending depth: notice the minus sign
            -templateMatches1.getKey().getReferenceDepth().compareTo( templateMatches2.getKey().getReferenceDepth() ) )
        .flatMap( templateMatch -> templateMatch.getValue().stream()
            .map( match -> new TemplatePair(templateMatch.getKey(), match) ));
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
