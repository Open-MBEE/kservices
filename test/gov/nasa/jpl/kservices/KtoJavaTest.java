package gov.nasa.jpl.kservices;

import org.junit.Test;
import java.io.*;

public class KtoJavaTest {

    @Test
    public void testSimple() throws FileNotFoundException {
        Tester.checkSolution("simple");
    }

    @Test
    public void testSimpleString() throws FileNotFoundException {
        Tester.checkSolution("simpleString");
    }

    @Test
    public void testAbs() throws FileNotFoundException {
        Tester.checkSolution("abs");
    }

    @Test
    public void testFactorial() throws FileNotFoundException {
        Tester.checkSolution("factorial");
    }

    @Test
    public void testBatteryDemo() throws FileNotFoundException {
        Tester.checkSolution("batteryDemo");
    }

    @Test
    public void testFlattenFunction() throws FileNotFoundException {
        Tester.checkSolution("flatten");
    }

    @Test
    public void testAbsFunction() throws FileNotFoundException {
        Tester.checkSolution("absFunction");
    }

    @Test
    public void testComments() throws FileNotFoundException {
        Tester.checkSolution("comments");
    }

    @Test
    public void testUnsatisfied() throws FileNotFoundException {
        Tester.checkSolution("unsatisfied");
    }

    @Test
    public void testAbstractFunction() throws FileNotFoundException {
        Tester.checkSolution("abstractFunction");
    }

}