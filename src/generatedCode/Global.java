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
import gov.nasa.jpl.ae.event.ParameterListenerImpl;
import gov.nasa.jpl.ae.event.Event;
import gov.nasa.jpl.ae.solver.ObjectDomain;
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

    public class LevelOne extends ParameterListenerImpl {

        public LevelOne() {
            super();
            initLevelOneMembers();
            initLevelOneCollections();
            initLevelOneDependencies();
        }

        public class LevelTwo extends ParameterListenerImpl {

            public LevelTwo() {
                super();
                initLevelTwoMembers();
                initLevelTwoCollections();
                initLevelTwoDependencies();
            }

            public class LevelThree extends ParameterListenerImpl {

                public LevelThree() {
                    super();
                    initLevelThreeMembers();
                    initLevelThreeCollections();
                    initLevelThreeDependencies();
                }

                public IntegerParameter x3 = null;

                public BooleanParameter y = null;

                public void initLevelThreeMembers() {
                    try {
                        if (x3 == null) x3 = new IntegerParameter("x3", (Integer) null, this);
                        if (y == null) y = new BooleanParameter("y", (Boolean) null, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                protected void initLevelThreeCollections() {
                    parameters.add(x3);
                    parameters.add(y);
                }

                public void initLevelThreeDependencies() {
                }
            }

            public class LevelThreeB extends ParameterListenerImpl {

                public LevelThreeB() {
                    super();
                    initLevelThreeBMembers();
                    initLevelThreeBCollections();
                    initLevelThreeBDependencies();
                }

                public Parameter< Global.LevelOne.LevelTwo > back = null;

                public void initLevelThreeBMembers() {
                    try {
                        if (back == null) back = new Parameter<Global.LevelOne.LevelTwo>("back", new ObjectDomain<LevelTwo>(LevelTwo.class, LevelOne.this), (Global.LevelOne.LevelTwo) (new ConstructorCall(null, ClassUtils.getConstructorForArgTypes(LevelTwo.class), new Object[] {}, (Class<?>) null)).evaluate(true), this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                protected void initLevelThreeBCollections() {
                    parameters.add(back);
                }

                public void initLevelThreeBDependencies() {
                }
            }

            public IntegerParameter x2 = null;

            public Parameter< Global.LevelOne.LevelTwo.LevelThree > nl2 = null;

            public void initLevelTwoMembers() {
                try {
                    if (x2 == null) x2 = new IntegerParameter("x2", (Integer) null, this);
                    if (nl2 == null) nl2 = new Parameter<Global.LevelOne.LevelTwo.LevelThree>("nl2", new ObjectDomain<LevelThree>(LevelThree.class, LevelTwo.this), (Global.LevelOne.LevelTwo.LevelThree) (new ConstructorCall(null, ClassUtils.getConstructorForArgTypes(LevelThree.class), new Object[] {}, (Class<?>) null)).evaluate(true), this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            protected void initLevelTwoCollections() {
                parameters.add(x2);
                parameters.add(nl2);
            }

            public void initLevelTwoDependencies() {
            }
        }

        public IntegerParameter x1 = null;

        public Parameter< Global.LevelOne.LevelTwo > nl1 = null;

        public void initLevelOneMembers() {
            try {
                if (x1 == null) x1 = new IntegerParameter("x1", (Integer) null, this);
                if (nl1 == null) nl1 = new Parameter<Global.LevelOne.LevelTwo>("nl1", new ObjectDomain<LevelTwo>(LevelTwo.class, LevelOne.this), (Global.LevelOne.LevelTwo) (new ConstructorCall(null, ClassUtils.getConstructorForArgTypes(LevelTwo.class), new Object[] {}, (Class<?>) null)).evaluate(true), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void initLevelOneCollections() {
            parameters.add(x1);
            parameters.add(nl1);
        }

        public void initLevelOneDependencies() {
        }
    }

    public Parameter< Global.LevelOne > AutoGeneratedLevelOne = null;

    public void initGlobalMembers() {
        try {
            if (AutoGeneratedLevelOne == null) AutoGeneratedLevelOne = new Parameter<Global.LevelOne>("AutoGeneratedLevelOne", new ObjectDomain<LevelOne>(LevelOne.class, Global.this), (Global.LevelOne) null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initGlobalCollections() {
        parameters.add(AutoGeneratedLevelOne);
    }

    public void initGlobalDependencies() {
    }
}
