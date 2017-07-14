package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

@SuppressWarnings("serial")
public class TemplateDataSource extends LinkedHashMap<String, Path> {
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

  public JSONObject toJSON() {
    JSONObject data = new JSONObject().put("_type", "TemplateDataSource");
    this.forEach( (key, path) -> {
      data.put(key, path.toJSON());
    });
    return data;
  }
}
