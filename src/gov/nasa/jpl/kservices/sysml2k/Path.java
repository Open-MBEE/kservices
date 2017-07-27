package gov.nasa.jpl.kservices.sysml2k;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.AlternationelementContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.AttributefilterelementContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.DoubleattributefilterelementContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.IndexelementContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.NegationfilterelementContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.PathContext;
import gov.nasa.jpl.kservices.sysml2k.JsonPath2Parser.TagelementContext;

class Path {
  private static final Integer ALTERNATION_WILD_SIZE = 3; // the minimum number of elements to have before an alternation pattern turns into a wildcard
  private static final String WILD_TAG = "*";
  private static final String ROOT_TAG = "$";
  private static final String REFERENCE_TAG = "^";
  private static final String LIBRARY_TAG = "LIB:";
  private static final String WILD_INDEX = "*";
  
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
                  .anyMatch( b2 -> matchAtLeast(b1, b2, ELEMENT_MATCH_TYPE.WILD) ));
        
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
  
  static{
    new Path();
  }
  
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
    Path branch = new Path(new AttributeFilterPathElement(new Path(new TagPathElement(attribute)), type));
    branch.branches = modifies.branches;
    this.branches.add(branch);
  }
  
  public static Path fromPathStr(String pathStr) throws S2KParseException {
    try {
      ANTLRInputStream inputStream = new ANTLRInputStream(pathStr);
      JsonPath2Lexer lexer = new JsonPath2Lexer(inputStream);
      CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
      JsonPath2Parser parser = new JsonPath2Parser(commonTokenStream);
      PathVisitor pathVisitor = new PathVisitor();
      return pathVisitor.visit(parser.path());
    } catch (Exception e) {
      throw new S2KParseException("Could not parse string as a Path.", e);
    }
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
  
  public Path withoutLeaves() {
    if (this.isLeaf()) {
      return new Path(); // special case: if method is invoked directly on a leaf, return a root
    } else {
      Path output = new Path(this.element);
      output.branches = this.branches.stream()
          .filter( branch -> !branch.isLeaf() ) // trim leaves to prevent having root nodes at the tips
          .map( Path::withoutLeaves )
          .collect( Collectors.toList() );
      return output;
    }
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
  
  public Map<Path,Object> access(Object jsonObj, Collection<JSONObject> libraries) {
    return innerAccess(new AccessContext(jsonObj, jsonObj, jsonObj, libraries));
  }
  public Map<Path,Object> access(Object jsonObj, Object referenceJsonObj, Collection<JSONObject> libraries) {
    return innerAccess(new AccessContext(jsonObj, jsonObj, referenceJsonObj, libraries));
  }
  
  public Integer distance(Path other) {
    BiFunction<Path, Path, Integer> branchDistance = (p1, p2) -> p1.branches.stream()
        .mapToInt( branch -> p2.branches.stream()
            .mapToInt( branch::distance )
            .min()
            .orElse(0) )
        .sum();
    // Rough "edit distance" between Paths
    // The smaller branch distance will be because that side has fewer branches,
    // so we take the larger branch distance to take into account the essentially non-matching branches.
    return (matchAtLeast(this.element, other.element, ELEMENT_MATCH_TYPE.WILD) ? 0 : 1)
        + Math.max(branchDistance.apply(this, other), branchDistance.apply(other, this));
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
  
  public Object toJSON() {
    return this.toJSON(false);
  }
  
  public Object toJSON(boolean strict) {
    if (strict) {
      return new JSONObject()
          .put("_type", "Path")
          .put("element", element.toJSON())
          .put("branches", branches.stream().map( Path::toJSON ).collect( Collectors.toList() ));
    } else {
      return this.toString();
    }
  }
  
  public static Path fromJSON(Object jsonObj) throws S2KParseException {
    if (jsonObj instanceof String) {
      return Path.fromPathStr((String) jsonObj);
    } else if (jsonObj instanceof JSONObject) {
      return Path.innerFromJSON((JSONObject) jsonObj);
    } else {
      throw new S2KParseException("Unknown type, could not parse Path.");
    }
  }
  
  /// Private helpers

  private Path(PathElement element) {
    this.element = element;
    this.branches = new LinkedList<Path>();
  }

  private Map<Path,Object> innerAccess(AccessContext accessContext) {
    Map<PathElement,Object> values = this.element.access(accessContext);
    if (this.isLeaf()) {
      return values.entrySet().stream()
          .map( entry -> new AbstractMap.SimpleEntry<>(new Path(entry.getKey()), entry.getValue()) )
          .collect( Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue) );
    } else {
      return branches.stream()
          .flatMap( branch -> values.entrySet().stream()
              .flatMap( value -> branch.innerAccess( accessContext.swapObject(value.getValue()) ).entrySet().stream()
                  .map( pathValue -> {
                    Path wrapped = new Path(value.getKey());
                    wrapped.branches.add(pathValue.getKey());
                    return new AbstractMap.SimpleEntry<Path, Object>( wrapped, pathValue.getValue() );
                  }) ))
          .collect( Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue) );
    }
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

  private static Path innerFromJSON(JSONObject jsonObj) throws S2KParseException {
    try {
      if (!jsonObj.getString("_type").equals("Path")) {
        throw new S2KParseException("JSON object does not represent a Path");
      }
      Path output = new Path(PathElement.fromJSON(jsonObj.getJSONObject("element")));
      JSONArray jsonBranches = jsonObj.getJSONArray("branches");
      for (int i = 0; i < jsonBranches.length(); ++i) {
        output.addBranch( Path.fromJSON(jsonBranches.get(i)) );
      }
      return output;
    } catch (JSONException e) {
      throw new S2KParseException("JSON object could not be parsed as a Path.", e);
    }
  }
  
  /// Private inner classes
  
  private static class AccessContext {
    public Object object;
    public Object root;
    public Object reference;
    public Collection<JSONObject> libraries;
    
    public AccessContext(Object object, Object root, Object reference, Collection<JSONObject> libraries) {
      this.object    = object;
      this.root      = root;
      this.reference = reference;
      this.libraries = libraries;
    }
    
    public AccessContext copy() {
      return new AccessContext(object, root, reference, libraries);
    }
    
    public AccessContext swapObject(Object newObject) {
      AccessContext output = this.copy();
      output.object = newObject;
      return output;
    }
  }
  
  
  private static abstract class PathElement {
    protected static Map<String, Function<JSONObject, Optional<PathElement>>> fromJsonMethods = new LinkedHashMap<String, Function<JSONObject, Optional<PathElement>>>();

    static{
      // TODO: there has to be a better way to do this...
      // I need to initialize all the classes, but actually building one is really a kludge.
      new TagPathElement();
      new IndexPathElement(0);
      new AlternationPathElement();
      new NegationFilterPathElement(new Path());
      new AttributeFilterPathElement(new Path(), "type");
      new DoubleAttributeFilterPathElement(new Path(), new Path());
    }
    
    /// Public methods
    
    public abstract PathElement copy();
    public abstract ELEMENT_MATCH_TYPE specLevel();
    public abstract PathElement makeWild();
    public abstract Map<PathElement, Object> access(AccessContext accessContext);
    public abstract String toString();
    public abstract JSONObject toJSON();
    
    public static PathElement fromJSON(JSONObject jsonObj) throws S2KParseException {
      try {
        return fromJsonMethods.get(jsonObj.getString("_type")).apply(jsonObj)
            .orElseThrow( () -> new S2KParseException("JSON object could not be parsed as a PathElement.") );
      } catch (JSONException | NullPointerException e) {
        throw new S2KParseException("Could not parse JSON as a PathElement.", e);
      }
    }

    public ELEMENT_MATCH_TYPE match(PathElement other) {
      // handle symmetry at the top level, so implementers need only look from their side
      return Stream.of( this.lmatch(other), other.lmatch(this) )
          .max( ELEMENT_MATCH_TYPE::compareTo )
          .orElse( ELEMENT_MATCH_TYPE.NONE );
    }
    
    @Override
    public boolean equals(Object other) {
      return (other instanceof PathElement) &&
          (this.match((PathElement) other) == ELEMENT_MATCH_TYPE.EXACT);
    }
    
    /// Protected helpers
    
    protected abstract ELEMENT_MATCH_TYPE lmatch(PathElement other);
    
    protected static Map<PathElement, Object> newAccessMap() {
      return new LinkedHashMap<PathElement, Object>();
    }
    protected static Map<PathElement, Object> makeAccessMap(PathElement key, Object value) {
      Map<PathElement, Object> output = newAccessMap();
      output.put(key, value);
      return output;
    }
  }
  
  private static class TagPathElement extends PathElement {
    private static final String WILD = WILD_TAG;
    private static final String ROOT = ROOT_TAG;
    private static final String REF  = REFERENCE_TAG;
    private static final String LIB  = LIBRARY_TAG;
    private String tag;
    
    static{
      fromJsonMethods.put("TagPathElement", jsonObj -> Optional.of( new TagPathElement(jsonObj.getString("tag")) ));
    }
    
    /// Public methods
    
    public TagPathElement() {
      this.tag = ROOT;
    }
    
    public TagPathElement(String tag) {
      this.tag = tag;
    }
    
    public TagPathElement copy() {
      return new TagPathElement(tag);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return tag.equals(WILD) ? ELEMENT_MATCH_TYPE.STRUCTURE : ELEMENT_MATCH_TYPE.EXACT;
    }
    
    public PathElement makeWild() {
      return new TagPathElement(WILD);
    }
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      try {
        if (tag.equals(ROOT)) {
          return makeAccessMap(this, accessContext.root);
          
        } else if (tag.equals(REF)) {
          return makeAccessMap(this, accessContext.reference);
          
        } else if (tag.equals(LIB)) {
          Map<PathElement, Object> output = newAccessMap();
          accessContext.libraries.forEach( lib -> output.put(this, lib) );
          return output;
          
        } else if (tag.equals(WILD)) {
          JSONObject trueJsonObj = (JSONObject) accessContext.object;
          Map<PathElement, Object> output = newAccessMap();
          for (Object key : trueJsonObj.keySet()) {
            output.put( new TagPathElement((String) key), trueJsonObj.get((String) key) );
          }
          return output;
          
        } else {
          return makeAccessMap(this, ((JSONObject) accessContext.object).get(tag));
        }
      } catch (ClassCastException | JSONException e) {
        return newAccessMap();
      }
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(tag)
          .toHashCode();
    }
    
    public String toString() {
      return (tag.equals(ROOT) ||
              tag.equals(REF)  || 
              tag.equals(LIB)  ?  tag : "." + tag);
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "TagPathElement")
          .put("tag", tag);
    }
    
    /// Protected helpers
    
    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
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
  }
  
  private static class IndexPathElement extends PathElement {
    private static final String WILD_STR = WILD_INDEX;
    private static final Integer WILD = -1; // Actually, any illegal index would work. Don't rely on particular value.
    private Integer index;

    static{
      fromJsonMethods.put("IndexPathElement", jsonObj -> Optional.of( new IndexPathElement(jsonObj.getInt("index")) ));
    }
    
    /// Public methods
    
    public IndexPathElement(Integer index) {
      this.index = index;
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
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      try {
        JSONArray jsonArray = (JSONArray) accessContext.object;
        if (index.equals(WILD)) {
          Map<PathElement, Object> output = newAccessMap();
          for (int i = 0; i < jsonArray.length(); ++i) {
            output.put( new IndexPathElement(i), jsonArray.get(i) );
          }
          return output;
        } else {
          return makeAccessMap(this, jsonArray.get(index));
        }
      } catch (ClassCastException | JSONException e) {
        return newAccessMap();
      }
    }
    
    @Override
    public int hashCode() {
      return new HashCodeBuilder(19, 29)
          .append(index)
          .toHashCode();
    }
    
    public String toString() {
      return "[" + (index.equals(WILD) ? WILD_STR : index.toString()) + "]";
    }
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "IndexPathElement")
          .put("index", index);
    }

    /// Protected helpers
    
    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
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
  }
  
  private static class AlternationPathElement extends PathElement {
    private Set<PathElement> innerElements;

    static{
      fromJsonMethods.put("AlternationPathElement", jsonObj -> {
        try {
          JSONArray jsonElements = jsonObj.getJSONArray("innerElements");
          PathElement[] innerElements = new PathElement[jsonElements.length()];
          for (int i = 0; i < jsonElements.length(); ++i) {
            innerElements[i] = PathElement.fromJSON( jsonElements.getJSONObject(i) );
          }
          return Optional.of( new AlternationPathElement(innerElements) );
        } catch (S2KParseException e) {
          return Optional.empty();
        }
      });
    }
    
    /// Public methods
  
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
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      return innerElements.stream()
          .map( e -> e.access(accessContext) )
          .reduce( newAccessMap(), (acc, map) -> { acc.putAll(map); return acc; } );
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
  
    /// Protected helpers

    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
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
  }

  private static abstract class FilterPathElement extends PathElement {
  }
  
  private static class NegationFilterPathElement extends FilterPathElement {
    private Path negatedPath;
    
    static{
      fromJsonMethods.put("NegationFilterPathElement", jsonObj -> {
        try {
          return Optional.of(
              new NegationFilterPathElement(
                  Path.fromJSON( jsonObj.get("negatedPath") )));
        } catch (S2KParseException e) {
          return Optional.empty();
        }
      });
    }
    
    /// Public methods
    
    public NegationFilterPathElement(Path negatedPath) {
      this.negatedPath = negatedPath;
    }
    
    public PathElement copy() {
      return new NegationFilterPathElement( negatedPath.copy() );
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return ELEMENT_MATCH_TYPE.WILD;
    }
    
    public PathElement makeWild() {
      return this; // what does this mean, really?
    }
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      Map<Path, Object> lookup = negatedPath.innerAccess(accessContext);
      if (lookup.isEmpty()) {
        return makeAccessMap(this, accessContext.object);
      } else {
        return newAccessMap();
      }
    }
    
    public String toString() {
      return String.format("?(!%s)", negatedPath.toString());
    }
    
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "NegationFilterPathElement")
          .put("negatedPath", negatedPath.toJSON());
    }
    
    /// Protected helpers
    
    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
      if (other instanceof NegationFilterPathElement) {
        return Stream.of(
                ((NegationFilterPathElement) other).negatedPath.match(this.negatedPath),
                ELEMENT_MATCH_TYPE.STRUCTURE)
            .max( ELEMENT_MATCH_TYPE::compareTo ) // return no less than a STRUCTURE match
            .orElse(ELEMENT_MATCH_TYPE.STRUCTURE);
      }
      return ELEMENT_MATCH_TYPE.NONE;
    }
  }
  
  private static class AttributeFilterPathElement extends FilterPathElement {
    private static final String WILD = "*";
    
    private Path attribute;
    private String value;

    static{
      fromJsonMethods.put("AttributeFilterPathElement", jsonObj -> {
        try {
          return Optional.of(
              new AttributeFilterPathElement(
                  Path.fromJSON(jsonObj.get("attribute")),
                  jsonObj.getString("value") ));
        } catch (S2KParseException e) {
          return Optional.empty();
        }
      });
    }
    
    /// Public methods
    
    public AttributeFilterPathElement(Path attribute, String value) {
      this.attribute = attribute;
      this.value     = value;
    }
    
    public PathElement copy() {
      return new AttributeFilterPathElement(attribute.copy(), value);
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
//      return Stream.of(
//              (value.equals(WILD) ? ELEMENT_MATCH_TYPE.WILD : ELEMENT_MATCH_TYPE.EXACT),
//              attribute.specLevel() )
//          .min(ELEMENT_MATCH_TYPE::compareTo)
//          .orElse(ELEMENT_MATCH_TYPE.NONE);
      return ( value.equals(WILD) ? ELEMENT_MATCH_TYPE.WILD : ELEMENT_MATCH_TYPE.EXACT );
    }
    
    public PathElement makeWild() {
      // should this just be some kind of "delete me" value?
      // If a "wild" element really matches anything, then the filter is a null-op.
      return new AttributeFilterPathElement(attribute, WILD);
    }
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      Map<Path, Object> attrValues = attribute.innerAccess(accessContext);
      // define matching on a wild attribute to be just having that attribute
      if ( value.equals(WILD) && !attrValues.isEmpty() ) {
        return makeAccessMap(
            new AttributeFilterPathElement(attribute,
                attrValues.values().iterator().next().toString()), // choose an arbitrary value to "match" the wildcard
            accessContext.object);
        
      } else if ( attrValues.values().contains(value) ) {
        return makeAccessMap( this, accessContext.object ); // this is already a specific attribute value
        
      } else {
        return newAccessMap(); // no match at all
      }
    }
    
    public String toString() {
      return String.format("?(%s=\"%s\")", attribute.toString(), value);
    }
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "AttributeFilterPathElement")
          .put("attribute", attribute.toJSON())
          .put("value", value);
    }
  
    /// Protected helpers

    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
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
  }
  
  private static class DoubleAttributeFilterPathElement extends FilterPathElement {
    private Path mainAttribute, comparisonAttribute;

    static{
      fromJsonMethods.put("DoubleAttributeFilterPathElement", jsonObj -> {
        try {
          return Optional.of(
              new DoubleAttributeFilterPathElement(
                  Path.fromJSON(jsonObj.get("mainAttribute")),
                  Path.fromJSON(jsonObj.get("comparisonAttribute")) ));
        } catch (S2KParseException e) {
          return Optional.empty();
        }
      });
    }
    
    /// Public methods
    
    public DoubleAttributeFilterPathElement(Path mainAttribute, Path comparisonAttribute) {
      this.mainAttribute       = mainAttribute;
      this.comparisonAttribute = comparisonAttribute;
    }
    
    public PathElement copy() {
      return new DoubleAttributeFilterPathElement(mainAttribute.copy(), comparisonAttribute.copy());
    }
    
    public ELEMENT_MATCH_TYPE specLevel() {
      return ELEMENT_MATCH_TYPE.EXACT; // effectively disables wildcard merging
    }
    
    public PathElement makeWild() {
      return this; // dummy method, since merging is basically disabled for this.
    }
    
    public Map<PathElement, Object> access(AccessContext accessContext) {
      Map<Path, Object> mainAttrLookup = mainAttribute.innerAccess(accessContext),
                  comparisonAttrLookup = comparisonAttribute.innerAccess(accessContext);
      
      Set<Object> commonValues = new LinkedHashSet<>(mainAttrLookup.values());
      commonValues.retainAll(comparisonAttrLookup.values());
      if ( !commonValues.isEmpty() ) {
        return makeAccessMap( this, accessContext.object );
        
      } else {
        return newAccessMap(); // no match at all
      }
    }
    
    public String toString() {
      return String.format("?(%s=%s)", mainAttribute.toString(), comparisonAttribute.toString());
    }
  
    public JSONObject toJSON() {
      return new JSONObject()
          .put("_type", "DoubleAttributeFilterPathElement")
          .put("mainAttribute", mainAttribute.toJSON())
          .put("comparisonAttribute", comparisonAttribute.toJSON());
    }
  
    /// Protected helpers

    protected ELEMENT_MATCH_TYPE lmatch(PathElement other) {
      if (other instanceof DoubleAttributeFilterPathElement) {
        DoubleAttributeFilterPathElement attrOther = (DoubleAttributeFilterPathElement) other;
        
        if (matchAtLeast(this.mainAttribute, attrOther.mainAttribute, ELEMENT_MATCH_TYPE.EXACT) &&
            matchAtLeast(this.comparisonAttribute, attrOther.comparisonAttribute, ELEMENT_MATCH_TYPE.EXACT)) {
          return ELEMENT_MATCH_TYPE.EXACT;
          
        } else {
          return ELEMENT_MATCH_TYPE.STRUCTURE;
        }
      }
      
      return ELEMENT_MATCH_TYPE.NONE;
    }
  }

  
  private static class PathVisitor extends JsonPath2BaseVisitor<Path> {
    @Override
    public Path visitPath(PathContext ctx) {
      ElementVisitor elementVisitor = new ElementVisitor();
      PathElement element = ctx.element().accept(elementVisitor);
      Path output = new Path(element);
      if (ctx.branches() != null) {
        ctx.branches().path().stream()
            .map( path -> path.accept(this) )
            .forEach( output::addBranch );
      }
      
      return output;
    }
  }
  private static class ElementVisitor extends JsonPath2BaseVisitor<PathElement> {
    @Override
    public PathElement visitTagelement(TagelementContext ctx) {
      if (ctx.tag() != null) {
        return new TagPathElement(ctx.tag().getText());
      } else {
        return new TagPathElement(ctx.getText());
      }
    }
    
    @Override
    public PathElement visitIndexelement(IndexelementContext ctx) {
      String indexStr = ctx.index().getText();
      if (indexStr.equals(WILD_INDEX)) {
        return new IndexPathElement(0).makeWild();
      } else {
        return new IndexPathElement( Integer.valueOf(indexStr) );
      }
    }
    
    @Override
    public PathElement visitAlternationelement(AlternationelementContext ctx) {
      return new AlternationPathElement(
          ctx.element().stream()
              .map( element -> element.accept(this) ) // build each PathElement
              .toArray( length -> new PathElement[length] )); // package them to give to varargs constructor
    }
    
    @Override
    public PathElement visitNegationfilterelement(NegationfilterelementContext ctx) {
      return new NegationFilterPathElement( 
          ctx.path().accept( new PathVisitor() ));
    }
    
    @Override
    public PathElement visitAttributefilterelement(AttributefilterelementContext ctx) {
      return new AttributeFilterPathElement(
          ctx.path().accept( new PathVisitor() ),
          ctx.attributevalue().getText().replaceAll("^\"|\"$", "")); // trim quotes
    }
    
    @Override
    public PathElement visitDoubleattributefilterelement(DoubleattributefilterelementContext ctx) {
      PathVisitor pathVisitor = new PathVisitor();
      return new DoubleAttributeFilterPathElement(
          ctx.path(0).accept(pathVisitor),
          ctx.path(1).accept(pathVisitor));
    }
  }
}