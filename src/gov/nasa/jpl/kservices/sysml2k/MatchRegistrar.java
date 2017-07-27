package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class MatchRegistrar extends Registrar<Template,TemplateMatch> {
  public MatchRegistrar merge(MatchRegistrar other) {
    MatchRegistrar output = new MatchRegistrar();
    super.merge(other).forEach( (template, templateMatch) -> output.put(template, templateMatch) );
    return output;
  }
  
  /**
   * Returns the templates and their matches, ordered so that instantiation dependencies are satisfied.
   * @return A List of TeplatePairs satisfying the above ordering principle.
   */
  public List<TemplatePair> instantiationOrder() {
    // TODO: Fix this. The ordering is wrong, I think.
    
    // Note: this is essentially a topological sort.
    // We use the Pairs as nodes, designated by their names, and compute edges as follows:
    // We define the source to be that templateMatch's name (the value of the trigger field)
    // We define the destination to be the parent reference, if any, of the templateMatch
    
    List<TemplatePair> output = new LinkedList<TemplatePair>();
    List<TemplatePair> needToAdd = this.pairingStream()
        .map( TemplatePair::new )
        .collect( Collectors.toList() );
    
    // define a pair to be a source if no pair yet to be added refers to this pair as its parent.
    // that is to say, we can add a node iff all the nodes that call this their parent have been added prior.
    Predicate<TemplatePair> isSourceNode = pair ->
      needToAdd.stream()
        .map( innerPair -> innerPair.templateMatch.getParentReference() )
        .filter( Optional::isPresent )
        .map( Optional::get )
        .noneMatch( pair.template.getReferenceName(pair.templateMatch)::equals );
        
    while (!needToAdd.isEmpty()) {
      // strip all the source nodes off the graph, and move them to the output
      List<TemplatePair> sources = needToAdd.stream()
        .filter( isSourceNode )
        .collect( Collectors.toList() );
      // just in case there are circular dependencies, for whatever reason:
      if (sources.isEmpty()) {
        output.addAll(needToAdd);
        break; // just lump all of these together, since we can no longer distinguish between them
      }
      output.addAll( sources );
      needToAdd.removeAll( sources );
    }
    
    return output;
  }
  
  /// Public sub-classes
  
  public static class TemplatePair {
    public Template template;
    public TemplateMatch templateMatch;
    
    private TemplatePair(Map.Entry<Template, TemplateMatch> entry) {
      this(entry.getKey(), entry.getValue());
    }
    
    public TemplatePair(Template template, TemplateMatch templateMatch) {
      this.template = template;
      this.templateMatch = templateMatch;
    }
  }
}
