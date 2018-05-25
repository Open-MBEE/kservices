package gov.nasa.jpl.kservices;

import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.FileUtils;
import gov.nasa.jpl.mbee.util.Random;
import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

public class KtoJavaTest {


    @Test
    public void testSimple() throws FileNotFoundException {
        Random.reset();
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

    @Test
    public void testBatteryDemo() throws FileNotFoundException {
        checkSolution("batteryDemo");
    }

    @Test
    public void testFlattenFunction() throws FileNotFoundException {
        checkSolution("flatten");
    }

    @Test
    public void testAbsFunction() throws FileNotFoundException {
        checkSolution("absFunction");
    }

    public void checkSolution(String kFileName) throws  FileNotFoundException {
        Random.reset();
        char fs = File.separatorChar;
        String testCaseDir = "test" + fs + "gov" + fs + "nasa" + fs + "jpl" + fs + "kservices" + fs + "kTestCases" + fs;
        String otherTestCaseDir = "src" + fs + "kTestCases" + fs;
        String testSolnDir = "test" + fs + "gov" + fs + "nasa" + fs + "jpl" + fs + "kservices" + fs + "kTestCaseSolutions" + fs;
        String inputFile = testCaseDir + kFileName + ".k";
        File f = new File(inputFile);
        if ( !f.exists() ) {
            inputFile = otherTestCaseDir + kFileName + ".k";
            f = new File(inputFile);
        }
        assertTrue( f.exists() );
        String[] input = new String[]{inputFile};
        KtoJava kToJava = KtoJava.runMain(input);
        String outputSolnPath = EventXmlToJava.generatedCodeLocation + fs + "generatedCode" + fs + "solution.log";
        String expectedSolnPath = testSolnDir + kFileName + "Solution";
        File outputSolnFile = new File( outputSolnPath );
        File expectedSolnFile = new File(expectedSolnPath);
        String outputSolution  = FileUtils.fileToString( outputSolnFile );
        String expectedSolution = FileUtils.fileToString( expectedSolnFile);
        assertEquals(expectedSolution, outputSolution);
    }


}