package gov.nasa.jpl.kservices;

import org.junit.Test;

import static org.junit.Assert.*;

public class KtoJavaTest {

    @Test
    public void testSimple() {
        String[] simpleK = new String[]{ "--captureOff src/kTestCases/simple.k"};
        KtoJava.main(simpleK);

    }



}