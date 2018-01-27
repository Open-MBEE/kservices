package gov.nasa.jpl.kservices.k2apgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Constraint {

    String name = null;
    String type = "forbidden_condition";
    String condition = null;
    String message = "failed assertion";
    String severity = "error";

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("constraint " + name + ": " + type);
        sb.append("\n");
        sb.append("    begin\n");

        sb.append("        condition\n");
        sb.append(Util.indent(condition + ";", 12));
        sb.append("\n");

        sb.append("        message\n");
        sb.append(Util.indent("\"" + message + "\";", 12));
        sb.append("\n");

        sb.append("        severity\n");
        sb.append(Util.indent(severity + ";", 12));
        sb.append("\n");
        sb.append("\n    end constraint " + name + "\n");

        return sb.toString();
    }
}
