package gov.nasa.jpl.kservices;

import japa.parser.ASTHelper;
import japa.parser.ASTParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.Type;
// import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;

import scala.collection.JavaConversions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
// import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Keep these for resolving class references.
import gov.nasa.jpl.ae.event.*;
import gov.nasa.jpl.ae.util.ClassData;
import gov.nasa.jpl.ae.util.JavaForFunctionCall;
import gov.nasa.jpl.ae.util.JavaToConstraintExpression;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.ae.xml.XmlUtils;
import gov.nasa.jpl.mbee.util.Pair;
import gov.nasa.jpl.ae.fuml.*;
import gov.nasa.jpl.mbee.util.ClassUtils;
import gov.nasa.jpl.mbee.util.CompareUtils;
import gov.nasa.jpl.mbee.util.Debug;
import gov.nasa.jpl.mbee.util.FileUtils;
import gov.nasa.jpl.mbee.util.NameTranslator;
import gov.nasa.jpl.mbee.util.TimeUtils;
import gov.nasa.jpl.mbee.util.Timer;
import gov.nasa.jpl.mbee.util.Utils;
import demandResponse.*;

import gov.nasa.jpl.mbee.util.Random;

import k.frontend.Frontend;
import k.frontend.Model;
import k.frontend.PackageDecl;
import k.frontend.AnnotationDecl;
import k.frontend.Argument;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;

import gov.nasa.jpl.mbee.util.Debug;

// import gov.nasa.jpl.kservices.scala.AeKUtil;

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
import k.frontend.Frontend;
import k.frontend.TypeChecker;

/*
 * Translates XML to executable Java classes for Analysis Engine behavior
 */

public class KtoJava {

    String k;
    String packageName;
    JavaToConstraintExpression expressionTranslator;
    int counter;
    TypeChecker typeChecker;
    Model model;
    Boolean isExpression;
    final String globalName;

    public KtoJava( String k, String pkgName, boolean translate ) {
        this.globalName = "Global";
        this.k = k;
        if ( pkgName != null && !pkgName.equals( "" ) ) {
            this.packageName = pkgName;
        }
        // Debug.turnOn();
        init();
        if ( translate ) {
            translateClasses();
        }
    }

    public KtoJava( String k, String pkgName ) {
        this.globalName = "Global";
        this.k = k;
        if ( pkgName != null && !pkgName.equals( "" ) ) {
            this.packageName = pkgName;
        }
        init();
        if ( this.isExpression ) {
            translateExpression();
        } else {
            translateClasses();
        }
        

    }

    public void init() {
        this.counter = 0;
        expressionTranslator = new JavaToConstraintExpression( packageName );
        this.model = Frontend.getModelFromString( this.k );
        typeChecker = new TypeChecker( this.model );
        this.isExpression = Frontend.isExpression( this.model );
        buildParamTable( getClassData().getParamTable() );

        buildMethodTable( getClassData().getMethodTable() );

    }

    public void
           buildParamTable( Map< String, Map< String, ClassData.Param > > paramTable ) {
        Map< String, ClassData.Param > params =
                new TreeMap< String, ClassData.Param >();
        ClassData.Param param;
        addGlobalParams( paramTable );
        ArrayList< EntityDecl > entityList =
                new ArrayList< EntityDecl >( JavaConversions.asJavaCollection( Frontend.getEntitiesFromModel( this.model ) ) );
        for ( EntityDecl entity : entityList ) {
            String entityName = getClassName( entity );
            params = new TreeMap< String, ClassData.Param >();
            ArrayList< PropertyDecl > propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getAllPropertyDecls() ) );
            for ( PropertyDecl p : propertyList ) {
                param = makeParam( p );
                params.put( p.name(), param );
            }
            ArrayList< FunDecl > funList =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( entity.getAllFunDecls() ) );
            for ( FunDecl funDecl : funList ) {
                List< Param > funParams =
                        new ArrayList< Param >( JavaConversions.asJavaCollection( funDecl.params() ) );
                for ( Param p : funParams ) {
                    param = new ClassData.Param( p.name(),
                                                 JavaToConstraintExpression.typeToClass( p.ty()
                                                                                          .toString() ),
                                                 null );
                    params.put( p.name(), param );
                }

            }
            paramTable.put( entityName, params );

        }

    }

    public void
           addGlobalParams( Map< String, Map< String, ClassData.Param > > paramTable ) {
        ClassData.Param param;
        Map< String, ClassData.Param > params =
                new TreeMap< String, ClassData.Param >();
        List< PropertyDecl > topLevelProperties =
                new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( this.model ) ) );
        List< FunDecl > topLevelFunctions =
                new ArrayList< FunDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelFunctions( this.model ) ) );
        for ( PropertyDecl p : topLevelProperties ) {
            param = makeParam( p );
            params.put( p.name(), param );
        }
        for ( FunDecl funDecl : topLevelFunctions ) {
            List< Param > funParams =
                    new ArrayList< Param >( JavaConversions.asJavaCollection( funDecl.params() ) );
            for ( Param p : funParams ) {
                param = new ClassData.Param( p.name(),
                                             JavaToConstraintExpression.typeToClass( p.ty()
                                                                                      .toString() ),
                                             null );
                params.put( p.name(), param );
            }
        }
        paramTable.put( globalName, params );

    }

    public void
           buildMethodTable( Map< String, Map< String, Set< MethodDeclaration > > > methodTable ) {
        addGlobalMethods( methodTable );
        ArrayList< EntityDecl > entityList =
                new ArrayList< EntityDecl >( JavaConversions.asJavaCollection( Frontend.getEntitiesFromModel( this.model ) ) );
        for ( EntityDecl entity : entityList ) {
            String entityName = getClassName( entity );

            Map< String, Set< MethodDeclaration > > classMethods =
                    methodTable.get( entityName );

            if ( classMethods == null ) {
                classMethods =
                        new TreeMap< String, Set< MethodDeclaration > >();
                methodTable.put( entityName, classMethods );
            }
            Collection< MethodDeclaration > methodCollection =
                    getMethods( entity );

            for ( MethodDeclaration methodDecl : methodCollection ) {
                Set< MethodDeclaration > methodSet =
                        classMethods.get( methodDecl.getName() );
                if ( methodSet == null ) {
                    methodSet =
                            new TreeSet< MethodDeclaration >( new CompareUtils.GenericComparator< MethodDeclaration >() );
                    classMethods.put( methodDecl.getName(), methodSet );
                }
                methodSet.add( methodDecl );
            }

        }
    }

    public void
           addGlobalMethods( Map< String, Map< String, Set< MethodDeclaration > > > methodTable ) {
        List< FunDecl > topLevelFunctions =
                new ArrayList< FunDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelFunctions( this.model ) ) );
        Map< String, Set< MethodDeclaration > > classMethods =
                new TreeMap< String, Set< MethodDeclaration > >();
        methodTable.put( globalName, classMethods );
        Collection< MethodDeclaration > methodCollection = getMethods( null );
        for ( MethodDeclaration methodDecl : methodCollection ) {
            Set< MethodDeclaration > methodSet =
                    classMethods.get( methodDecl.getName() );
            if ( methodSet == null ) {
                methodSet =
                        new TreeSet< MethodDeclaration >( new CompareUtils.GenericComparator< MethodDeclaration >() );
                classMethods.put( methodDecl.getName(), methodSet );
            }
            methodSet.add( methodDecl );
        }

    }

    public Collection< MethodDeclaration > getMethods( EntityDecl entity ) {
        ArrayList< MethodDeclaration > methodDeclarations =
                new ArrayList< MethodDeclaration >();
        ArrayList< FunDecl > functions;
        if ( entity != null ) {
            functions =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( entity.getFunDecls() ) );
        } else {
            functions =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelFunctions( this.model ) ) );
        }

        for ( FunDecl funDecl : functions ) {
            MethodDeclaration methodDecl = makeMethodDecl( funDecl );
            if ( methodDecl != null ) {
                methodDeclarations.add( methodDecl );
            }
        }
        return methodDeclarations;

    }

    public MethodDeclaration makeMethodDecl( FunDecl funDecl ) {
        MethodDeclaration methodDecl = new MethodDeclaration();
        String typeString = funDecl.ty().get().toString();
        methodDecl.setType( makeType( typeString ) );
        methodDecl.setModifiers( 1 );
        methodDecl.setName( funDecl.ident() );
        List< Param > funParams =
                new ArrayList< Param >( JavaConversions.asJavaCollection( funDecl.params() ) );
        List< japa.parser.ast.body.Parameter > params =
                new ArrayList< japa.parser.ast.body.Parameter >();
        japa.parser.ast.body.Parameter param;
        for ( Param p : funParams ) {
            param = ASTHelper.createParameter( makeType( p.ty().toString() ),
                                               p.name() );
            params.add( param );
        }
        methodDecl.setParameters( params );
        if ( funDecl.body().isEmpty() ) { // in the case where a method is
            // declared but not defined, not sure
            // if this is the best thing to do
            return null;
        }
        methodDecl.setThrows( Arrays.asList( new NameExpr( "Exception" ) ) );

        return methodDecl;
    }

    public Type makeType( String typeString ) {
        Type type;
        switch ( typeString ) {
            case "Int":
                type = new PrimitiveType( PrimitiveType.Primitive.Int );
                break;
            case "Bool":
                type = new PrimitiveType( PrimitiveType.Primitive.Boolean );
                break;
            case "Real":
                type = new PrimitiveType( PrimitiveType.Primitive.Double );
                break;
            default:
                type = new ClassOrInterfaceType( typeString );
        }
        return type;

    }

    public ClassData.Param makeParam( PropertyDecl p ) {
        String name = p.name();
        String type =
                JavaToConstraintExpression.typeToClass( p.ty().toString() );
        String value;
        if ( p.expr().isEmpty() ) {
            value = "null";
            if ( !( type.equals( "Boolean" ) || type.equals( "Double" )
                    || type.equals( "Integer" ) || type.equals( "Long" )
                    || type.equals( "String" ) ) ) {
                value = "new " + type + "()";
            }
        } else {
            value = p.expr().get().toJavaString();
        }
        return new ClassData.Param( name, type, value );

    }

    public void translateExpression() {
        getClassData().setCurrentClass( "Main" );
        initClassCompilationUnit( getClassData().getCurrentClass() );

        ClassOrInterfaceDeclaration newClassDecl =
                new ClassOrInterfaceDeclaration( ModifierSet.PUBLIC, false,
                                                 getClassData().getCurrentClass() );
        ASTHelper.addTypeDeclaration( getClassData().getCurrentCompilationUnit(),
                                      newClassDecl );

        int mods = ModifierSet.PUBLIC | ModifierSet.STATIC;

        MethodDeclaration mainMethodDecl =
                new MethodDeclaration( mods, new VoidType(), "main" );
        BlockStmt mainBody = new BlockStmt();
        mainMethodDecl.setBody( mainBody );

        ConstructorDeclaration ctor =
                new ConstructorDeclaration( ModifierSet.PUBLIC,
                                            newClassDecl.getName() );
        ASTHelper.addMember( newClassDecl, ctor );
        BlockStmt ctorBody = new BlockStmt();
        ctor.setBlock( ctorBody );

        Type type = ASTHelper.createReferenceType( "String", 1 );
        VariableDeclaratorId id = new VariableDeclaratorId( "args" );
        japa.parser.ast.body.Parameter parameter =
                new japa.parser.ast.body.Parameter( type, id );

        ASTHelper.addParameter( mainMethodDecl, parameter );
        ASTHelper.addMember( newClassDecl, mainMethodDecl );

        StringBuffer stmtsMain = new StringBuffer();

        List< Expression > args = new ArrayList< Expression >();
        Expression expr = expressionTranslator.parseExpression( this.k );
        String aeString = expressionTranslator.astToAeExpr( expr, null, true,
                                                            true, true, true );
        stmtsMain.append( "Object value = " + aeString + ";" );
        stmtsMain.append( "System.out.println( value );" );
        ASTHelper.addStmt( ctorBody,
                           new ExplicitConstructorInvocationStmt( false, null,
                                                                  args ) );

        addImport( "gov.nasa.jpl.ae.event.Expression" );

        addStatements( mainBody, stmtsMain.toString() );

        String tryCatchString = "try{\n" + ";\n" + "} catch ( Exception e ) {\n"
                                + "  // TODO Auto-generated catch block\n"
                                + "  e.printStackTrace();\n" + "}\n";

        List< Statement > stmts = new ArrayList< Statement >();
        if ( Debug.isOn() ) Debug.outln( "trying to parse \"" + stmts + "\"" );

        TryStmt tryStmt = null;

        ASTParser parser = new ASTParser( new StringReader( tryCatchString ) );
        try {
            tryStmt = parser.TryStatement();
        } catch ( ParseException e ) {
            e.printStackTrace();
            return;
        }
        tryStmt.setTryBlock( mainBody );
        stmts.add( tryStmt );
        BlockStmt newBody = new BlockStmt( stmts );
        mainMethodDecl.setBody( newBody );

    }

    public void translateClasses() {
        // TODO
        processClassDeclarations( true );
        processClassDeclarations( false );
        processExecutionEvent();
    }

    /**
     * @return the expressionTranslator
     */
    public JavaToConstraintExpression getExpressionTranslator() {
        return expressionTranslator;
    }

    public ClassData getClassData() {
        JavaToConstraintExpression t = getExpressionTranslator();
        if ( Debug.errorOnNull( "Trying to get classData from null translator!",
                                t ) ) {
            return null;
        }
        return t.getClassData();
    }

    private void processClassDeclarations( boolean justClassDeclarations ) {
        ClassOrInterfaceDeclaration classDecl = null;
        ArrayList< EntityDecl > entityList =
                new ArrayList< EntityDecl >( JavaConversions.asJavaCollection( Frontend.getEntitiesFromModel( this.model ) ) );
        ClassOrInterfaceDeclaration globalClassDecl;
        if ( justClassDeclarations ) {
            getClassData().setCurrentCompilationUnit( initClassCompilationUnit( globalName ) );
            globalClassDecl =
                    processClassDeclaration( null, justClassDeclarations );
            ASTHelper.addTypeDeclaration( getClassData().getCurrentCompilationUnit(),
                                          globalClassDecl );
        } else {
            globalClassDecl =
                    processClassDeclaration( null, justClassDeclarations );
        }

        for ( EntityDecl entity : entityList ) {
            getClassData().getNestedToEnclosingClassNames().put( getClassName( entity ),
                                                                 globalName );

            classDecl =
                    processClassDeclaration( entity, justClassDeclarations );
            if ( justClassDeclarations ) {
                ASTHelper.addMember( globalClassDecl, classDecl );
            }
        }

    }

    public String getClassName( EntityDecl entity ) {
        String className = globalName;
        if ( entity != null ) {
            className += "." + entity.ident();
        }
        return className;
    }

    public ClassOrInterfaceDeclaration
           processClassDeclaration( EntityDecl entity,
                                    boolean justClassDeclarations ) {
        String currentClass = getClassName( entity );

        getClassData().setCurrentClass( currentClass );

        ClassOrInterfaceDeclaration newClassDecl = null;

        if ( justClassDeclarations ) {
            newClassDecl =
                    new ClassOrInterfaceDeclaration( ModifierSet.PUBLIC, false,
                                                     ClassUtils.simpleName( currentClass ) );

            getSuperClasses( entity, newClassDecl );
            createDefaultConstructor( newClassDecl );
        } else {
            // getClassData().setCurrentCompilationUnit(
            // getClassData().getClasses()
            // .get( currentClass ) );
            newClassDecl = getClassData().getClassDeclaration( currentClass ); // need
                                                                               // to
                                                                               // fix
            setMethodBodies( entity );
            createMembers( newClassDecl, entity );
        }

        return newClassDecl;

    }

    public void setMethodBodies( EntityDecl entity ) {
        List< FunDecl > funDecls;
        Map< String, Set< MethodDeclaration > > classMethods;
        if ( entity == null ) {
            classMethods = getClassData().getMethodTable().get( globalName );
            funDecls =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelFunctions( this.model ) ) );
        } else {
            String entityName = getClassName( entity );
            classMethods = getClassData().getMethodTable().get( entityName );
            funDecls =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( entity.getFunDecls() ) );
        }

        for ( FunDecl funDecl : funDecls ) {
            Set< MethodDeclaration > methodSet =
                    classMethods.get( funDecl.ident() );
            if ( methodSet != null ) {
                for ( MethodDeclaration methodDecl : methodSet ) {
                    BlockStmt body = new BlockStmt();
                    String typeString = funDecl.ty().get().toString();
                    if ( !funDecl.body().isEmpty() ) {
                        Expression expr =
                                expressionTranslator.parseExpression( ( (ExpressionDecl)funDecl.body()
                                                                                               .apply( 0 ) ).exp()
                                                                                                            .toJavaString() );
                        String aeString =
                                expressionTranslator.astToAeExpr( expr,
                                                                  typeString,
                                                                  true, true,
                                                                  true, true );
                        addStatements( body, "return " + aeString
                                             + ".getValue( true );" );

                        methodDecl.setBody( body );
                    }
                }
            }

        }

    }

    protected void createDefaultConstructor( TypeDeclaration newClassDecl ) {
        ConstructorDeclaration ctor =
                new ConstructorDeclaration( ModifierSet.PUBLIC,
                                            newClassDecl.getName() );
        ASTHelper.addMember( newClassDecl, ctor );
        BlockStmt block = new BlockStmt();
        ASTHelper.addStmt( block, new ExplicitConstructorInvocationStmt() );
        ctor.setBlock( block );
        ASTHelper.addStmt( block,
                           new MethodCallExpr( null,
                                               "init" + newClassDecl.getName()
                                                     + "Members" ) );
        ASTHelper.addStmt( block,
                           new MethodCallExpr( null,
                                               "init" + newClassDecl.getName()
                                                     + "Collections" ) );
        ASTHelper.addStmt( block,
                           new MethodCallExpr( null,
                                               "init" + newClassDecl.getName()
                                                     + "Dependencies" ) );

    }

    protected void getSuperClasses( EntityDecl entity,
                                    ClassOrInterfaceDeclaration newClassDecl ) {
        if ( entity == null ) {
            addExtends( newClassDecl, "DurativeEvent" );
            return;
        }
        List< ClassOrInterfaceType > extendsList = getInheritsFrom( entity );
        if ( !Utils.isNullOrEmpty( extendsList ) ) {
            newClassDecl.setExtends( extendsList );
        }
        if ( Utils.isNullOrEmpty( newClassDecl.getExtends() ) ) {
            addExtends( newClassDecl, "DurativeEvent" );
        }
    }

    public List< ClassOrInterfaceType > getInheritsFrom( EntityDecl entity ) {

        // Need to figure out how to do multiple extends TODO
        // List< String > extendsStringList =
        // new ArrayList< String >( JavaConversions.asJavaCollection(
        // TypeChecker.getSuperClasses( entity.ident() ) ) );
        // List< ClassOrInterfaceType > extendsList =
        // new ArrayList< ClassOrInterfaceType >();
        // for ( String e : extendsStringList ) {
        // ClassOrInterfaceType c = new ClassOrInterfaceType( e );
        // extendsList.add( c );
        // }
        // return extendsList;
        // only do one for now
        List< k.frontend.Type > types =
                new ArrayList< k.frontend.Type >( JavaConversions.asJavaCollection( entity.extending() ) );
        if ( types.isEmpty() ) {
            return new ArrayList< ClassOrInterfaceType >();
        }
        k.frontend.Type type =
                new ArrayList< k.frontend.Type >( JavaConversions.asJavaCollection( entity.extending() ) ).get( 0 );
        return Arrays.asList( new ClassOrInterfaceType( type.toString() ) );
    }

    //
    // protected void getImports( Node clsNode ) {
    // List< String > imports =
    // XmlUtils.getChildrenElementText( clsNode, "import" );
    // for ( String imp : imports ) {
    // addImport( imp );
    // }
    // }
    // TODO

    public Set< MethodDeclaration > getMethodsForClass( String className ) {
        Map< String, Set< MethodDeclaration > > classMethods =
                getClassData().getMethodTable().get( className );
        if ( classMethods == null ) return ClassData.emptyMethodDeclarationSet;
        Set< MethodDeclaration > methodsForClass =
                new TreeSet< MethodDeclaration >( new CompareUtils.GenericComparator< MethodDeclaration >() );
        for ( Set< MethodDeclaration > methodsByName : classMethods.values() ) {
            methodsForClass.addAll( methodsByName );
        }
        return methodsForClass;
    }

    protected void createMembers( TypeDeclaration newClassDecl,
                                  EntityDecl entity ) {

        Collection< MethodDeclaration > methods =
                getMethodsForClass( getClassData().getCurrentClass() );
        for ( MethodDeclaration methodDecl : methods ) {

            ASTHelper.addMember( newClassDecl, methodDecl );
        }

        MethodDeclaration initMembers =
                createPublicVoidMethod( "init" + newClassDecl.getName()
                                        + "Members" );

        MethodDeclaration initElaborations =
                createPublicVoidMethod( "init" + newClassDecl.getName()
                                        + "Elaborations" );

        MethodDeclaration initDependencies =
                createPublicVoidMethod( "init" + newClassDecl.getName()
                                        + "Dependencies" );
        addStatements( initElaborations.getBody(),
                       "init" + newClassDecl.getName() + "Dependencies();" );

        List< FieldDeclaration > members = new ArrayList< FieldDeclaration >();
        Collection< FieldDeclaration > parameters =
                getParameters( entity, initMembers );
        Collection< FieldDeclaration > constraints =
                getConstraints( entity, initMembers );

        Collection< FieldDeclaration > dependencies =
                getDependencies( entity, initDependencies );

        members.addAll( parameters );
        members.addAll( constraints );
        members.addAll( dependencies );
        addTryCatchToInitMembers( initMembers );

        MethodDeclaration initCollections =
                createInitCollectionsMethod( "init" + newClassDecl.getName()
                                             + "Collections", parameters, constraints );

        // Add fields and methods to class declaration.
        for ( FieldDeclaration f : members ) {
            ASTHelper.addMember( newClassDecl, f );
        }
        ASTHelper.addMember( newClassDecl, initMembers );
        ASTHelper.addMember( newClassDecl, initCollections );
        ASTHelper.addMember( newClassDecl, initDependencies );

    }

    public ArrayList< FieldDeclaration >
           getDependencies( EntityDecl entity,
                            MethodDeclaration initDependencies ) {
        ArrayList< FieldDeclaration > dependencies =
                new ArrayList< FieldDeclaration >();
        ArrayList< PropertyDecl > propertyList;
        ArrayList< ConstraintDecl > constraintList;
        if ( entity == null ) {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( this.model ) ) );
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelConstraints( this.model ) ) );
        } else {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getPropertyDecls() ) );
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( entity.getConstraintDecls() ) );
        }
        for ( PropertyDecl property : propertyList ) {
            ClassData.Param depParam = makeParam( property );
            if ( depParam.value != "null" ) {
                FieldDeclaration f =
                        createDependencyField( depParam, initDependencies );
                if ( f != null ) {
                    dependencies.add( f );

                }
            }
        }

        String expression;
        for ( ConstraintDecl constraint : constraintList ) {

            String name = constraint.name().isEmpty() ? null
                                                      : constraint.name().get();
            expression = constraint.exp().toJavaString();
            if ( expression.contains( "==" ) ) {
                BinExp binExp = (BinExp)constraint.exp();
                String paramName = binExp.exp1().toJavaString();
                ClassData.Param param =
                        getClassData().getParam( getClassData().getCurrentClass(),
                                                 paramName, true, false, false,
                                                 false );
                if ( param != null ) {
                    String type = param.type;
                    ClassData.Param depParam =
                            new ClassData.Param( paramName, type,
                                                 binExp.exp2().toJavaString() );
                    if ( depParam.value != "null" ) {
                        FieldDeclaration f =
                                createDependencyField( depParam,
                                                       initDependencies );
                        if ( f != null ) {
                            dependencies.add( f );

                        }
                    }
                }

            }

        }

        return dependencies;
    }

    public FieldDeclaration
           createDependencyField( ClassData.Param p,
                                  MethodDeclaration initDependencies ) {

        String suffix = "Dependency";
        String depName = p.name.replace( '.', '_' ) + suffix;
        Expression sinkExpr = expressionTranslator.parseExpression( p.name );
        String sink = null;
        if ( sinkExpr instanceof FieldAccessExpr ) {
            sink = expressionTranslator.fieldExprToAe( (FieldAccessExpr)sinkExpr,
                                                       true, true, false, false,
                                                       false, true );
        } else {
            sink = p.name;
        }
        String source = expressionTranslator.javaToAeExpr( p.value, null, // p.type,
                                                           true, false );

        String scope = null; // , member = null;
        int pos = sink.lastIndexOf( "." );
        if ( pos >= 0 ) {
            scope = sink.substring( 0, sink.lastIndexOf( '.' ) );

        }

        String addDepStmt = "addDependency( " + sink + ", " + source + " );";
        if ( scope != null ) {
            addDepStmt = "if ( ((Object)" + scope
                         + ") instanceof ParameterListenerImpl) {\n((ParameterListenerImpl)((Object)"
                         + scope + "))." + addDepStmt + "\n} else {\n"
                         + addDepStmt + "\n}";
        }

        addStatements( initDependencies.getBody(), addDepStmt );
        return createFieldOfGenericType( depName, "Dependency", p.type, null );
    }

    public ArrayList< FieldDeclaration >
           getParameters( EntityDecl entity, MethodDeclaration initMembers ) {
        ArrayList< FieldDeclaration > parameters =
                new ArrayList< FieldDeclaration >();
        ArrayList< PropertyDecl > propertyList;
        if ( entity == null ) {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( this.model ) ) );
        } else {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getPropertyDecls() ) );
        }
        FieldDeclaration f;
        for ( PropertyDecl property : propertyList ) {
            ClassData.Param p = makeParam( property );
            f = createParameterField( p, initMembers );
            if ( f != null ) {
                parameters.add( f );
            }

        }

        return parameters;
    }

    public Collection< FieldDeclaration >
           getConstraints( EntityDecl entity, MethodDeclaration initMembers ) {
        ArrayList< FieldDeclaration > constraints =
                new ArrayList< FieldDeclaration >();
        FieldDeclaration f;
        String expression;
        ArrayList< ConstraintDecl > constraintList;
        if ( entity == null ) {
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelConstraints( this.model ) ) );
        } else {
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( entity.getConstraintDecls() ) );
        }
        for ( ConstraintDecl constraint : constraintList ) {

            String name = constraint.name().isEmpty() ? null
                                                      : constraint.name().get();
            expression = constraint.exp().toJavaString();
            if ( expression.contains( "==" ) ) {
                BinExp binExp = (BinExp)constraint.exp();
                String paramName = binExp.exp1().toJavaString();
                ClassData.Param param =
                        getClassData().getParam( getClassData().getCurrentClass(),
                                                 paramName, true, false, false,
                                                 false );
                if ( param != null ) {
                    continue;
                }
            }

            f = createConstraintField( name, expression, initMembers );
            if ( f != null ) {
                constraints.add( f );
            }

        }
    

    return constraints;

    }

    public FieldDeclaration createConstraintField( String name,
                                                   String expression ) {

        if ( name == null ) {
            name = new String( "constraint" + counter++ );
        }

        String constructorArgs =
                expressionTranslator.javaToAeExpr( expression, "Boolean",
                                                   false );
        String constraintType = "ConstraintExpression";

        return createFieldOfGenericType( name, constraintType,
                                         constructorArgs );

    }

    // public String translateExpression(String expression) {
    //
    // }

    public FieldDeclaration
           createConstraintField( String name, String expression,
                                  MethodDeclaration initMembers ) {
        if ( initMembers == null ) {
            return createConstraintField( name, expression );
        }
        if ( name == null || name.trim().length() == 0 ) {
            name = new String( "constraint" + counter++ );
        }

        String constructorArgs =
                expressionTranslator.javaToAeExpr( expression, "Boolean",
                                                   true );
        // constructorArgs = "new Expression<Boolean>( \"" + expression +
        // "\", \"Java\" )";
        String constraintType = "ConstraintExpression";

        Statement s = createAssignmentOfGenericType( name, constraintType, null,
                                                     constructorArgs );
        ASTHelper.addStmt( initMembers.getBody(), s );

        return createFieldOfGenericType( name, constraintType, null, null );
    }

    public FieldDeclaration createParameterField( ClassData.Param p ) {
        String args[] =
                expressionTranslator.convertToEventParameterTypeAndConstructorArgs( p );
        // return createFieldOfGenericType( p.name, type, p.type, args );
        return createFieldOfGenericType( p.name, args[ 0 ], null, // args[ 1 ],
                                         args[ 2 ] );
    }

    public FieldDeclaration
           createParameterField( ClassData.Param p,
                                 MethodDeclaration initMembers ) {
        if ( initMembers == null ) {
            return createParameterField( p );
        }
        String args[] =
                expressionTranslator.convertToEventParameterTypeAndConstructorArgs( p );
        Statement s = createAssignmentOfGenericType( p.name, args[ 0 ],
                                                     args[ 1 ], args[ 2 ] );
        ASTHelper.addStmt( initMembers.getBody(), s );
        FieldDeclaration f =
                createFieldOfGenericType( p.name, args[ 0 ], args[ 1 ], null );

        return f;
    }

    public static FieldDeclaration
           createFieldOfGenericType( String name, String typeName,
                                     String parameterTypeName,
                                     String constructorArgs ) {
        String fieldTypeName = typeName;
        if ( !Utils.isNullOrEmpty( parameterTypeName ) ) {
            fieldTypeName +=
                    "< " + ClassUtils.getNonPrimitiveClassName( parameterTypeName )
                             + " >";
        }
        ClassOrInterfaceType fieldType =
                new ClassOrInterfaceType( fieldTypeName );
        FieldDeclaration f = null;
        VariableDeclaratorId id = new VariableDeclaratorId( name );
        Expression init = null;
        String initValue = null;
        if ( constructorArgs == null ) {
            initValue = "null";
        } else {
            initValue = "new " + typeName;
            if ( !Utils.isNullOrEmpty( parameterTypeName ) ) {
                initValue +=
                        "< " + ClassUtils.getNonPrimitiveClassName( parameterTypeName )
                             + " >";
            }
            initValue += "( " + constructorArgs + " )";
        }
        init = new NameExpr( initValue );
        VariableDeclarator variable = new VariableDeclarator( id, init );
        f = ASTHelper.createFieldDeclaration( ModifierSet.PUBLIC, fieldType,
                                              variable );
        return f;
    }

    public static Statement
           createAssignmentOfGenericType( String name, String typeName,
                                          String parameterTypeName,
                                          String constructorArgs ) {
        StringBuffer stmtsString = new StringBuffer();
        stmtsString.append( "if ( " + name + " == null ) " );
        stmtsString.append( name + " = " );
        if ( constructorArgs == null ) {
            stmtsString.append( "null;" );
        } else {
            stmtsString.append( "new " + typeName );
            if ( !Utils.isNullOrEmpty( parameterTypeName ) ) {
                stmtsString.append( "< "
                                    + ClassUtils.getNonPrimitiveClassName( parameterTypeName )
                                    + " >" );
            }
            stmtsString.append( "( " + constructorArgs + " );" );
        }

        if ( Debug.isOn() ) Debug.outln( "Trying to parse assignment with ASTParser.BlockStatement(): \""
                                         + stmtsString.toString() + "\"" );
        ASTParser parser =
                new ASTParser( new StringReader( stmtsString.toString() ) );
        Statement stmt = null;
        try {
            stmt = parser.BlockStatement();
        } catch ( ParseException e ) {
            e.printStackTrace();
        }

        return stmt;
    }

    public static FieldDeclaration
           createFieldOfGenericType( String name, String typeName,
                                     String constructorArgs ) {
        return createFieldOfGenericType( name, typeName, null,
                                         constructorArgs );
    }

    protected MethodDeclaration createPublicVoidMethod( String methodName ) {
        MethodDeclaration initMembers =
                new MethodDeclaration( ModifierSet.PUBLIC, new VoidType(),
                                       methodName );
        initMembers.setBody( new BlockStmt() );
        return initMembers;
    }

    public static void addStatements( BlockStmt block, String stmts ) {
        addStatements( block, -1, stmts );
    }

    public static void addStatements( BlockStmt block, int pos, String stmts ) {
        if ( Debug.isOn() ) Debug.outln( "trying to parse \"" + stmts + "\"" );
        List< Statement > list = stringToStatementList( stmts );
        addStmts( block, pos, list );
    }

    private static void addStmts( BlockStmt block, List< Statement > list ) {
        addStmts( block, -1, list );
    }

    private static void addStmts( BlockStmt block, int pos,
                                  List< Statement > list ) {
        if ( list != null ) {
            if ( pos == -1 ) {
                if ( block == null
                     || Utils.isNullOrEmpty( block.getStmts() ) ) {
                    pos = 0;
                } else {
                    pos = block.getStmts().size();
                }
            }
            for ( Statement stmt : list ) {
                addStmt( block, pos++, stmt );
            }
        }
    }

    public static void addStmt( BlockStmt block, int pos, Statement stmt ) {
        List< Statement > stmts = block.getStmts();
        if ( stmts == null ) {
            stmts = new ArrayList< Statement >();
            block.setStmts( stmts );
        }
        if ( pos == -1 ) {
            stmts.add( stmt );
        } else {
            stmts.add( pos, stmt );
        }
    }

    public static List< Statement > stringToStatementList( String s ) {
        if ( Debug.isOn() ) Debug.outln( "trying to parse Java statements \""
                                         + s + "\"" );
        ASTParser parser = new ASTParser( new StringReader( s ) );
        List< Statement > stmtList = null;
        try {
            stmtList = parser.Statements();
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stmtList;
    }

    protected void setPackage() {
        // set the package based on the xmlFileName
        // String packageName =
        // "generated."
        // + xmlFileName.substring( 0, xmlFileName.lastIndexOf( '.' ) )
        // .replaceAll( "[^A-Za-z0-9_]+", "_" );
        if ( Debug.isOn() ) Debug.outln( "setting package for current compilation unit to "
                                         + packageName );
        getClassData().getCurrentCompilationUnit()
                      .setPackage( new PackageDeclaration( ASTHelper.createNameExpr( packageName ) ) );
    }

    private CompilationUnit initCompilationUnit( String name ) {
        getClassData().setCurrentCompilationUnit( new CompilationUnit() );
        getClassData().getClasses()
                      .put( ClassUtils.simpleName( name ),
                            getClassData().getCurrentCompilationUnit() );
        setPackage();
        return getClassData().getCurrentCompilationUnit();
    }

    private CompilationUnit initClassCompilationUnit( String name ) {
        getClassData().setCurrentCompilationUnit( initCompilationUnit( ClassUtils.simpleName( name ) ) );
        // REVIEW -- How can we access eclipse's ability to auto-remove unused
        // imports?
        // addImport( "gov.nasa.jpl.ae.event.*" );
        addImport( "gov.nasa.jpl.ae.event.Parameter" );
        addImport( "gov.nasa.jpl.ae.event.IntegerParameter" );
        addImport( "gov.nasa.jpl.ae.event.LongParameter" );
        addImport( "gov.nasa.jpl.ae.event.DoubleParameter" );
        addImport( "gov.nasa.jpl.ae.event.StringParameter" );
        addImport( "gov.nasa.jpl.ae.event.BooleanParameter" );
        addImport( "gov.nasa.jpl.ae.event.StateVariable" );
        addImport( "gov.nasa.jpl.ae.event.Timepoint" );
        addImport( "gov.nasa.jpl.ae.event.Expression" );
        addImport( "gov.nasa.jpl.ae.event.ConstraintExpression" );
        addImport( "gov.nasa.jpl.ae.event.Functions" );
        addImport( "gov.nasa.jpl.ae.event.FunctionCall" );
        addImport( "gov.nasa.jpl.ae.event.ConstructorCall" );
        addImport( "gov.nasa.jpl.ae.event.Call" );
        addImport( "gov.nasa.jpl.ae.event.Effect" );
        addImport( "gov.nasa.jpl.ae.event.EffectFunction" );
        addImport( "gov.nasa.jpl.ae.event.TimeDependentConstraintExpression" );
        addImport( "gov.nasa.jpl.ae.event.Dependency" );
        addImport( "gov.nasa.jpl.ae.event.ElaborationRule" );
        addImport( "gov.nasa.jpl.ae.event.EventInvocation" );
        addImport( "gov.nasa.jpl.ae.event.DurativeEvent" );
        addImport( "gov.nasa.jpl.ae.event.Event" );
        addImport( "gov.nasa.jpl.mbee.util.Utils" );
        addImport( "gov.nasa.jpl.mbee.util.Debug" );
        addImport( "gov.nasa.jpl.mbee.util.ClassUtils" );
        addImport( "java.util.Vector" );
        addImport( "java.util.Map" );
        return getClassData().getCurrentCompilationUnit();
    }

    private void addImport( String impName ) {
        NameExpr ne = new NameExpr( impName );
        ImportDeclaration d = new ImportDeclaration( ne, false, false );
        if ( getClassData().getCurrentCompilationUnit().getImports() == null ) {
            getClassData().getCurrentCompilationUnit()
                          .setImports( new ArrayList< ImportDeclaration >() );
        }
        // check for duplicates -- REVIEW - inefficient linear search
        // TODO -- never finds duplicates!
        for ( ImportDeclaration i : getClassData().getCurrentCompilationUnit()
                                                  .getImports() ) {
            if ( i.getName().getName().equals( impName ) ) return;
        }
        getClassData().getCurrentCompilationUnit().getImports().add( d );
    }

    private void addTryCatchToInitMembers( MethodDeclaration initMembers ) {
        TryStmt tryStmt = null;

        // Need to add a statement that will certainly need all of these
        // exceptions;
        // otherwise, we'll get a compile error for trying to catch something
        // that
        // can't be thrown. Test code commented out below.
        String pkg = packageName + ".";
        if ( pkg.length() == 1 ) {
            pkg = "";
        }

        String tryCatchString = "try{\n" + ";\n" + "} catch ( Exception e ) {\n"
                                + "  // TODO Auto-generated catch block\n"
                                + "  e.printStackTrace();\n" + "}\n";

        List< Statement > stmts = new ArrayList< Statement >();
        if ( Debug.isOn() ) Debug.outln( "trying to parse \"" + stmts + "\"" );

        ASTParser parser = new ASTParser( new StringReader( tryCatchString ) );
        try {
            tryStmt = parser.TryStatement();
        } catch ( ParseException e ) {
            e.printStackTrace();
            return;
        }
        tryStmt.setTryBlock( initMembers.getBody() );
        stmts.add( tryStmt );
        BlockStmt newBody = new BlockStmt( stmts );
        initMembers.setBody( newBody );
    }

    private MethodDeclaration
            createInitCollectionsMethod( String methodName,
                                         Collection< FieldDeclaration > parameters,

                                         Collection< FieldDeclaration > constraints ) {
        MethodDeclaration initCollections =
                new MethodDeclaration( ModifierSet.PROTECTED, new VoidType(),
                                       methodName );
        // TODO -- Add initCollections()'s body.
        BlockStmt block = new BlockStmt();
        List< Statement > stmtList = null;
        stmtList = createStmtsFromFieldCollection( "parameters.add( ",
                                                   parameters, " );\n" );
        addStmts( block, stmtList );
        stmtList =
                createStmtsFromFieldCollection( "constraintExpressions.add( ",
                                                constraints, " );\n" );
        addStmts( block, stmtList );

        initCollections.setBody( block );
        return initCollections;
    }

    private List< Statement > createStmtsFromFieldCollection( String prefix,
                                                              Collection< FieldDeclaration > fieldCollection,
                                                              String suffix ) {
        if ( fieldCollection == null || fieldCollection.isEmpty() ) return null;
        // private Statement createInitCollectionStmt( String collectionName,
        // Collection< FieldDeclaration > fieldCollection ) {
        StringBuilder sb = new StringBuilder();
        for ( FieldDeclaration f : fieldCollection ) {
            // sb.append(" " + collectionName + ".add( " + f.getVariables().get(
            // 0
            // ) + " );\n" );
            sb.append( prefix + f.getVariables().get( 0 ).getId() + suffix );
        }
        return stringToStatementList( sb.toString() );
    }

    protected void processExecutionEvent() { // change this to something

        getClassData().setCurrentClass( "Main" );
        initClassCompilationUnit( getClassData().getCurrentClass() );

        ClassOrInterfaceDeclaration newClassDecl =
                new ClassOrInterfaceDeclaration( ModifierSet.PUBLIC, false,
                                                 getClassData().getCurrentClass() );
        ASTHelper.addTypeDeclaration( getClassData().getCurrentCompilationUnit(),
                                      newClassDecl );

        int mods = ModifierSet.PUBLIC | ModifierSet.STATIC;

        MethodDeclaration mainMethodDecl =
                new MethodDeclaration( mods, new VoidType(), "main" );
        BlockStmt mainBody = new BlockStmt();
        mainMethodDecl.setBody( mainBody );

        ConstructorDeclaration ctor =
                new ConstructorDeclaration( ModifierSet.PUBLIC,
                                            newClassDecl.getName() );
        ASTHelper.addMember( newClassDecl, ctor );
        BlockStmt ctorBody = new BlockStmt();
        ctor.setBlock( ctorBody );

        Type type = ASTHelper.createReferenceType( "String", 1 );
        VariableDeclaratorId id = new VariableDeclaratorId( "args" );
        japa.parser.ast.body.Parameter parameter =
                new japa.parser.ast.body.Parameter( type, id );
        // Wire everything together.
        ASTHelper.addParameter( mainMethodDecl, parameter );
        ASTHelper.addMember( newClassDecl, mainMethodDecl );

        // List< PropertyDecl > topLevelProperties =
        // new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection(
        // Frontend.getTopLevelProperties( this.model ) ) );
        // PropertyDecl toExecute = topLevelProperties.get( 0 );
        // String className = toExecute.ty().toString();
        // String instanceName = toExecute.name();
        // if ( instanceName == null || instanceName.isEmpty() ) {
        // instanceName = className + ( counter++ );
        // }

        addExtends( newClassDecl, globalName );

        StringBuffer stmtsMain = new StringBuffer();

        stmtsMain.append( "Main scenario = new Main();" );
        stmtsMain.append( "scenario.satisfy( true, null );" );
        stmtsMain.append( "System.out.println((scenario.isSatisfied(true, null) ? \"Satisfied\" : \"Not Satisfied\") + \"\\n\" + scenario.executionString());" );


        List< Expression > args = new ArrayList< Expression >();

        ASTHelper.addStmt( ctorBody,
                           new ExplicitConstructorInvocationStmt( false, null,
                                                                  args ) );

        addImport( "gov.nasa.jpl.ae.event.Expression" );

        addStatements( mainBody, stmtsMain.toString() );
    }

    protected static void addExtends( ClassOrInterfaceDeclaration newClassDecl,
                                      String superClass ) {
        if ( newClassDecl.getExtends() == null ) {
            newClassDecl.setExtends( new ArrayList< ClassOrInterfaceType >() );
        }
        newClassDecl.getExtends().add( new ClassOrInterfaceType( superClass ) );
    }

    public String getPackageSourcePath( String projectPath ) {
        if ( projectPath == null ) {
            projectPath = "";
        } else {
            projectPath += File.separator;
        }
        String packagePath =
                getPackageName().replace( '.', File.separatorChar );
        String srcPath = projectPath + "src" + File.separator + packagePath;
        return srcPath;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void writeFiles( KtoJava translator, String directory ) {
        if ( translator != null ) {
            // Figure out where to write the files
            String targetDirectory = translator.getPackageSourcePath( null );
            if ( targetDirectory == null ) {
                if ( directory == null ) {
                    targetDirectory = packageName;
                } else {
                    targetDirectory = directory + File.separator + packageName;
                }
            }

            // Create the directory for the package where the files will be
            // written
            // and see if the directory exists.
            File targetDirectoryFile = new File( targetDirectory );
            if ( !targetDirectoryFile.exists() ) {
                if ( !targetDirectoryFile.mkdirs() ) {
                    System.err.println( "Error! Unable to make package directory: "
                                        + targetDirectoryFile.getAbsolutePath() );
                }
            } else {
                assert targetDirectoryFile.isDirectory();
            }

            // Delete old Java and class files.
            File[] files =
                    EventXmlToJava.getJavaFileList( targetDirectoryFile );
            Debug.outln( "Deleting old .java files in "
                         + targetDirectoryFile.getAbsolutePath() + ": "
                         + Utils.toString( files ) );
            EventXmlToJava.deleteFiles( files );
            files = translator.getJavaFiles( targetDirectory, false, false );
            Debug.outln( "Deleting old .class files in "
                         + targetDirectoryFile.getAbsolutePath() + ": "
                         + Utils.toString( files ) );
            EventXmlToJava.deleteFiles( files );
            String binDir = targetDirectoryFile.getAbsolutePath()
                                               .replaceFirst( "([^a-zA-Z])src([^a-zA-Z])",
                                                              "\\1bin\\2" );
            files = translator.getJavaFiles( binDir, false, false );
            Debug.outln( "Deleting old .class files in " + binDir + ": "
                         + Utils.toString( files ) );
            EventXmlToJava.deleteFiles( files );

            // Now write the files.
            try {
                translator.writeJavaFiles( targetDirectory );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void writeJavaFile( String fileName ) throws IOException {
        File f = new File( fileName );
        FileWriter w = new FileWriter( f );
        w.write( getClassData().getCurrentCompilationUnit().toString() );
        w.close();
    }

    public void writeJavaFiles( String javaPath ) throws IOException {
        for ( Entry< String, CompilationUnit > e : getClassData().getClasses()
                                                                 .entrySet() ) {
            getClassData().setCurrentClass( e.getKey() );
            getClassData().setCurrentCompilationUnit( e.getValue() );
            String fileName =
                    ( javaPath.trim() + File.separator + e.getKey() + ".java" );
            writeJavaFile( fileName );
            if ( Debug.isOn() ) Debug.outln( "wrote compilation unit to file "
                                             + fileName );
        }
    }

    public static File[] getJavaFileList( File path ) {
        File[] fileArr = null;
        assert path.exists();
        fileArr = path.listFiles();
        return fileArr;
    }

    public static File[] getJavaFileList( String javaPath ) {
        File[] fileArr = null;
        File path = new File( javaPath );
        return getJavaFileList( path );
    }

    public File[] getJavaFiles( String javaPath, boolean sourceOrClass,
                                boolean justCurrentClasses ) {
        File[] fileArr = null;
        File path = new File( javaPath );
        if ( javaPath == null ) {
            javaPath = ( sourceOrClass ? "src" : "bin" ) + File.separator
                       + this.packageName;
            File path2 = new File( javaPath );
            if ( !path2.exists() && !sourceOrClass ) {
                javaPath = "src" + File.separator + this.packageName;
                path2 = new File( javaPath );
            }
            if ( path2.exists() ) {
                path = path2;
            }
        }
        assert path.exists();
        if ( !justCurrentClasses ) {
            fileArr = getJavaFileList( path );
            if ( fileArr != null ) {
                List< File > files = new ArrayList< File >();
                for ( File f : fileArr ) {
                    if ( f.getName()
                          .endsWith( sourceOrClass ? ".java" : ".class" ) ) {
                        files.add( f );
                    }
                }
                fileArr = new File[ files.size() ];
                int ctr = 0;
                for ( File f : files ) {
                    fileArr[ ctr++ ] = f;
                }
            }
            return fileArr;
        }

        fileArr = new File[ getClassData().getClasses().size() ];
        if ( !getClassData().getClasses().isEmpty() ) {
            int ctr = 0;
            for ( String clsName : getClassData().getClasses().keySet() ) {
                String filePathName = javaPath.trim() + File.separator + clsName
                                      + ( sourceOrClass ? ".java" : ".class" );
                fileArr[ ctr++ ] = new File( filePathName );
            }
        }
        return fileArr;
    }

    public static void main( String[] args ) {
        // ParameterListenerImpl p = new ParameterListenerImpl( "hi" );
        // IntegerParameter i = new IntegerParameter( "i", p );
        // p.getParameters().add( i );
        // p.getConstraintExpressions()
        // .add( new ConstraintExpression( new Functions.Less( new
        // gov.nasa.jpl.ae.event.Expression( i ),
        // new gov.nasa.jpl.ae.event.Expression( 5 ) ) ) );
        // p.satisfy( true, null );
        // System.out.println( "i = " + i.getValue() );
        String kToExecute = "";
        for ( String arg : args ) {
            kToExecute += arg + " ";
        }

        KtoJava kToJava = new KtoJava( kToExecute, "generatedCode" );

        kToJava.writeFiles( kToJava, "/Users/ayelaman/git/kservices" );

    }

}
