package gov.nasa.jpl.pma;

import k.frontend.Model;
import k.frontend.PackageDecl;
import k.frontend.AnnotationDecl;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import gov.nasa.jpl.ae.event.ConstructorCall;
import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.ae.util.JavaToConstraintExpression;
import gov.nasa.jpl.mbee.util.Debug;
import k.frontend.Annotation;
import k.frontend.QualifiedName;
import k.frontend.ImportDecl;
import k.frontend.EntityDecl;
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
import k.frontend.ForExp;
import k.frontend.BinExp;
import k.frontend.BinaryOp;
import k.frontend.UnaryExp;
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

public class KToAe {

    enum KAstClass {
                      Model, PackageDecl, AnnotationDecl, Annotation,
                      QualifiedName, ImportDecl, EntityDecl,
                      IdentifierToken, TypeParam, TypeBound, TypeDecl,
                      PropertyDecl, FunSpec, Param, FunDecl,
                      ConstraintDecl, ExpressionDecl, ParenExp, IdentExp,
                      DotExp, FunApplExp, IfExp, MatchExp,
                      MatchCase, BlockExp, WhileExp, ForExp, BinExp, UnaryExp,
                      QuantifiedExp, TupleExp,
                      CollectionEnumExp, CollectionRangeExp, CollectionComprExp,
                      LambdaExp, AssertExp, TypeCastCheckExp,
                      ReturnExp, PositionalArgument, NamedArgument,
                      IntegerLiteral, RealLiteral, CharacterLiteral,
                      StringLiteral, BooleanLiteral, CollectType, SumType,
                      ClassType, IdentType, CartesianType,
                      FunctionType, ParenType, SubType, LiteralPattern,
                      IdentPattern, ProductPattern, TypedPattern,
                      RngBinding, ExpCollection, TypeCollection, Multiplicity
    };
    
    enum KBinaryOp {
                    LT, LTE, GT, GTE, AND, OR, IMPL, IFF, EQ, NEQ, MUL, DIV,
                    REM, SETINTER, SETDIFF, LISTCONCAT,
                    TUPLEINDEX, ADD, SUB, SETUNION, ISIN, NOTISIN, SUBSET,
                    PSUBSET, ASSIGN
    };

    public KToAe() {
    }
    
    public Object astToAeExpr( Object kExpr, String type,
                               boolean convertFcnCallArgsToExprs,
                               boolean lookOutsideClassDataForTypes,
                               boolean complainIfDeclNotFound,
                               boolean evaluateCall,
                               StringBuffer error ) {
        if ( kExpr == null ) return null;
        KAstClass cls = null;
        try {
            cls = KAstClass.valueOf( kExpr.getClass().getSimpleName() );
        } catch (IllegalArgumentException e ) { }
        if ( cls == null ) return null;
        
        Class< ? > returnType = null; // This would be used to disambiguate. It
                                      // could be passed in.
        
        gov.nasa.jpl.ae.event.Expression<?> aeExpr = null;

        switch ( cls ) {
            case Model:
            case PackageDecl:
            case AnnotationDecl:
            case Annotation:
            case QualifiedName:
            case ImportDecl:
            case EntityDecl:
            case IdentifierToken:
            case TypeParam:
            case TypeBound:
            case TypeDecl:
            case PropertyDecl:
            case FunSpec:
            case Param:
            case FunDecl:
            case ConstraintDecl:
            case ExpressionDecl:
            case ParenExp:
            case IdentExp:
            case DotExp:
            case FunApplExp:
            case IfExp:
            case MatchExp:
            case MatchCase:
            case BlockExp:
            case WhileExp:
            case ForExp:
            case BinExp:
                BinExp binExp = (BinExp)kExpr;
                aeExpr = kBinExpToAe( binExp, type, convertFcnCallArgsToExprs, lookOutsideClassDataForTypes,
                                      complainIfDeclNotFound, evaluateCall, error );
                break;
            case UnaryExp:
            case QuantifiedExp:
            case TupleExp:
            case CollectionEnumExp:
            case CollectionRangeExp:
            case CollectionComprExp:
            case LambdaExp:
            case AssertExp:
            case TypeCastCheckExp:
            case ReturnExp:
            case PositionalArgument:
            case NamedArgument:
                aeExpr = new Expression<String>( ((NamedArgument)kExpr).ident() );
                break;
            case IntegerLiteral:
                aeExpr = new Expression<Integer>( ((IntegerLiteral)kExpr).i() );
                break;
            case RealLiteral:
                aeExpr = new Expression<Double>( ((RealLiteral)kExpr).f().doubleValue() );
                break;
            case CharacterLiteral:
            case StringLiteral:
                aeExpr = new Expression<String>( ((StringLiteral)kExpr).s() );
                break;
            case BooleanLiteral:
                aeExpr = new Expression<Boolean>( ((BooleanLiteral)kExpr).b() );
                break;
            case CollectType:
            case SumType:
            case ClassType:
            case IdentType:
//                aeExpr = new Expression<String>( ((IdentType)kExpr).ident().toPath() );
//                break;
            case CartesianType:
            case FunctionType:
            case ParenType:
            case SubType:
            case LiteralPattern:
            case IdentPattern:
            case ProductPattern:
            case TypedPattern:
            case RngBinding:
            case ExpCollection:
            case TypeCollection:
            case Multiplicity:
                break;
            default:
                break;
        }
        return aeExpr;
    }

    protected void addErrorMessage( StringBuffer error, String msg ) {
        if ( error != null ) {
            if ( error.length() > 0 ) {
                error.append(", ");
            }
            error.append( msg );
        }
    }
    
    public Expression<?> kBinExpToAe(BinExp binExp, String type,
                                     boolean convertFcnCallArgsToExprs,
                                     boolean lookOutsideClassDataForTypes,
                                     boolean complainIfDeclNotFound,
                                     boolean evaluateCall,
                                     StringBuffer error ) {
        if ( binExp == null ) return null;
        Expression<?> aeExpr = null;
        Class< ? > returnType = null;
        BinaryOp op = binExp.op();
        if ( op == null ) {
            addErrorMessage( error,
                             "Binary expression " + binExp + " has no operator!" );
            return aeExpr;
        }
        ConstructorCall call =
                JavaToConstraintExpression.binaryOpNameToEventFunction( op.getClass().getSimpleName(),
                                                                        returnType );
        Debug.errorOnNull( true, "A Functions class must exist for every Java binary operator", call );
        Vector< Object > args = new Vector< Object >();
        
        args.add( astToAeExpr( binExp.exp1(), null, true, lookOutsideClassDataForTypes, complainIfDeclNotFound,
                               evaluateCall, error ) );
        args.add( astToAeExpr( binExp.exp2(), null, true, lookOutsideClassDataForTypes, complainIfDeclNotFound,
                               evaluateCall, error ) );
        call.setArguments( args );
        if ( evaluateCall ) {
            try {
                aeExpr = new Expression< Object >( call.evaluate( true ) );
            } catch ( IllegalAccessException e ) {
                addErrorMessage( error, "IllegalAccessException evaluating " +
                                 binExp + e.getLocalizedMessage() );
            } catch ( InvocationTargetException e ) {
                addErrorMessage( error, "InvocationTargetException evaluating " +
                                 binExp + e.getLocalizedMessage() );
            } catch ( InstantiationException e ) {
                addErrorMessage( error, "InstantiationException evaluating " +
                                 binExp + e.getLocalizedMessage() );
            }
        } else {
          aeExpr = new Expression<Object>( call );
        }
        return aeExpr;
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
    

}
