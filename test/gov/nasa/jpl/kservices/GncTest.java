package gov.nasa.jpl.kservices;

import org.junit.Test;

import java.io.FileNotFoundException;

public class GncTest {

    @Test
    public void testGnc() throws FileNotFoundException {
        Tester.checkSolution("gnc");
    }
}
