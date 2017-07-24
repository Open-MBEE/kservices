package gov.nasa.jpl.kservices.sysml2k;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.regex.Matcher;

import org.json.JSONObject;

import gov.nasa.jpl.kservices.sysml2k.Template.Field;

@SuppressWarnings("serial")
public class TemplateMatch extends LinkedHashMap<String, String> {
  private static final String PARENT_REFERENCE_NAME = "PARENT_REFERENCE";
  
  public static MatchRegistrar fromMatcher(Matcher matcher, Template referenceTemplate, Collection<Template> allTemplates) {
    MatchRegistrar output  = new MatchRegistrar();
    TemplateMatch newMatch = new TemplateMatch();
    
    for (Field field : referenceTemplate.getFields()) {
      if (field.isRecursive) {
        for (Template template : allTemplates) {
          template.matchToTarget(matcher.group(field.name), allTemplates).forEach( (templateName, matches) -> {
                Template childTemplate = template.asChildOf(referenceTemplate);
                matches.forEach( match -> {
                  // make top-level returned matches reference this match as their parent
                  match.putIfAbsent(PARENT_REFERENCE_NAME, matcher.group(referenceTemplate.getTriggerName()));
                  output.register(childTemplate, match);
                });
              });
        }
      } else {
        newMatch.put(field.name, matcher.group(field.name));
      }
    }
    
    output.register(referenceTemplate, newMatch);
    return output;
  }
  
  public Optional<String> getParentReference() {
    if (this.containsKey(PARENT_REFERENCE_NAME)) {
      return Optional.of(this.get(PARENT_REFERENCE_NAME));
    } else {
      return Optional.empty();
    }
  }
  
  public JSONObject toJSON() {
    return new JSONObject(this).put("_type", "Match");
  }
}