package gov.nasa.jpl.kservices;

import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

public class KtoJavaTest {



    @Test
    public void testSimple() {
        String[] simpleK = new String[]{ "src/kTestCases/simple.k"};
        KtoJava.main(simpleK);
        String path = "src/generatedCode/writeJavaOut.log";
        File f = new File( path );

    }



}