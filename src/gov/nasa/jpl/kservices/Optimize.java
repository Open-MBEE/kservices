package gov.nasa.jpl.kservices;

import gov.nasa.jpl.mbee.util.Pair;
import k.frontend.ConstraintDecl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

enum OptimizeMode {
    MIN, MAX
}

enum VarType {
    REAL, INT, BOOL
}

public class Optimize {
    //public static HashMap<String, HashSet<ConstraintDecl>> constraintMap;
    public static ArrayList<String> constraintList = new ArrayList<>();
    public static ArrayList<Pair<String, VarType>> variableList = new ArrayList<>();

    public static final String LP_IN = "/tmp/lp_in.txt";
    public static final String LP_OUT = "/tmp/lp_out.txt";
    public static final String CALL_GLPK = "./src/gov/nasa/jpl/kservices/CallGLPK/callGLPK";

    public static Double maximize(String varName) throws IOException {
        return optimize(varName, OptimizeMode.MAX);
    }

    public static Double minimize(String varName) throws IOException {
        return optimize(varName, OptimizeMode.MIN);
    }

    private static Double optimize(String varName, OptimizeMode mode) throws IOException {
        PrintWriter out = new PrintWriter(LP_IN, "UTF-8");
        out.println(mode == OptimizeMode.MAX ? "maximize" : "minimize");
        out.println("    " + varName);
        out.println("subject to");
        for(String constraint : constraintList) {
            out.println("    " + constraint);
        }

        ArrayList<String> integerVars = new ArrayList<>();
        ArrayList<String> binaryVars = new ArrayList<>();

        out.println("bounds");
        for(Pair<String, VarType> var : variableList) {
            out.println("    " + var.first + " free");

            if(var.second == VarType.INT) {
                integerVars.add(var.first);
            } else if(var.second == VarType.BOOL) {
                binaryVars.add(var.first);
            }

        }

        if(integerVars.size() > 0) {
            out.println("integer");
            for(String integer : integerVars) {
                out.println("    " + integer);
            }
        }

        if(binaryVars.size() > 0) {
            out.println("binary");
            for(String binary : binaryVars) {
                out.println("    " + binary);
            }
        }

        out.println("end");
        out.close();

        try {
            Process p = Runtime.getRuntime().exec(CALL_GLPK + " " + LP_IN + " " + LP_OUT);
            p.waitFor();
            if(p.exitValue() != 0) {
                System.err.println("call to GLPK returned with exit value " + p.exitValue());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Double result = 0.0;

        BufferedReader br = new BufferedReader(new FileReader(LP_OUT));
        String line;
        while ((line = br.readLine()) != null) {
            if(line.startsWith("objective value")) {
                result = Double.parseDouble(line.substring(18)); //after "objective value = "
            }
            System.out.println(line);
        }

        return result;
    }

}
