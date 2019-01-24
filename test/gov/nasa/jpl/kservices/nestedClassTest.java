package gov.nasa.jpl.kservices;

import org.junit.Test;
import java.io.*;

public class nestedClassTest {

    @Test
    public void testParallelSameClassName() throws FileNotFoundException {
        Tester.checkSolution("nestedClass/parallelSameClassName");
    }

    // The test below uses same-named nested classes, which are not currently
    // supported because Java does not support them.
//    @Test
//    public void testSeriesSameClassName() throws FileNotFoundException {
//        Tester.checkSolution("nestedClass/seriesSameClassName");
//    }
}
