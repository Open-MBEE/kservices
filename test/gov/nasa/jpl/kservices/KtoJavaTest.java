package test.gov.nasa.jpl.kservices;

import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.FileUtils;
import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

public class KtoJavaTest {



    @Test
    public void testSuite() throws FileNotFoundException {
        String[] testNames = new String[]{"simple", "simpleString", "abs", "factorial"};
        for (String testName : testNames) {
            checkSolution(testName);
        }

    }

    public void checkSolution(String kFileName) throws  FileNotFoundException {
        String testCaseDir = "test/gov/nasa/jpl/kservices/kTestCases/";
        String testSolnDir = "test/gov/nasa/jpl/kservices/kTestCaseSolutions/";
        String inputFile = testCaseDir + kFileName + ".k";
        String[] input = new String[]{inputFile};
        KtoJava.main(input);
        String outputSolnPath = "src/generatedCode/solution.log";
        String expectedSolnPath = testSolnDir + kFileName + "Solution";
        File outputSolnFile = new File( outputSolnPath );
        File expectedSolnFile = new File(expectedSolnPath);
        String outputSolution  = FileUtils.fileToString( outputSolnFile );
        String expectedSolution = FileUtils.fileToString( expectedSolnFile);
        assertEquals(expectedSolution, outputSolution);



    }





}