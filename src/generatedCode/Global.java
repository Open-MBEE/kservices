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

public class Global extends DurativeEvent {

    public Global() {
        super();
        initGlobalMembers();
        initGlobalCollections();
        initGlobalDependencies();
    }

    public Expression<Double> merpAbs(double x) throws Exception {
        return new Expression<Double>(new Functions.Conditional(new Expression<Boolean>(new Functions.Less(new Expression(x), new Expression<Integer>(0))), new Expression(new Functions.Negative(new Expression(x))), new Expression(x)));
    }

    public DoubleParameter y = null;

    public Dependency< Double > yDependency = null;

    public void initGlobalMembers() {
        try {
            if (y == null) y = new DoubleParameter("y", (Double) (new FunctionCall(this, ClassUtils.getMethodForArgTypes("Global", "generatedCode", "merpAbs", double.class), new Object[] { -1E0 }, (Class<?>) null)).evaluate(true), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initGlobalCollections() {
        parameters.add(y);
    }

    public void initGlobalDependencies() {
        addDependency(y, new Expression(new FunctionCall(this, ClassUtils.getMethodForArgTypes("Global", "generatedCode", "merpAbs", double.class), new Object[] { new Expression<Double>(new Functions.Negative(new Expression<Double>(1E0))) }, (Class<?>) null)));
    }
}
