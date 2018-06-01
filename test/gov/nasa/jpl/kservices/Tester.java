package gov.nasa.jpl.kservices;

import gov.nasa.jpl.ae.event.ParameterListenerImpl;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.mbee.util.FileUtils;
import gov.nasa.jpl.mbee.util.Random;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tester {
    public static final char FS = File.separatorChar;
    public static final String TEST_CASE_DIR = "test" + FS + "gov" + FS + "nasa" + FS + "jpl" + FS + "kservices" + FS + "kTestCases" + FS;
    public static final String TEST_SOLN_DIR = "test" + FS + "gov" + FS + "nasa" + FS + "jpl" + FS + "kservices" + FS + "kTestCaseSolutions" + FS;

    public static void checkSolution(String kFileName) throws FileNotFoundException {
        ParameterListenerImpl.reset();
        Random.reset();

        String inputFile = TEST_CASE_DIR + kFileName + ".k";
        File f = new File(inputFile);
        assertTrue( f.exists() );

        String[] input = new String[]{inputFile};
        KtoJava.main(input);

        String outputSolnPath = EventXmlToJava.generatedCodeLocation + FS + "generatedCode" + FS + "solution.log";
        String expectedSolnPath = TEST_SOLN_DIR + kFileName + "Solution";
        File outputSolnFile = new File( outputSolnPath );
        File expectedSolnFile = new File(expectedSolnPath);
        String outputSolution  = FileUtils.fileToString( outputSolnFile );
        String expectedSolution = FileUtils.fileToString( expectedSolnFile);

        assertEquals(expectedSolution, outputSolution);
    }
}
