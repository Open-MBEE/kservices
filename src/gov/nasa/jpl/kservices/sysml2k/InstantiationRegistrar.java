package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class InstantiationRegistrar extends LinkedHashMap<String, Collection<String>> {
  private static final String TOP_LEVEL_REFERENCE = "";
  
  public void register(String referenceName, String instantiation) {
    if (!this.containsKey(referenceName)) {
      this.put(referenceName, new LinkedList<String>());
    }
    this.get(referenceName).add(instantiation);
  }
  
  public String getTopLevelReference() {
    return TOP_LEVEL_REFERENCE;
  }
  
}
