package gov.nasa.jpl.kservices.sysml2k;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class TemplateDataSource extends HashMap<String, Path> {
  /**
   * Merges the Paths in this DataSource with those of another.
   * @param other The DataSource to merge with.
   * @return A new DataSource, with corresponding Paths merged.
   */
  public TemplateDataSource merge(TemplateDataSource other) {
    TemplateDataSource output = new TemplateDataSource();
    for (Map.Entry<String, Path> field : this.entrySet()) {
      output.put(field.getKey(), field.getValue().merge( other.get(field.getKey()) ));
    }
    return output;
  }
}
