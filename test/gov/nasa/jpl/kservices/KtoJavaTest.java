package gov.nasa.jpl.kservices;

import org.junit.Test;
import java.io.*;

public class KtoJavaTest {

    @Test
    public void testBatteryDemo() throws FileNotFoundException {
        Tester.checkSolution("batteryDemo");
    }

    @Test
    public void testSimple() throws FileNotFoundException {
        Tester.checkSolution("simple");
    }

    @Test
    public void testSimpleString() throws FileNotFoundException {
        Tester.checkSolution("simpleString");
    }
    
    @Test
    public void testStringConcat() throws FileNotFoundException {
        Tester.checkSolution("stringConcat");
    }

    @Test
    public void testStringFieldInConstructor() throws FileNotFoundException {
        Tester.checkSolution("stringFieldInConstructor");
    }

    @Test
    public void testAbs() throws FileNotFoundException {
        Tester.checkSolution("abs");
    }

    @Test
    public void testFactorial() throws FileNotFoundException {
        Tester.checkSolution( "factorial" );
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
    public void testMod() throws FileNotFoundException {
        Tester.checkSolution("mod");
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

    @Test
    public void testRandomVar() throws FileNotFoundException {
        Tester.checkSolution("randomVar");
    }

    @Test
    public void testRandomIfThenElse() throws FileNotFoundException {
        Tester.checkSolution("randomIfThenElse");
    }

    @Test
    public void testBias() throws FileNotFoundException {
        Tester.checkSolution("bias");
    }

    @Test
    public void testTvmGetSetValue() throws FileNotFoundException {
        Tester.checkSolution("tvmGetSetValue");
    }

    @Test
    public void testDistributionInClass() throws FileNotFoundException {
        Tester.checkSolution("distributionInClass");
    }

    @Test
    public void testGetValueOffTimeline() throws FileNotFoundException {
        Tester.checkSolution("getValueOffTimeline");
    }

    // Not working -- both true and false effects are retained; need to deconstruct from if-then-else maybe via subtractReference
    // JIRA CAEK-7
    //    @Test
    //    public void testConditionalEffect() throws FileNotFoundException {
    //        Tester.checkSolution("conditionalEffect");
    //    }

    // Not working -- y.z.setValue(ep2, "Hello") never gets set; c.inDomain() fails for var c: C
    //    @Test
    //    public void testEffectOnMember() throws FileNotFoundException {
    //        Tester.checkSolution("effectOnMember");
    //    }

    // Not working -- could be a GetMember problem
    //    @Test
    //    public void testElaboration() throws FileNotFoundException {
    //        Tester.checkSolution("elaboration");
    //    }

    // Not working -- evaluating over set of Ints returned false but should be true.
    //    @Test
    //    public void testForall() throws FileNotFoundException {
    //        Tester.checkSolution("forall");
    //    }



}
