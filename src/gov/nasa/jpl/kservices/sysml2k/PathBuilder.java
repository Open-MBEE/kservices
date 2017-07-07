package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedList;

class PathBuilder {
  private LinkedList<PathElement> pathElements;

  public void append(String tag) {
    pathElements.addLast( new TagPathElement(tag) );
  }
  public void append(Integer index) {
    pathElements.addLast( new IndexPathElement(index.toString()) );
  }
  
  public PathBuilder withAppend(String tag) {
    PathBuilder output = copy();
    output.append(tag);
    return output;
  }

  public PathBuilder withAppend(Integer index) {
    PathBuilder output = copy();
    output.append(index);
    return output;
  }
  
  public PathBuilder copy() {
    PathBuilder output = new PathBuilder();
    for (PathElement pe : pathElements) {
      output.pathElements.addLast(pe.copy());
    }
    return output;
  }
  
  public String toString() {
    String output = "";
    for (PathElement pe : pathElements) {
      output += pe.toString();
    }
    output = output.substring(0, output.length() - 1);
    return output;
  }
  
  /// Private helpers
  
  private abstract class PathElement {
    public abstract PathElement copy();
    public abstract String toString();
  }
  
  private class TagPathElement extends PathElement {
    private String tag;
    
    public TagPathElement(String tag) {
      this.tag = tag;
    }
    
    public TagPathElement copy() {
      return new TagPathElement(tag);
    }
    
    public String toString() {
      return "." + tag;
    }
  }
  
  private class IndexPathElement extends PathElement {
    private String index;
    
    public IndexPathElement(String index) {
      this.index = index;
    }
    
    public IndexPathElement copy() {
      return new IndexPathElement(index);
    }
    
    public String toString() {
      return "[" + index + "]";
    }
  }
}
