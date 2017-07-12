package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.HashCodeBuilder;

class Path {
  private static enum ELEMENT_MATCH_TYPE { NONE, STRUCTURE, WILD, EXACT }
  private static final Collection<BiPredicate<Path,Path>> mergePredicates = makeMergePredicates();
  private static Collection<BiPredicate<Path,Path>> makeMergePredicates() {
    Collection<BiPredicate<Path,Path>> output = new LinkedList<BiPredicate<Path,Path>>();
    
    output.add((p1, p2) -> p1.isLeaf() && p2.isLeaf());
    output.add((p1, p2) -> matchAtLeast(p1, p2, ELEMENT_MATCH_TYPE.WILD));
    // A structural match at this level plus a real match for every branch of one path to a branch in the other
    output.add((path1, path2) -> {
      BiPredicate<Path, Path> branchMatch = (p1, p2) ->
        p1.branches.stream()
            .allMatch( b1 ->
              p2.branches.stream()
                  .anyMatch( b2 -> matchAtLeast(b1, b2, ELEMENT_MATCH_TYPE.EXACT) ));
        
      return matchAtLeast(path1.element, path2.element, ELEMENT_MATCH_TYPE.STRUCTURE) &&
             path1.branches.size() == path2.branches.size() &&
             branchMatch.test(path1, path2);
    });
    
    return output;
  }
  private static final BiPredicate<Path,Path> shouldMerge = mergePredicates.stream().reduce( BiPredicate::or ).orElse( (p1,p2) -> false );
  private static final Collection<Predicate<PathElement>> wildPredicates = makeWildPredicates();
  private static Collection<Predicate<PathElement>> makeWildPredicates() {
    Collection<Predicate<PathElement>> output = new LinkedList<Predicate<PathElement>>();
    
    output.add(pe -> 
        (pe instanceof AlternationPathElement) &&
        (((AlternationPathElement) pe).innerElements.size() >= 3));
    
    return output;
  }
  private static final Predicate<PathElement> shouldMakeWild = wildPredicates.stream().reduce( Predicate::or ).orElse( p -> false ); 
  
  private PathElement element;
  private List<Path> branches;
  
  /// Public methods
  
  public Path() {
    this.element  = new TagPathElement();
    this.branches = new LinkedList<Path>();
  }
  public Path(String tag) {
    this.element  = new TagPathElement(tag);
    this.branches = new LinkedList<Path>();
  }
  public Path(Integer index) {
    this.element  = new IndexPathElement(index.toString());
    this.branches = new LinkedList<Path>();
  }

  public boolean isLeaf() {
    return branches.size() == 0;
  }
  
  public boolean isLinear() {
    return branches.size() <= 1;
  }
  
  public Path copy() {
    Path output = new Path();
    output.element = this.element.copy();
    output.branches = this.branches.stream()
        .map( path -> path.copy() )
        .collect(Collectors.toList());
    return output;
  }
  
  public void addBranch(Path branch) {
    this.branches.add(branch);
  }
  
  public void addBranches(Collection<Path> branches) {
    this.branches.addAll(branches);
  }
  
  /**
   * Merges this and another Path, using heuristics to reduce complexity.
   * @param other The path to merge with.
   * @return The merged path.
   */
  public Path merge(Path other) {
    ELEMENT_MATCH_TYPE nodeMatch = this.element.match( other.element );
    // shallow copy this node:
    Path output = new Path();
    output.branches.addAll(this.branches);
    output.branches.addAll(other.branches);
    
    if (nodeMatch == ELEMENT_MATCH_TYPE.EXACT) {
      // Exact match, so copy and continue.
      output.element = this.element;
      
    } else if (nodeMatch == ELEMENT_MATCH_TYPE.WILD) {
      // Need to take the most general wildcard that matches.
      
      if (this.element.specLevel().compareTo(other.element.specLevel()) >= 0) {
        // this is at least as exact as other; so other is more general, and should be used
        output.element = other.element;
        
      } else {
        // other is more exact, so this is more general, and should be used
        output.element = this.element;
      }
      
    } else if ( this.branches.stream().anyMatch( p -> isRealMatch( this.element.match(p.element) ) ) ) {
      // we should skip an element here:
      output.element = new OptionalPathElement(this.element);
      
    } else if ( other.branches.stream().anyMatch( p -> isRealMatch( other.element.match(p.element) ) ) ) {
      // we should skip an element in other:
      output.element = new OptionalPathElement(other.element);
      
    } else {
      // we need both sides, so do an alternation here
      output.element = new AlternationPathElement(this.element, other.element);
    }
    
    output.simplify();
    return output;
  }
  
  /**
   * Simplifies this path according to an internal measure of complexity
   */
  public void simplify() {
    branches.forEach( Path::simplify );
    
    if (branches.size() == 1 && this.element.equals(new TagPathElement())) {
      // this is a do-nothing node, so de-facto remove it
      // does this by copying child's info, letting GC delete the child itself.
      Path child = branches.get(0);
      this.element  = child.element;
      this.branches = child.branches;
      return; // the child was already simplified, above, so we can save some work
    }
    
    if (shouldMakeWild.test(element)) {
      element = element.makeWild(); 
    }
    
    Path[] branchesTemp = branches.toArray(new Path[branches.size()]);
    
    Path b1, b2;
    for (int i = 0; i < branchesTemp.length; ++i) {
      b1 = branchesTemp[i];
      for (int j = i+1; j < branchesTemp.length; ++j) {
        b2 = branchesTemp[j];
        
        if (shouldMerge.test(b1, b2)) {
          branches.remove(j); // remove later element first
          branches.remove(i);
          branches.add(b1.merge(b2));
          this.simplify();
          return; // after that index-fiddling, can't safely use temp array
        }
      }
    }
  }
  
  public String toString() {
    String output = element.toString();
    switch (branches.size()) {
      case 0:
        break;
        
      case 1:
        output += branches.get(0).toString();
        break;
        
      default:
        output += "[" + branches.stream()
                         .map( b -> b.toString() )
                         .collect( Collectors.joining(",") ) + "]";
        break;
    }
    return output;
  }
  
  /// Private helpers
  
  private static boolean isRealMatch(ELEMENT_MATCH_TYPE emt) {
    return emt.compareTo(ELEMENT_MATCH_TYPE.WILD) >= 0;
  }
  
  private static boolean matchAtLeast(PathElement pe1, PathElement pe2, ELEMENT_MATCH_TYPE minimum) {
    return pe1.match(pe2).compareTo(minimum) >= 0;
  }
  private static boolean matchAtLeast(Path pe1, Path pe2, ELEMENT_MATCH_TYPE minimum) {
    return pe1.match(pe2).compareTo(minimum) >= 0;
  }

  private ELEMENT_MATCH_TYPE match(Path other) {
    if (this.isLeaf()) {
      return (other.isLeaf() ? this.element.match(other.element) : ELEMENT_MATCH_TYPE.NONE);
    } else {
      return Stream.<ELEMENT_MATCH_TYPE>concat(
                Stream.of( this.element.match(other.element) ),
                Stream.of( this.branches.stream()
                    .flatMap( b -> other.branches.stream().map( b::match ) ) // many-many branch match
                    .max(ELEMENT_MATCH_TYPE::compareTo) // take best branch's match
                    .orElse(ELEMENT_MATCH_TYPE.NONE)))
          .min(ELEMENT_MATCH_TYPE::compareTo) // take worst of this element and branch match
          .orElse(ELEMENT_MATCH_TYPE.NONE);
    }
  }
  
  
  /// Private inner classes
  
  private static abstract class PathElement {
    public abstract ELEMENT_MATCH_TYPE match(PathElement other);
    public abstract PathElement copy();
    public abstract ELEMENT_MATCH_TYPE specLevel();
    public abstract PathElement makeWild();
    public abstract String toString();
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof PathElement) &&
          (this.match((PathElement) other) == ELEMENT_MATCH_TYPE.EXACT);
    }
  }
  
  private static class TagPathElement extends PathElement {
    private static final String WILD = "*";
    private static final String HERE = ""; // Gains the necessary dot at toString step. Else, becomes "..".
    private String tag;
    
    public TagPathElement() {
      this.tag = HERE;
    }
    
    public TagPathElement(String tag) {
      this.tag = tag;
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      if (other instanceof TagPathElement) {
        String otherTag = ((TagPathElement)other).tag;
        
        if (this.tag.equals(otherTag)) {
          return ELEMENT_MATCH_TYPE.EXACT;

        } else if (this.tag.equals(WILD) || otherTag.equals(WILD)) {
          return ELEMENT_MATCH_TYPE.WILD;
          
        } else {
          return ELEMENT_MATCH_TYPE.STRUCTURE;
          
        }
      } else {
        return ELEMENT_MATCH_TYPE.NONE;
      }
    }
    
    public TagPathElement copy() {
      return new TagPathElement(tag);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return tag == WILD ? ELEMENT_MATCH_TYPE.STRUCTURE : ELEMENT_MATCH_TYPE.EXACT;
    }
    
    public PathElement makeWild() {
      return new TagPathElement(WILD);
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(tag)
          .toHashCode();
    }
    
    public String toString() {
      return "." + tag;
    }
  }
  
  private static class IndexPathElement extends PathElement {
    private static final String WILD = "*";
    private String index;
    
    public IndexPathElement(String index) {
      this.index = index;
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      if (other instanceof IndexPathElement) {
        String otherIndex = ((IndexPathElement)other).index;
        if (this.index.equals(otherIndex)) {
          return ELEMENT_MATCH_TYPE.EXACT;
        } else if (this.index.equals(WILD) || otherIndex.equals(WILD)) {
          return ELEMENT_MATCH_TYPE.WILD;
        } else {
          return ELEMENT_MATCH_TYPE.STRUCTURE;
        }
      } else {
        return ELEMENT_MATCH_TYPE.NONE;
      }
    }
    
    public IndexPathElement copy() {
      return new IndexPathElement(index);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return index == WILD ? ELEMENT_MATCH_TYPE.STRUCTURE : ELEMENT_MATCH_TYPE.EXACT;
    }

    public PathElement makeWild() {
      return new IndexPathElement(WILD);
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(index)
          .toHashCode();
    }
    
    public String toString() {
      return "[" + index + "]";
    }
  }
  
  private static class OptionalPathElement extends PathElement {
    private PathElement innerElement;
    
    public OptionalPathElement(PathElement innerElement) {
      this.innerElement = innerElement;
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      ELEMENT_MATCH_TYPE output = innerElement.match(other);
      if (output.compareTo(ELEMENT_MATCH_TYPE.STRUCTURE) < 0) {
        // promote STRUCTURE or lesser matches to WILD matches, which are stronger
        output = ELEMENT_MATCH_TYPE.WILD;
      }
      return output;
    }
  
    public PathElement copy() {
      return new OptionalPathElement(innerElement);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return ELEMENT_MATCH_TYPE.WILD;
    }

    public PathElement makeWild() {
      return innerElement.makeWild();
    }
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof OptionalPathElement) &&
             ( ((OptionalPathElement) other).innerElement.equals(this.innerElement) );
    }
    
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(innerElement)
          .toHashCode();
    }
    
    public String toString() {
      return String.format("[%s,.]", innerElement.toString());
    }
  }

  private static class AlternationPathElement extends PathElement {
    private Set<PathElement> innerElements;
    
    public AlternationPathElement(PathElement... innerElements) {
      this.innerElements = new LinkedHashSet<PathElement>();
      for (PathElement innerElement : innerElements) {
        if (innerElement instanceof AlternationPathElement) {
          // absorb an alternation, to prevent multi-level alternations
          this.innerElements.addAll(((AlternationPathElement) innerElement).innerElements);
        } else {
          this.innerElements.add(innerElement);
        }
      }
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      if (other instanceof AlternationPathElement) {
        AlternationPathElement altOther = (AlternationPathElement) other;
        // if every element in other exactly matches an element in this, we have an exact match for the overall alternation
        if (altOther.innerElements.size() == this.innerElements.size() &&
            altOther.innerElements.stream()
                .map( pe -> this.innerElements.stream()
                    .map( pe::match )
                    .max(ELEMENT_MATCH_TYPE::compareTo)
                    .orElse(ELEMENT_MATCH_TYPE.NONE) )
                .min(ELEMENT_MATCH_TYPE::compareTo)
                .orElse(ELEMENT_MATCH_TYPE.NONE) == ELEMENT_MATCH_TYPE.EXACT) {
          return ELEMENT_MATCH_TYPE.EXACT;
        }
      }
      return innerElements.stream()
          .flatMap( p -> Stream.of(p.match(other), other.match(p)) )
          .map( emt -> (emt == ELEMENT_MATCH_TYPE.EXACT ? ELEMENT_MATCH_TYPE.WILD : emt) ) // replace exact with wild
          .max( ELEMENT_MATCH_TYPE::compareTo )
          .orElse(ELEMENT_MATCH_TYPE.NONE);
    }
    
    public AlternationPathElement copy() {
      return new AlternationPathElement( 
          (PathElement[]) innerElements.stream().map( p -> p.copy() ).toArray() );
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      switch (innerElements.size()) {
        case 0:
          return ELEMENT_MATCH_TYPE.EXACT;
        case 1:
          return innerElements.iterator().next().specLevel();
        default:
          return ELEMENT_MATCH_TYPE.WILD;
      }
    }

    public PathElement makeWild() {
      return innerElements.iterator().next().makeWild();
    }
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof AlternationPathElement) &&
          ( ((AlternationPathElement) other).innerElements.equals(this.innerElements) );
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(innerElements)
          .toHashCode();
    }
    
    public String toString() {
      return "[" + innerElements.stream()
          .map( p -> p.toString().replaceAll("^\\[|\\]$", "") )
          .collect( Collectors.joining(",") ) + "]";
    }
  }
}