package gov.nasa.jpl.kservices.sysml2k;

import com.jayway.jsonpath.JsonPath;

import gov.nasa.jpl.kservices.sysml2k.S2KUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class S2KLearner {
  /// Private Utilities
  
  /**
   * Returns all parts of the target code that match the specified template.
   * @param template A template to look for
   * @param target The target code to look in
   * @return A list of MatchResults, each representing one instance of the template in the target.
   */
  private static List<MatchResult> matchTemplate(Template template, String target) {
    Matcher templateMatcher = template.asTargetRegex().matcher(target);
    List<MatchResult> results = new LinkedList<MatchResult>();
    while (templateMatcher.find()) {
      results.add(templateMatcher.toMatchResult());
    }
    return results;
  }
  
  private static List<PathBuilder> matchElement(JSONObject jsonObj, MatchResult match) {
    Set<String> matchValues = new HashSet<String>();
    for (int i = 1; i <= match.groupCount(); ++i) {
      matchValues.add(match.group(i));
    }
    
    return matchElement(jsonObj, matchValues, new PathBuilder());
  }
  
  private static List<PathBuilder> matchElement(JSONObject jsonObj, Set<String> matchValues, PathBuilder currentPath) {
    List<PathBuilder> output = new LinkedList<PathBuilder>();
    
    for (Object key : jsonObj.keySet()) {
      try {
        String keyStr = (String)key;
        Object val = jsonObj.get(keyStr);
        output.addAll( matchElement(val, matchValues, currentPath.withAppend(keyStr)) );
      } catch (ClassCastException e) {
        // silently ignore the bad key
      }
    }
    throw new UnsupportedOperationException("Not implemented yet.");
  }
  
  private static List<PathBuilder> matchElement(JSONArray jsonObj, Set<String> matchValues, PathBuilder currentPath) {
    List<PathBuilder> output = new LinkedList<PathBuilder>();
    
    for (int i = 0; i < jsonObj.length(); ++i) {
      output.addAll( matchElement(jsonObj.get(i), matchValues, currentPath.withAppend(i)) );
    }
    
    return output;
  }
  
  private static List<PathBuilder> matchElement(Object jsonObj, Set<String> matchValues, PathBuilder currentPath) {
    if (jsonObj instanceof JSONObject) {
      return matchElement((JSONObject)jsonObj, matchValues, currentPath);
    } else if (jsonObj instanceof JSONArray) {
      return matchElement((JSONArray)jsonObj, matchValues, currentPath);
    } else {
      List<PathBuilder> output = new LinkedList<PathBuilder>();
      if (matchValues.contains(S2KUtil.ksanitize(jsonObj.toString()))) {
        output.add(currentPath);
      }
      return output;
    }
  }
  
  
  /**
   * Attempts to link a source model object to the given template matches.
   * @param jsonObj The source model to search in
   * @param matches Result of a matchTemplate call
   * @return A list of matching elements' paths, sorted by best match.
   */
  private static List<PathBuilder> matchElements(JSONObject jsonObj, List<MatchResult> matches) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
  
  private static String generatePath(Object jsonObj, Object target) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
  
  private static double comparePaths(String path1, String path2) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
  
  private static String mergePaths(List<String> paths) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
}