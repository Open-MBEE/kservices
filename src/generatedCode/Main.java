package generatedCode;

import gov.nasa.jpl.ae.event.Parameter;
import gov.nasa.jpl.ae.event.IntegerParameter;
import gov.nasa.jpl.ae.event.LongParameter;
import gov.nasa.jpl.ae.event.DoubleParameter;
import gov.nasa.jpl.ae.event.StringParameter;
import gov.nasa.jpl.ae.event.BooleanParameter;
import gov.nasa.jpl.ae.event.StateVariable;
import gov.nasa.jpl.ae.event.Timepoint;
import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.ae.event.ConstraintExpression;
import gov.nasa.jpl.ae.event.Functions;
import gov.nasa.jpl.ae.event.FunctionCall;
import gov.nasa.jpl.ae.event.ConstructorCall;
import gov.nasa.jpl.ae.event.Call;
import gov.nasa.jpl.ae.event.Effect;
import gov.nasa.jpl.ae.event.EffectFunction;
import gov.nasa.jpl.ae.event.TimeDependentConstraintExpression;
import gov.nasa.jpl.ae.event.Dependency;
import gov.nasa.jpl.ae.event.ElaborationRule;
import gov.nasa.jpl.ae.event.EventInvocation;
import gov.nasa.jpl.ae.event.DurativeEvent;
import gov.nasa.jpl.ae.event.Event;
import gov.nasa.jpl.mbee.util.Utils;
import gov.nasa.jpl.mbee.util.Debug;
import gov.nasa.jpl.mbee.util.ClassUtils;
import java.util.Vector;
import java.util.Map;

public class Main extends Global {

    public Main() {
        super();
    }

    public static void main(String[] args) {
        Main scenario = new Main();
        scenario.satisfy(true, null);
        System.out.println((scenario.isSatisfied(true, null) ? "Satisfied" : "Not Satisfied") + "\n" + scenario.executionString());
    }
}
