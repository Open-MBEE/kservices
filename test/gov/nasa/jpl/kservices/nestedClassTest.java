package gov.nasa.jpl.kservices;

import org.junit.Test;
import java.io.*;

public class nestedClassTest {

    @Test
    public void testParallelSameClassName() throws FileNotFoundException {
        Tester.checkSolution("nestedClass/parallelSameClassName");
    }

    @Test
    public void testSeriesSameClassName() throws FileNotFoundException {
        Tester.checkSolution("nestedClass/seriesSameClassName");
    }
}
