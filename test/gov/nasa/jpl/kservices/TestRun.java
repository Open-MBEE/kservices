package gov.nasa.jpl.kservices;

//import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
//import gov.nasa.jpl.mbee.util.FileUtils;
import org.junit.Test;
//import java.io.*;

import static org.junit.Assert.*;

public class TestRun {
    @Test
    public void test() {
        String inputFile = "src/kTestCases/abs.k";
        String[] input = new String[]{"--captureOff", "--solve", "--package", "abs", inputFile};
        KtoJava.main(input);
    }
}
