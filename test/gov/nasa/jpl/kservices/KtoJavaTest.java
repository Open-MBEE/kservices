package gov.nasa.jpl.kservices;

import gov.nasa.jpl.mbee.util.FileUtils;
import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

public class KtoJavaTest {



    @Test
    public void testSimple() throws FileNotFoundException {
        checkSolution("simple");
    }

    @Test
    public void testSimpleString() throws FileNotFoundException {
        checkSolution("simpleString");
    }

    @Test
    public void testAbs() throws FileNotFoundException {
        checkSolution("abs");
    }

    @Test
    public void testFactorial() throws FileNotFoundException {
        checkSolution("factorial");
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