package gov.nasa.jpl.kservices;

import k.frontend.Model;
import k.frontend.PackageDecl;
import k.frontend.AnnotationDecl;
import k.frontend.Argument;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;

import gov.nasa.jpl.mbee.util.Debug;

//import gov.nasa.jpl.kservices.scala.AeKUtil;

import gov.nasa.jpl.ae.solver.*;
import gov.nasa.jpl.ae.sysml.SystemModelSolver;
import gov.nasa.jpl.ae.sysml.TranslatedCall;
import gov.nasa.jpl.ae.sysml.TranslatedConstructorCall;
import gov.nasa.jpl.ae.sysml.TranslatedFunctionCall;
import gov.nasa.jpl.ae.event.*;
import gov.nasa.jpl.ae.event.Functions.Binary;
import gov.nasa.jpl.ae.event.Functions.Unary;
import gov.nasa.jpl.ae.fuml.*;

import gov.nasa.jpl.ae.util.JavaToConstraintExpression;
import k.frontend.ADD;
import k.frontend.Annotation;
import k.frontend.QualifiedName;
import k.frontend.ImportDecl;
import k.frontend.EntityDecl;
import k.frontend.Exp;
import k.frontend.IdentifierToken;
import k.frontend.TypeParam;
import k.frontend.TypeBound;
import k.frontend.TypeDecl;
import k.frontend.PropertyDecl;
import k.frontend.FunSpec;
import k.frontend.Param;
import k.frontend.FunDecl;
import k.frontend.ConstraintDecl;
import k.frontend.ExpressionDecl;
import k.frontend.ParenExp;
import k.frontend.IdentExp;
import k.frontend.DotExp;
import k.frontend.FunApplExp;
import k.frontend.IfExp;
import k.frontend.MatchExp;
import k.frontend.MatchCase;
import k.frontend.BlockExp;
import k.frontend.WhileExp;
import scala.collection.immutable.List;
import k.frontend.ForExp;
import k.frontend.Frontend;
import k.frontend.BinExp;
import k.frontend.BinaryOp;
import k.frontend.UnaryExp;
import k.frontend.UnaryOp;
import k.frontend.QuantifiedExp;
import k.frontend.TupleExp;
import k.frontend.CollectionEnumExp;
import k.frontend.CollectionRangeExp;
import k.frontend.CollectionComprExp;
import k.frontend.LambdaExp;
import k.frontend.AssertExp;
import k.frontend.TypeCastCheckExp;
import k.frontend.ReturnExp;
import k.frontend.PositionalArgument;
import k.frontend.NamedArgument;
import k.frontend.IntegerLiteral;
import k.frontend.RealLiteral;
import k.frontend.CharacterLiteral;
import k.frontend.StringLiteral;
import k.frontend.BooleanLiteral;
import k.frontend.CollectType;
import k.frontend.SumType;
import k.frontend.ClassType;
import k.frontend.IdentType;
import k.frontend.CartesianType;
import k.frontend.FunctionType;
import k.frontend.ParenType;
import k.frontend.SubType;
import k.frontend.LiteralPattern;
import k.frontend.IdentPattern;
import k.frontend.ProductPattern;
import k.frontend.TypedPattern;
import k.frontend.RngBinding;
import k.frontend.ExpCollection;
import k.frontend.TypeCollection;
import k.frontend.Multiplicity;

public class AeToK {

    enum AeAstClass {
        //Affectable,

        Call,
        ConstructorCall,
        FunctionCall,
        TranslatedCall,
        TranslatedConstructorCall,
        TranslatedFunctionCall,
        Command,
        //Functions,
        
        Domain,
        AbstractFiniteRangeDomain,
        AbstractRangeDomain,
        BooleanDomain,
        DoubleDomain,
        IntegerDomain,
        ObjectDomain,
        ClassDomain,
        RangeDomain,
        SingleValueDomain,
        StringDomain,
        TimeDomain,

        //Node,
        //Edge,

        //Effect,
        EffectFunction,
        EffectInstance,
        ElaborationRule,
        EventInvocation,
        
        Expression,
        Query,
        HowManyQuery,
        RelationQuery,

        
        //Variable,
        Parameter,
        BooleanParameter,
        DoubleParameter,
        Duration,
        IntegerParameter,
        StringParameter,
        Timepoint,
        TimeVariable,

        //ParameterListener,
        ParameterListenerImpl,
        Event,
        DurativeEvent,

        
        TimeVarying,
        Consumable,
        LinearTimeline,
        NestedTimeVaryingMap,
        ObjectFlow,
        TimeVaryingList,
        TimeVaryingMap,
        TimeVaryingMaps,
        TimeVaryingPlottableMap,
        TimeVaryingPlottableMaps,
        TimeVaryingProjection,


        Constraint,
        //ParameterConstraint,
        ConstraintExpression,
        ConstraintNetwork,
        Dependency,
        TimeDependentConstraint,
        TimeDependentConstraintExpression,

        //Solver,
        //Uncollection,
        //CollectionTree,
        //SysmlCall,
        
        SystemModelSolver
    };
    
    public enum AeOp {
        Add,
        And,
        Binary,
        BooleanBinary,
        Conditional,
        Divide,
        DoesThereExist,
        EQ,
        Equals,
        Exists,
        ForAll,
        GT,
        GTE,
        Greater,
        GreaterEquals,
        LT,
        LTE,
        Less,
        LessEquals,
        Minus,
        NEQ,
        Negative,
        Not,
        NotEquals,
        Or,
        Plus,
//        Prev,
        Sub,
        Sum,
        ThereExists,
        Times,
        Xor
    }
//    enum KAstClass {
//                      Model, PackageDecl, AnnotationDecl, Annotation,
//                      QualifiedName, ImportDecl, EntityDecl,
//                      IdentifierToken, TypeParam, TypeBound, TypeDecl,
//                      PropertyDecl, FunSpec, Param, FunDecl,
//                      ConstraintDecl, ExpressionDecl, ParenExp, IdentExp,
//                      DotExp, FunApplExp, IfExp, MatchExp,
//                      MatchCase, BlockExp, WhileExp, ForExp, BinExp, UnaryExp,
//                      QuantifiedExp, TupleExp,
//                      CollectionEnumExp, CollectionRangeExp, CollectionComprExp,
//                      LambdaExp, AssertExp, TypeCastCheckExp,
//                      ReturnExp, PositionalArgument, NamedArgument,
//                      IntegerLiteral, RealLiteral, CharacterLiteral,
//                      StringLiteral, BooleanLiteral, CollectType, SumType,
//                      ClassType, IdentType, CartesianType,
//                      FunctionType, ParenType, SubType, LiteralPattern,
//                      IdentPattern, ProductPattern, TypedPattern,
//                      RngBinding, ExpCollection, TypeCollection, Multiplicity
//    };
//    
//    enum KBinaryOp {
//                    LT, LTE, GT, GTE, AND, OR, IMPL, IFF, EQ, NEQ, MUL, DIV,
//                    REM, SETINTER, SETDIFF, LISTCONCAT,
//                    TUPLEINDEX, ADD, SUB, SETUNION, ISIN, NOTISIN, SUBSET,
//                    PSUBSET, ASSIGN
//    };

    public AeToK() {
    }
    
    
    public static Class<?> aeType( Object aeObject ) {
        if ( aeObject == null ) return null;
        Class<?> cls = aeClass( aeObject.getClass() );
        return cls;
    }
    public static Class<?> aeClass( Class<?> objClass ) {
        if ( objClass == null ) return null;

        if ( Call.class.isAssignableFrom( objClass ) ) {
            return Call.class;
        }
        if ( ConstructorCall.class.isAssignableFrom( objClass ) ) {
            return ConstructorCall.class;
        }
        if ( FunctionCall.class.isAssignableFrom( objClass ) ) {
            return FunctionCall.class;
        }
        if ( TranslatedCall.class.isAssignableFrom( objClass ) ) {
            return TranslatedCall.class;
        }
        if ( TranslatedConstructorCall.class.isAssignableFrom( objClass ) ) {
            return TranslatedConstructorCall.class;
        }
        if ( TranslatedFunctionCall.class.isAssignableFrom( objClass ) ) {
            return TranslatedFunctionCall.class;
        }
        if ( Command.class.isAssignableFrom( objClass ) ) {
            return Command.class;
        }

        if ( Domain.class.isAssignableFrom( objClass ) ) {
            return Domain.class;
        }
        if ( AbstractFiniteRangeDomain.class.isAssignableFrom( objClass ) ) {
            return AbstractFiniteRangeDomain.class;
        }
        if ( AbstractRangeDomain.class.isAssignableFrom( objClass ) ) {
            return AbstractRangeDomain.class;
        }
        if ( BooleanDomain.class.isAssignableFrom( objClass ) ) {
            return BooleanDomain.class;
        }
        if ( DoubleDomain.class.isAssignableFrom( objClass ) ) {
            return DoubleDomain.class;
        }
        if ( IntegerDomain.class.isAssignableFrom( objClass ) ) {
            return IntegerDomain.class;
        }
        if ( ObjectDomain.class.isAssignableFrom( objClass ) ) {
            return ObjectDomain.class;
        }
        if ( ClassDomain.class.isAssignableFrom( objClass ) ) {
            return ClassDomain.class;
        }
        if ( RangeDomain.class.isAssignableFrom( objClass ) ) {
            return RangeDomain.class;
        }
        if ( SingleValueDomain.class.isAssignableFrom( objClass ) ) {
            return SingleValueDomain.class;
        }
        if ( StringDomain.class.isAssignableFrom( objClass ) ) {
            return StringDomain.class;
        }
        if ( TimeDomain.class.isAssignableFrom( objClass ) ) {
            return TimeDomain.class;
        }

        if ( EffectFunction.class.isAssignableFrom( objClass ) ) {
            return EffectFunction.class;
        }
        if ( EffectInstance.class.isAssignableFrom( objClass ) ) {
            return EffectInstance.class;
        }
        if ( ElaborationRule.class.isAssignableFrom( objClass ) ) {
            return ElaborationRule.class;
        }
        if ( EventInvocation.class.isAssignableFrom( objClass ) ) {
            return EventInvocation.class;
        }

        if ( Expression.class.isAssignableFrom( objClass ) ) {
            return Expression.class;
        }
//        if ( Query.class.isAssignableFrom( objClass ) ) { NEED TO MAKE QUERY A PUBLIC CLASS
//            return Query.class;
//        }
//        if ( HowManyQuery.class.isAssignableFrom( objClass ) ) {
//            return HowManyQuery.class;
//        }
        if ( RelationQuery.class.isAssignableFrom( objClass ) ) {
            return RelationQuery.class;
        }

        if ( Variable.class.isAssignableFrom( objClass ) ) {
            return Variable.class;
        }
        if ( Parameter.class.isAssignableFrom( objClass ) ) {
            return Parameter.class;
        }
        if ( BooleanParameter.class.isAssignableFrom( objClass ) ) {
            return BooleanParameter.class;
        }
        if ( DoubleParameter.class.isAssignableFrom( objClass ) ) {
            return DoubleParameter.class;
        }
        if ( Duration.class.isAssignableFrom( objClass ) ) {
            return Duration.class;
        }
        if ( IntegerParameter.class.isAssignableFrom( objClass ) ) {
            return IntegerParameter.class;
        }
        if ( StringParameter.class.isAssignableFrom( objClass ) ) {
            return StringParameter.class;
        }
        if ( Timepoint.class.isAssignableFrom( objClass ) ) {
            return Timepoint.class;
        }
        if ( TimeVariable.class.isAssignableFrom( objClass ) ) {
            return TimeVariable.class;
        }

        if ( ParameterListener.class.isAssignableFrom( objClass ) ) {
            return ParameterListener.class;
        }
        if ( ParameterListenerImpl.class.isAssignableFrom( objClass ) ) {
            return ParameterListenerImpl.class;
        }
        if ( Event.class.isAssignableFrom( objClass ) ) {
            return Event.class;
        }
        if ( DurativeEvent.class.isAssignableFrom( objClass ) ) {
            return DurativeEvent.class;
        }

        if ( TimeVarying.class.isAssignableFrom( objClass ) ) {
            return TimeVarying.class;
        }
        // TODO -- REVIEW -- These will always be false since they are
        // subclasses of TimeVarying, for which the above would always return.
        // Consider moving the check for TimeVarying after its subclasses.
        if ( Consumable.class.isAssignableFrom( objClass ) ) {
            return Consumable.class;
        }
        if ( LinearTimeline.class.isAssignableFrom( objClass ) ) {
            return LinearTimeline.class;
        }
        if ( NestedTimeVaryingMap.class.isAssignableFrom( objClass ) ) {
            return NestedTimeVaryingMap.class;
        }
        if ( ObjectFlow.class.isAssignableFrom( objClass ) ) {
            return ObjectFlow.class;
        }
        if ( TimeVaryingList.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingList.class;
        }
        if ( TimeVaryingMap.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingMap.class;
        }
        if ( TimeVaryingMaps.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingMaps.class;
        }
        if ( TimeVaryingPlottableMap.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingPlottableMap.class;
        }
        if ( TimeVaryingPlottableMaps.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingPlottableMaps.class;
        }
        if ( TimeVaryingProjection.class.isAssignableFrom( objClass ) ) {
            return TimeVaryingProjection.class;
        }

        if ( Constraint.class.isAssignableFrom( objClass ) ) {
            return Constraint.class;
        }
        if ( ConstraintExpression.class.isAssignableFrom( objClass ) ) {
            return ConstraintExpression.class;
        }
        if ( ConstraintNetwork.class.isAssignableFrom( objClass ) ) {
            return ConstraintNetwork.class;
        }
        if ( Dependency.class.isAssignableFrom( objClass ) ) {
            return Dependency.class;
        }
        if ( TimeDependentConstraint.class.isAssignableFrom( objClass ) ) {
            return TimeDependentConstraint.class;
        }
        if ( TimeDependentConstraintExpression.class.isAssignableFrom( objClass ) ) {
            return TimeDependentConstraintExpression.class;
        }

        if ( SystemModelSolver.class.isAssignableFrom( objClass ) ) {
            return SystemModelSolver.class;
        }

       return null;
    }
    
    public Exp aeToKExpr( Object aeObject, String type,
                           // boolean convertFcnCallArgsToExprs,
                           // boolean lookOutsideClassDataForTypes,
                           boolean complainIfDeclNotFound,
                           // boolean evaluateCall,
                           StringBuffer error ) {
        if ( aeObject == null ) return null;
        AeToK.AeAstClass cls = null;

        try {
            cls = AeAstClass.valueOf( aeObject.getClass().getSimpleName() );
        } catch ( IllegalArgumentException e ) {
            if ( aeObject instanceof FunctionCall ) {
                cls = AeAstClass.FunctionCall;
            }
        }
        if ( cls == null ) return null;

        Class< ? > returnType = null; // This would be used to disambiguate. It
                                      // could be passed in.

        String text = aeObject.toString();
        
        Exp aeExpr = Frontend.exp2KExp( text );
        
        if ( aeExpr == null ) {
        	aeExpr = aeToKExpr2(aeObject, type, complainIfDeclNotFound, error);
        }
        
        return aeExpr;
    }

    // TODO -- REVIEW -- It might be easier to write to K and parse instead of
    // building the K AST.
    public Exp aeToKExpr2( Object aeObject, String type,
//                              boolean convertFcnCallArgsToExprs,
//                              boolean lookOutsideClassDataForTypes,
                             boolean complainIfDeclNotFound,
//                              boolean evaluateCall,
                              StringBuffer error ) {
        if ( aeObject == null ) return null;
        AeToK.AeAstClass cls = null;
        
        try {
            cls = AeAstClass.valueOf( aeObject.getClass().getSimpleName() );
        } catch (IllegalArgumentException e ) { 
            if ( aeObject instanceof FunctionCall ) {
                cls = AeAstClass.FunctionCall;
            }
        }
        if ( cls == null ) return null;
        
        Class< ? > returnType = null; // This would be used to disambiguate. It
                                      // could be passed in.

        Exp aeExpr = null;

        switch ( cls ) {
            case AbstractFiniteRangeDomain:
                break;
            case AbstractRangeDomain:
                break;
            case BooleanDomain:
                break;
            case BooleanParameter:
                break;
            case Call:
                break;
            case Command:
                break;
            case Constraint:
                break;
            case ConstraintExpression:
                break;
            case ConstraintNetwork:
                break;
            case ConstructorCall:
                break;
            case Consumable:
                break;
            case Dependency:
                break;
            case Domain:
                break;
            case DoubleDomain:
                break;
            case DoubleParameter:
                break;
            case Duration:
                break;
            case DurativeEvent:
                break;
            case EffectFunction:
                break;
            case EffectInstance:
                break;
            case ElaborationRule:
                break;
            case Event:
                break;
            case EventInvocation:
                break;
            case Expression:
                break;
            case FunctionCall:
                try {
                    ArrayList<Argument> kArgs = new ArrayList< Argument >();
                    FunctionCall fc = (FunctionCall)aeObject;
                    String functionName = fc.getName();
                    Exp fNameExp = aeToKExpr(functionName, null, complainIfDeclNotFound, error);
                    for ( Object arg : fc.getArguments() ) {
                        Exp e = aeToKExpr(arg, null, complainIfDeclNotFound, error);
                        kArgs.add( new PositionalArgument(e) );
                    }
                    if ( fc instanceof Unary && fc.getArguments().size() == 1 ) {
                        UnaryOp op = aeToKUnaryOp(fc);
                    } else if ( fc instanceof Binary && fc.getArguments().size() == 2 ) {
                        BinaryOp op = aeToKBinaryOp(fc);
                        aeExpr = new BinExp( kArgs.get( 0 ), op, kArgs.get( 1 ) );
                    } else {
                    	//Exp arg0 = kArgs.isEmpty() ? null : kArgs.get( 0 );
                    	//kArgs.remove(0);
                    	//aeExpr = AeKUtil.makeFunApplExp(fNameExp, kArgs);
//                    	List<Argument> list = AeKUtil.arrayListToList(kArgs);
//                    	aeExpr = new FunApplExp(fNameExp, list);
                    }
                } catch (ClassCastException e) {
                    // TODO -- ERROR
                }
                break;
            case HowManyQuery:
                break;
            case IntegerDomain:
                break;
            case IntegerParameter:
                break;
            case LinearTimeline:
                break;
            case NestedTimeVaryingMap:
                break;
            case ObjectDomain:
                break;
            case ClassDomain:
                break;
            case ObjectFlow:
                break;
            case Parameter:
                break;
            case ParameterListenerImpl:
                break;
            case Query:
                break;
            case RangeDomain:
                break;
            case RelationQuery:
                break;
            case SingleValueDomain:
                break;
            case StringDomain:
                break;
            case StringParameter:
                break;
            case SystemModelSolver:
                break;
            case TimeDependentConstraint:
                break;
            case TimeDependentConstraintExpression:
                break;
            case TimeDomain:
                break;
            case Timepoint:
                break;
            case TimeVariable:
                break;
            case TimeVarying:
                break;
            case TimeVaryingList:
                break;
            case TimeVaryingMap:
                break;
            case TimeVaryingMaps:
                break;
            case TimeVaryingPlottableMap:
                break;
            case TimeVaryingPlottableMaps:
                break;
            case TimeVaryingProjection:
                break;
            case TranslatedCall:
                break;
            case TranslatedConstructorCall:
                break;
            case TranslatedFunctionCall:
                break;
            default:
                break;
        }
        return aeExpr;
    }

    public BinaryOp aeToKBinaryOp( FunctionCall fc ) {
        if ( fc == null ) return null;
        Class<?> cls = fc.getClass();
        return aeToKBinaryOp( cls );
    }
    public BinaryOp aeToKBinaryOp( Class<?> cls ) {
        if ( cls == null ) return null;
        BinaryOp op = null;
        AeOp aeOp = null;
        try {
            aeOp = AeOp.valueOf( cls.getSimpleName() );
        } catch (IllegalArgumentException e ) {
            return null;
        }
        //AeKUtil u = new AeKUtil();//nil;//gov.nasa.jpl.kservices.AeKUtil();
        //op = AeKUtil.aeToKBinaryOp(aeOp);
        if ( op == null ) {
            // TODO -- ERROR?
        }
        return op;
    }


    public UnaryOp aeToKUnaryOp( FunctionCall fc ) {
        if ( fc == null ) return null;
        Class<?> cls = fc.getClass();
        return aeToKUnaryOp( cls );
    }
    public UnaryOp aeToKUnaryOp( Class<?> cls ) {
        if ( cls == null ) return null;
        UnaryOp op = null;
        AeOp aeOp = null;
        try {
            aeOp = AeOp.valueOf( cls.getSimpleName() );
        } catch (IllegalArgumentException e ) {
            return null;
        }
        //op = AeKUtil.aeToKUnaryOp( aeOp );
        return op;
    }

    
    protected void addErrorMessage( StringBuffer error, String msg ) {
        if ( error != null ) {
            if ( error.length() > 0 ) {
                error.append(", ");
            }
            error.append( msg );
        }
    }
    
    
    void foo() {
        
//        case LT:
//        case LTE:
//        case GT:
//        case GTE:
//        case AND:
//        case OR:
//        case IMPL:
//        case IFF:
//        case EQ:
//        case NEQ:
//        case MUL:
//        case DIV:
//        case REM:
//        case SETINTER:
//        case SETDIFF:
//        case LISTCONCAT:
//        case TUPLEINDEX:
//        case ADD:
//        case SUB:
//        case SETUNION:
//        case ISIN:
//        case NOTISIN:
//        case SUBSET:
//        case PSUBSET:
//        case ASSIGN:

    }
    
    public static void main( String[] args ) {
        System.out.println("Hello, Earth.");
    }
    
}
