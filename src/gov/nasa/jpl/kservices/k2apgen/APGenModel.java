package gov.nasa.jpl.kservices.k2apgen;

import java.util.LinkedHashMap;
import java.util.Map;

public class APGenModel {
    Map<String, Activity> activities = new LinkedHashMap<String, Activity>();
    Map<String, Resource> resources = new LinkedHashMap<String, Resource>();
    Map<String, Function> functions = new LinkedHashMap<String, Function>();
    //Map<String, Constraint> resources = new LinkedHashMap<String, Constraint>();
    Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    Map<String, Parameter> instanceParameters = new LinkedHashMap<String, Parameter>();
}
