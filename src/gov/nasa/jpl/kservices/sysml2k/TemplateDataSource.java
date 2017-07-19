package gov.nasa.jpl.kservices.sysml2k;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class TemplateDataSource extends LinkedHashMap<String, Path> {
  public TemplateDataSource() {
    this(null);
  }
  public TemplateDataSource(Template parentTemplate) {
    super();
  }
  
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
    JSONObject data = new JSONObject()
        .put("_type", "TemplateDataSource");
    this.forEach( (key, path) -> {
      data.put(key, path.toJSON());
    });
    return data;
  }
  
  public static TemplateDataSource fromJSON(JSONObject jsonObj) throws S2KParseException {
    if (!jsonObj.getString("_type").equals("TemplateDataSource")) {
      throw new S2KParseException("jsonObj does not represent a TemplateDataSource.");
    }

    TemplateDataSource output = new TemplateDataSource();
    
    @SuppressWarnings("unchecked")
    Set<String> keySet = jsonObj.keySet();
    keySet.stream()
        .filter( key -> !key.equals("_type") && !key.equals("parentTemplate") )
        .forEach( key -> {
          try {
            output.put(key, Path.fromJSON(jsonObj.getJSONObject(key)));
          } catch (S2KParseException | JSONException e) {
            // silently ignore exceptions, but log the exception
            e.printStackTrace();
          }
        });
    return output;
  }
}
