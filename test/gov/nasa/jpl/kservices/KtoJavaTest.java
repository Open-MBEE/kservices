package gov.nasa.jpl.kservices;

import gov.nasa.jpl.mbee.util.FileUtils;
import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

public class KtoJavaTest {



    @Test
    public void testSimple() throws FileNotFoundException {
        String[] simpleK = new String[]{ "src/kTestCases/simple.k"};
        KtoJava.main(simpleK);
        String outputSolnPath = "src/generatedCode/solution.log";
        String expectedSolnPath = "test/gov/nasa/jpl/kservices/kTestCaseSolutions/simpleSolution";
        File outputSolnFile = new File( outputSolnPath );
        File expectedSolnFile = new File(expectedSolnPath);
        String outputSolution  = FileUtils.fileToString( outputSolnFile );
        String expectedSolution = FileUtils.fileToString( expectedSolnFile);
        assertEquals(expectedSolution, outputSolution);

    }





}