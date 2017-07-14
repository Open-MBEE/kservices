package gov.nasa.jpl.kservices.sysml2k;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.atn.SemanticContext.Predicate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Path {
  private static final Integer ALTERNATION_WILD_SIZE = 4; // the minimum number of elements to have before an alternation pattern turns into a wildcard
  
  private static enum ELEMENT_MATCH_TYPE { NONE, STRUCTURE, WILD, EXACT }
  
  private static final Collection<BiPredicate<Path,Path>> mergePredicates = makeMergePredicates();
  private static Collection<BiPredicate<Path,Path>> makeMergePredicates() {
    Collection<BiPredicate<Path,Path>> output = new LinkedList<BiPredicate<Path,Path>>();
    
    output.add((p1, p2) -> p1.isLeaf() && p2.isLeaf());
    output.add((p1, p2) -> matchAtLeast(p1.element, p2.element, ELEMENT_MATCH_TYPE.WILD));
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

  private static final BiFunction<Path,Path,Path> generalMergeFunction = (p1, p2) -> {
    Path merged = new Path(new AlternationPathElement(p1.element, p2.element));
    merged.branches.addAll(p1.branches);
    merged.branches.addAll(p2.branches);
    return merged;
  };
  private static final List<BiFunction<Path,Path,Optional<Path>>> mergeFunctions = makeMergeFunctions();
  private static List<BiFunction<Path,Path,Optional<Path>>> makeMergeFunctions() {
    List<BiFunction<Path,Path,Optional<Path>>> output = new LinkedList<BiFunction<Path,Path,Optional<Path>>>();
    
    output.add( (p1, p2) -> {
      if (matchAtLeast(p1.element, p2.element, ELEMENT_MATCH_TYPE.EXACT)) {
        Path merged = new Path(p1.element);
        merged.branches.addAll(p1.branches);
        merged.branches.addAll(p2.branches);
        return Optional.of(merged);
      } else {
        return Optional.empty();
      }
    });
    
    output.add( (p1, p2) -> {
      if (matchAtLeast(p1.element, p2.element, ELEMENT_MATCH_TYPE.WILD)) {
        Path merged = new Path(
            Stream.of(p1.element, p2.element)
                .min( (e1, e2) -> (e1.specLevel().compareTo(e2.specLevel())) ) // take the element with minimum specificity
                .orElse( p1.element )); // shouldn't ever actually hit this condition
        merged.branches.addAll(p1.branches);
        merged.branches.addAll(p2.branches);
        return Optional.of(merged);
      } else {
        return Optional.empty();
      }
    });
    
    output.add( (p1, p2) -> Optional.of(generalMergeFunction.apply(p1, p2)) );
    
    return output;
  }
  
  private static final List<Function<Path,Path>> atomicSimplifications = makeAtomicSimplifications();
  private static List<Function<Path,Path>> makeAtomicSimplifications() {
    List<Function<Path,Path>> output = new LinkedList<Function<Path,Path>>();
    
    output.add( p -> {
      if ((p.element instanceof AlternationPathElement) &&
          (((AlternationPathElement) p.element).innerElements.size() >= ALTERNATION_WILD_SIZE)) {
        Path simple = new Path(p.element.makeWild());
        simple.branches = p.branches;
        return simple;
      } else {
        return p;
      }
    });
    
    return output;
  }
  private static final Function<Path,Path> atomicSimplify = atomicSimplifications.stream().reduce( Function::compose ).orElseGet( Function::identity );
  
  private PathElement element;
  private List<Path> branches;
  
  /// Public methods
  
  public Path() {
    this(new TagPathElement());
  }
  public Path(String tag) {
    this(new TagPathElement(tag));
  }
  public Path(Integer index) {
    this(new IndexPathElement(index));
  }
  public Path(String attribute, String type, Path modifies) {
    this(modifies.element);
    Path branch = new Path(new AttributeFilterPathElement(new TagPathElement(attribute), type));
    branch.branches = modifies.branches;
    this.branches.add(branch);
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
  
  /**
   * Merges this and another Path, using heuristics to reduce complexity.
   * @param other The path to merge with.
   * @return The merged path.
   */
  public Path merge(Path other) {
    Path output = mergeFunctions.stream()
        .map( f -> f.apply(this, other) )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElseGet(() -> generalMergeFunction.apply(this, other));
    
    output.simplify();
    return output;
  }
  
  /**
   * Simplifies this path according to an internal measure of complexity
   */
  public void simplify() {
    branches.forEach( Path::simplify );
    
    Path atomicSimplified = atomicSimplify.apply(this);
    this.element  = atomicSimplified.element;
    this.branches = atomicSimplified.branches;
    
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
  
  public List<Object> access(Object jsonObj) {
    List<Object> values = this.element.access(jsonObj);
    if (this.isLeaf()) {
      return values;
    } else {
      return branches.stream()
          .flatMap( b -> values.stream().map( b::access ) )
          .collect( Collectors.toList() );
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
  
  public JSONObject toJSON() {
    return new JSONObject()
        .put("_type", "Path")
        .put("element", element.toJSON())
        .put("branches", branches.stream().map( Path::toJSON ).collect( Collectors.toList() ));
  }
  
  /// Private helpers

  private Path(PathElement element) {
    this.element = element;
    this.branches = new LinkedList<Path>();
  }

  private static boolean matchAtLeast(Path pe1, Path pe2, ELEMENT_MATCH_TYPE minimum) {
    return matchAtLeast(pe1, pe2, minimum, Path::match);
  }
  private static boolean matchAtLeast(PathElement pe1, PathElement pe2, ELEMENT_MATCH_TYPE minimum) {
    return matchAtLeast(pe1, pe2, minimum, PathElement::match);
  }
  private static <T> boolean matchAtLeast(T pe1, T pe2, ELEMENT_MATCH_TYPE minimum, BiFunction<T, T, ELEMENT_MATCH_TYPE> matcher) {
    return matcher.apply(pe1, pe2).compareTo(minimum) >= 0;
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
    public abstract List<Object> access(Object jsonObj);
    public abstract String toString();
    public abstract JSONObject toJSON();
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof PathElement) &&
          (this.match((PathElement) other) == ELEMENT_MATCH_TYPE.EXACT);
    }
  }
  
  private static class TagPathElement extends PathElement {
    private static final String WILD = "*";
    private static final String ROOT = "$"; // Gains the necessary dot at toString step. Else, becomes "..".
    private String tag;
    
    public TagPathElement() {
      this.tag = ROOT;
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
    
    public List<Object> access(Object jsonObj) {
      try {
        return Arrays.asList(((JSONObject) jsonObj).get(tag));
      } catch (ClassCastException | JSONException e) {
        return Arrays.asList();
      }
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(tag)
          .toHashCode();
    }
    
    public String toString() {
      return (tag.equals(ROOT) ? tag : "." + tag);
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "TagPathElement")
          .put("tag", tag);
    }
  }
  
  private static class IndexPathElement extends PathElement {
    private static final Integer WILD = -1; // Actually, any illegal index would work. Don't rely on particular value.
    private Integer index;
    
    public IndexPathElement(Integer index) {
      this.index = index;
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      if (other instanceof IndexPathElement) {
        Integer otherIndex = ((IndexPathElement)other).index;
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
    
    public List<Object> access(Object jsonObj) {
      try {
        return Arrays.asList(((JSONArray) jsonObj).get(index));
      } catch (ClassCastException | JSONException e) {
        return Arrays.asList();
      }
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
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "IndexPathElement")
          .put("index", index);
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
    
    public List<Object> access(Object jsonObj) {
      return innerElements.stream()
          .flatMap( e -> e.access(jsonObj).stream() )
          .collect( Collectors.toList() );
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
          .map( p -> p.toString() )
          .collect( Collectors.joining(",") ) + "]";
    }
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "AlternationPathElement")
          .put("innerElements", innerElements.stream().map( PathElement::toJSON ).collect( Collectors.toList() ));
    }
  }

  private static abstract class FilterPathElement extends PathElement {
  }
  
  private static class AttributeFilterPathElement extends FilterPathElement {
    private static final String WILD = "*";
    
    private PathElement attribute;
    private String value;
    
    public AttributeFilterPathElement(PathElement attribute, String type) {
      this.attribute = attribute;
      this.value     = type;
    }
    
    public ELEMENT_MATCH_TYPE match(PathElement other) {
      if (other instanceof AttributeFilterPathElement) {
        AttributeFilterPathElement attrOther = (AttributeFilterPathElement) other;
        
        if (this.value.equals(attrOther.value) &&
            matchAtLeast(this.attribute, attrOther.attribute, ELEMENT_MATCH_TYPE.EXACT)) {
          return ELEMENT_MATCH_TYPE.EXACT;
          
        } else if ((this.value.equals(WILD) || attrOther.value.equals(WILD)) &&
                   matchAtLeast(this.attribute, attrOther.attribute, ELEMENT_MATCH_TYPE.WILD)) {
          return ELEMENT_MATCH_TYPE.WILD;
          
        } else {
          return ELEMENT_MATCH_TYPE.STRUCTURE;
        }
      }
      
      return ELEMENT_MATCH_TYPE.NONE;
    }
    
    public PathElement copy() {
      return new AttributeFilterPathElement(attribute.copy(), value);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return Stream.of(
              (value.equals(WILD) ? ELEMENT_MATCH_TYPE.WILD : ELEMENT_MATCH_TYPE.EXACT),
              attribute.specLevel() )
          .min(ELEMENT_MATCH_TYPE::compareTo)
          .orElse(ELEMENT_MATCH_TYPE.NONE);
    }
    
    public PathElement makeWild() {
      // should this just be some kind of "delete me" value?
      // If a "wild" element really matches anything, then the filter is a null-op.
      return new AttributeFilterPathElement(attribute, WILD);
    }
    
    public List<Object> access(Object jsonObj) {
      try {
        return ( attribute.access(jsonObj).contains(value) ? Arrays.asList(jsonObj) : Arrays.asList() );
      } catch (ClassCastException | JSONException e) {
        return Arrays.asList();
      }
    }
    
    public String toString() {
      return String.format("?(@%s=%s)", attribute.toString(), value);
    }
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "AttributeFilterPathElement")
          .put("attribute", attribute.toJSON())
          .put("value", value);
    }
  }
}