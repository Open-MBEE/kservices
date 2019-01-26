package gov.nasa.jpl.kservices;

import gov.nasa.jpl.ae.event.ParameterListenerImpl;
import gov.nasa.jpl.ae.event.TimeVaryingMap;
import gov.nasa.jpl.ae.solver.HasIdImpl;
import gov.nasa.jpl.ae.util.distributions.Distribution;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.mbee.util.FileUtils;
import gov.nasa.jpl.mbee.util.Random;
import gov.nasa.jpl.mbee.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Tester {
    public static final char FS = File.separatorChar;
    public static final String TEST_CASE_DIR = "test" + FS + "gov" + FS + "nasa" + FS + "jpl" + FS + "kservices" + FS + "kTestCases" + FS;
    public static final String TEST_SOLN_DIR = "test" + FS + "gov" + FS + "nasa" + FS + "jpl" + FS + "kservices" + FS + "kTestCaseSolutions" + FS;
    public static final String TEST_SOLN_EXT_OLD = "Solution";
    public static final String TEST_SOLN_EXT_NEW = "Solution2";
    
    public static final String CONSTRAINT_FORMAT = "req\\s+(?<var>[a-zA-Z0-9_.]+)\\s*=\\s*(?<value>.*)\\s*";
    
    public static void checkSolution(String kFileName) throws FileNotFoundException {
      try {
        checkSolution(kFileName, true);
        return;
      } catch (FileNotFoundException e) {
        // ignore, try the old format
      }
      try {
        checkSolution(kFileName, false);
        return;
      } catch (FileNotFoundException e) {
        fail("Could not find a solution file to check against.");
      }
    }
    public static void checkSolution(String kFileName, boolean useNewFormat) throws FileNotFoundException {
        TimeVaryingMap<Integer> tvm = new TimeVaryingMap<>( "tvm" );
        HasIdImpl.reset();
        ParameterListenerImpl.reset();
        Random.reset();
        Distribution.reset();
        
        String expectedSolnPath = TEST_SOLN_DIR + kFileName + (useNewFormat ? TEST_SOLN_EXT_NEW : TEST_SOLN_EXT_OLD);
        File expectedSolnFile = new File(expectedSolnPath);
        if (!expectedSolnFile.exists()) {
          throw new FileNotFoundException("Solution file " + expectedSolnPath + " does not exist.");
        }
        String expectedSolution = FileUtils.fileToString( expectedSolnFile);

        String inputFile = TEST_CASE_DIR + kFileName + ".k";
        File f = new File(inputFile);
        assertTrue( f.exists() );

        String[] input = new String[]{inputFile};
        KtoJava.main(input);

        String outputSolnPath = EventXmlToJava.generatedCodeLocation + FS + "generatedCode" + FS + "solution.log";
        File outputSolnFile = new File( outputSolnPath );

        String outputSolution = "";
        if ( outputSolnFile.length() > 2 ){
            outputSolution = FileUtils.fileToString( outputSolnFile );
        } else {
            System.err.println("ERROR! Solution file is empty! " + outputSolnPath);
        }

        outputSolution = outputSolution.replaceAll( "@[a-f1-9][a-f0-9]*", "" );
        expectedSolution = expectedSolution.replaceAll( "@[a-f1-9][a-f0-9]*", "" );

        if (useNewFormat) {
          assertSolution(expectedSolution, outputSolution);
        } else {
          assertEquals(expectedSolution, outputSolution);
        }
    }
    
    /**
     * Specialized checking to compare two solutions more intelligently
     * Specifically, takes an expected formatted as a JSON description of variable values or ranges, like this:
     *   {
     *     "x": 4,
     *     "y": "[7,8)"
     *     "a.b.z": "Hello, world!"
     *   }
     * then extracts the values from the solution and compares them. When a range is specified, as for y above,
     * the extracted value is checked for membership in that range.
     * @param expected The String representation of the expected (gold standard) solution
     * @param actual The String representation of the actual output
     * @see {@link #assertSolutionEquals(Object, Object)}
     */
    public static void assertSolution(String expected, String actual) {
      JSONObject jexp = null;
      JSONObject jact = null;
      try {
        jact = new JSONObject(actual);
      } catch (JSONException e) {
      }
      
      assertNotNull(jact);
      assertTrue(jact.has("result"));
      assertTrue(jact.get("result") instanceof JSONObject);
      
      if (expected.equalsIgnoreCase("fail") ||
          expected.equalsIgnoreCase("failed") ||
          expected.equalsIgnoreCase("failure")) {
        // if "failed to solve" is desired result, just check the satisfied value:
        assertTrue(jact.getJSONObject("result").has("satisfied"));
        assertTrue(Utils.isFalse( jact.getJSONObject("result").get("satisfied") ));
        return;
      } // else assert that we succeed, and do a full solution check:
      
      assertTrue(jact.getJSONObject("result").has("satisfied"));
      assertTrue(Utils.isTrue( jact.getJSONObject("result").get("satisfied") ));

      try {
        jexp = new JSONObject(expected);
      } catch (JSONException e) {
        System.err.println("Could not parse expected as JSON object.");
        e.printStackTrace();
      }
      
      assertNotNull(jexp);
      assertTrue(jact.getJSONObject("result").has("constraints"));
      assertTrue(jact.getJSONObject("result").get("constraints") instanceof JSONArray);

      JSONArray constraints = jact.getJSONObject("result").getJSONArray("constraints");

      Pattern constraintPattern = Pattern.compile(CONSTRAINT_FORMAT);
      
      Map<String, String> actualValues = new LinkedHashMap<>();
      
      for (int i = 0; i < constraints.length(); ++i) {
        assertTrue("Constraint " + constraints.get(i) + " is not a string.", constraints.get(i) instanceof String);
        String str = constraints.getString(i);
        Matcher m = constraintPattern.matcher(str);
        assertTrue(m.matches());
        assertTrue(m.groupCount() == 2);
        String varName = m.group("var");
        String value = m.group("value");
        assertNotNull(varName);
        assertNotNull(value);
        actualValues.put(varName, value);
      }
      
      for (Object key : jexp.keySet()) {
        if (!(key instanceof String)) continue; // no idea how this could fire, but we'll be safe anyways
        String keyStr = (String)key;
        assertTrue("Output does not report a value for " + keyStr, actualValues.containsKey(keyStr));
        Object expVal = jexp.get(keyStr);
        Object actVal = actualValues.get(keyStr);
        assertSolutionEquals(expVal, actVal);
      }
    }

    /**
     * Specialized checking to compare two solutions more intelligently
     * In particular, if expected is a String specifying a range of values,
     * this will check if actual is a value in that range.
     * @param expected The expected (gold standard) solution value
     * @param actual The actual output value
     */
    protected static void assertSolutionEquals(Object expected, Object actual) {
      if (expected == null) {
        assertNull(actual);
        return;
      } else {
        assertNotNull(actual);
      }
      Double actualDouble = null;
      if (actual instanceof Number) {
        actualDouble = ((Number) actual).doubleValue();
      } else if (actual instanceof String && Utils.isNumber((String)actual)) {
        actualDouble = Utils.toDouble((String)actual);
      }
      
      if (expected instanceof String &&
          ((String) expected).matches("^\\s*[\\[(].*,.*[\\])]\\s*$")) {
        boolean isValidRange = true;
        
        String expStr = ((String) expected).trim();
        boolean incLB = expStr.startsWith("[");
        boolean incUB = expStr.endsWith("]");
        String[] bounds = expStr.replaceAll("^[\\[(]|[\\])]$", "").split(",");
        isValidRange &= bounds.length == 2;
        
        Double lb = null, ub = null;
        if (isValidRange) {
          lb = Utils.toDouble(bounds[0]);
          ub = Utils.toDouble(bounds[1]);
        }
        isValidRange &= (lb != null) && (ub != null);
        
        if (isValidRange) {
          assertNotNull("Output " + actual + " was not a Number.", actualDouble);
          int lbComp = Utils.compare(lb, actualDouble);
          int ubComp = Utils.compare(ub, actualDouble);
          if (incLB) {
            assertTrue(String.format("Value %f was less than non-strict lower bound %f", actualDouble, lb), lbComp <= 0);
          } else {
            assertTrue(String.format("Value %f was less than or equal to strict lower bound %f", actualDouble, lb), lbComp < 0);
          }
          if (incUB) {
            assertTrue(String.format("Value %f was greater than non-strict upper bound %f", actualDouble, ub), ubComp >= 0);
          } else {
            assertTrue(String.format("Value %f was greater than or equal to strict upper bound %f", actualDouble, ub), ubComp > 0);
          }
          return;
        }
        
      } else if (expected instanceof Number) {
        Double expectedDouble = ((Number) expected).doubleValue();
        assertNotNull(String.format("Output %s was not a Number.", actual), actualDouble);
        assertTrue(String.format("Output %f was not expected value %f", actualDouble, expectedDouble), Utils.valuesEqual(expectedDouble, actualDouble));
        return;
      
      } else if (expected instanceof String && Utils.isNumber((String) expected)) {
        Double expectedDouble = Utils.toDouble((String) expected);
        assertNotNull(String.format("Output %s was not a Number.", actual), actualDouble);
        assertTrue(String.format("Output %f was not expected value %f", actualDouble, expectedDouble), Utils.valuesEqual(expectedDouble, actualDouble));
        return;
        
      }
      
      //if control gets here, no special code was possible, do a generic assertEquals
      assertEquals(expected, actual);
    }
    
//    /**
//     * Specialized checking to compare two solutions more intelligently
//     * In particular, objects are compared irrespective of key order,
//     * and values in the expected object can be specified by ranges.
//     * So, the actual output
//     * {@code {"foo": 4}}
//     * would match an expected object like
//     * {@code {"foo": "[2, 5)"}}
//     * @param expected The JSONObject that is the expected (gold standard) solution
//     * @param actual The JSONObject that is the actual output
//     */
//    protected static void assertSolutionJson(JSONObject expected, JSONObject actual, boolean allowExcess) {
//      for (Object key : expected.keySet()) {
//        if (!(key instanceof String)) continue; // no idea how this could fire, but we'll be safe anyways
//        String keyStr = (String)key;
//        assertTrue("Output is missing key " + keyStr, actual.has(keyStr));
//        assertSolutionJson(expected.get(keyStr), actual.get(keyStr), allowExcess);
//      }
//      if (!allowExcess) {
//        // do check for excess keys:
//        for (Object key : actual.keySet()) {
//          if (!(key instanceof String)) continue; // no idea how this could fire, but we'll be safe anyways
//          String keyStr = (String)key;
//          assertTrue("Output has excess key " + keyStr, expected.has(keyStr));
//        }
//      }
//    }
//
//    /**
//     * Specialized checking to compare two solutions more intelligently
//     * In particular, arrays are compared element-by-element, using
//     * the {@link #assertSolutionJson} methods.
//     * @param expected The JSONArray that is the expected (gold standard) solution
//     * @param actual The JSONArray that is the actual output
//     */
//    protected static void assertSolutionJson(JSONArray expected, JSONArray actual, boolean allowExcess) {
//      assertEquals(
//          "Output array " + actual.toString() + " has size " + actual.length() + ", but expected size " + expected.length(),
//          expected.length(), actual.length());
//      for (int i = 0; i < expected.length(); ++i) {
//        assertSolutionJson(expected.get(i), actual.get(i), allowExcess);
//      }
//    }
}
