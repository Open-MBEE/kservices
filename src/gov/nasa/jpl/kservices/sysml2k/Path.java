package gov.nasa.jpl.kservices.sysml2k;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;

class Path {
  private PathElement element;
  private List<Path> branches;
  private enum ELEMENT_MATCH_TYPE { NONE, STRUCTURE, WILD, EXACT }
  private static final Map<ELEMENT_MATCH_TYPE, Integer> matchScore = makeMatchScore();
  private static Map<ELEMENT_MATCH_TYPE, Integer> makeMatchScore() {
    Map<ELEMENT_MATCH_TYPE, Integer> output = new HashMap<ELEMENT_MATCH_TYPE, Integer>();
    output.put(ELEMENT_MATCH_TYPE.EXACT,      0);
    output.put(ELEMENT_MATCH_TYPE.WILD,       0);
    output.put(ELEMENT_MATCH_TYPE.STRUCTURE, -1);
    output.put(ELEMENT_MATCH_TYPE.NONE,      -2);
    return output;
  }
  private static final Integer NONE_MATCH_SCORE = matchScore.get(ELEMENT_MATCH_TYPE.NONE); // for convenience and efficiency
  
  public Path() {
    this.element = new TagPathElement();
  }
  public Path(String tag) {
    this.element = new TagPathElement(tag);
  }
  public Path(Integer index) {
    this.element = new IndexPathElement(index.toString());
  }

  public boolean isLeaf() {
    return branches.size() == 0;
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
   * Computes a measure of similarity between two paths.
   * @param other The path to compare to.
   * @return A score, with lower numbers representing more disparate Paths.
   */
  public Integer compare(Path other) {
    Integer nodeScore = matchScore.get( this.element.match(other.element) );
    
    if (this.isLeaf() || other.isLeaf()) {
      return nodeScore + (NONE_MATCH_SCORE * Math.min(this.minDepth(), other.minDepth()));
      
    } else {
      // Best score possible between a branch here and a branch in other
      Integer branchScore = doubleBranchCompare(this.branches, other.branches);
      
      return Math.max(nodeScore + branchScore,                 // score if we match this node
          NONE_MATCH_SCORE + Math.max(                         // or take a NONE on this node and...
              branchScore,                                     // skip both sides (use an Alternation)
              Math.max(branchCompare(other.branches, this),    // skip other node (use Optional on other)
                       branchCompare(this.branches, other)))); // skip this node (use Optional on this)
    }
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
    Path[] branchesTemp   = (Path[]) branches.toArray();
    Integer[] branchComps = (Integer[]) branches.stream().map( Path::complexity ).toArray();
    
    
    Path b1, b2, merged;
    Integer c1, c2, mergedComp; // corresponding complexities
    for (int i = 0; i < branchesTemp.length; ++i) {
      b1 = branchesTemp[i];
      c1 = branchComps[i];
      for (int j = i+1; j < branchesTemp.length; ++j) {
        b2 = branchesTemp[j];
        c2 = branchComps[j];
        
        merged = b1.merge(b2);
        mergedComp = merged.complexity();
        
        if (mergedComp < c1 + c2) {
          branches.remove(i);
          branches.remove(j);
          branches.add(merged);
          this.simplify();
          return; // after that index-fiddling, can't safely use temp arrays
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
  
  private Integer minDepth() {
    return branches.stream()
        .map( (b) -> { return b.minDepth() + 1; } )
        .min( Comparator.naturalOrder() )
        .orElse(0);
  }
  
  private Integer complexity() {
    return element.complexity() + branches.stream().mapToInt( Path::complexity ).sum();
  }
  
  private static Integer branchCompare(List<Path> branches, Path path) {
    return branches.stream()
        .map(path::compare)
        .max(naturalOrder())
        .orElse(Integer.MIN_VALUE);
  }
  
  private static Integer doubleBranchCompare(List<Path> branches1, List<Path> branches2) {
    return branches1.stream()
        .map( sub -> branchCompare(branches2, sub) )
        .max(naturalOrder())
        .orElse(Integer.MIN_VALUE);
  }
  
  /// Private inner classes
  
  private static abstract class PathElement {
    public abstract ELEMENT_MATCH_TYPE match(PathElement other);
    public abstract PathElement copy();
    public abstract ELEMENT_MATCH_TYPE specLevel();
    public abstract String toString();
    
    public Integer complexity() {
      switch (this.specLevel()) {
        case EXACT:
          return 3;
        case WILD:
          return 4;
        case STRUCTURE:
          return 5;
        default:
          return 10;
      }
    }
  }
  
  private static class TagPathElement extends PathElement {
    private static final String WILD = "*";
    private static final String HERE = ".";
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
        
        if (this.tag == otherTag) {
          return ELEMENT_MATCH_TYPE.EXACT;

        } else if (this.tag == WILD || otherTag == WILD) {
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
        if (this.index == otherIndex) {
          return ELEMENT_MATCH_TYPE.EXACT;
        } else if (this.index == WILD || otherIndex == WILD) {
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
    
    public String toString() {
      return String.format("[%s,.]", innerElement.toString());
    }
  
    public Integer complexity() {
      return super.complexity() * innerElement.complexity();
    }
  }

  private static class AlternationPathElement extends PathElement {
    private List<PathElement> innerElements;
    
    public AlternationPathElement(PathElement... innerPaths) {
      this.innerElements = Arrays.asList(innerPaths);
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      return innerElements.stream()
          .map( p -> p.match(other) )
          .max( (m1, m2) -> m1.compareTo(m2) )
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
          return innerElements.get(0).specLevel();
        default:
          return ELEMENT_MATCH_TYPE.WILD;
      }
    }
    
    public String toString() {
      return "[" + innerElements.stream()
          .map( p -> p.toString().replaceAll("^\\[|\\]$", "") )
          .collect( Collectors.joining(",") ) + "]";
    }
    
    public Integer complexity() {
      return super.complexity() * (innerElements.stream().mapToInt( PathElement::complexity ).sum());
    }
  }
}