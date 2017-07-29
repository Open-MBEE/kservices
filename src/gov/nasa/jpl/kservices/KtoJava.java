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
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.Math;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

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
import k.frontend.MemberDecl;
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
    int constraintCounter;
    int expressionCounter;
    TypeChecker typeChecker;
    Model model;
    final String globalName;
    final String autoGenerated;
    Set< EntityDecl > allClasses;
    Set< EntityDecl > topLevelClasses;
    Set< String > allClassNames;
    Set< String > topLevelClassNames;
    Set< String > instantiatedClassNames;
    Map< String, Set< String > > classToParentNames;

    public KtoJava( String k, String pkgName, boolean translate ) {
        this.globalName = "Global";
        this.autoGenerated = "AutoGenerated";
        this.k = k;
        if ( pkgName != null && !pkgName.equals( "" ) ) {
            this.packageName = pkgName;
        }
        // Debug.turnOn();
        this.constraintCounter = 0;
        this.expressionCounter = 0;
        this.expressionTranslator =
                new JavaToConstraintExpression( packageName );
        System.out.println();

        this.model = Frontend.getModelFromString( this.k );
        System.out.println();
        try {

            typeChecker = new TypeChecker( this.model );
        } catch ( Throwable e ) {
            System.err.println( "Input did not Type Check " + e );
        }
        this.topLevelClasses = getTopLevelClasses();
        this.allClasses = getAllClasses();
        this.allClassNames = new TreeSet< String >();
        this.topLevelClassNames = new TreeSet< String >();
        this.classToParentNames = new TreeMap< String, Set< String > >();
        for ( EntityDecl e : topLevelClasses ) {
            this.topLevelClassNames.add( e.ident() );
        }
        for ( EntityDecl e : allClasses ) {
            this.allClassNames.add( e.ident() );
            this.classToParentNames.put( e.ident(),
                                         new TreeSet< String >( JavaConversions.asJavaCollection( e.getExtendingNames() ) ) );

        }
        for ( String e : allClassNames ) {
            getAllSuperClassNames( e );
        }

        this.instantiatedClassNames = new TreeSet< String >();
        buildNestingTable( getClassData().getNestedToEnclosingClassNames() );
        buildParamTable( getClassData().getParamTable() );
        buildMethodTable( getClassData().getMethodTable() );
        if ( translate ) {
            translateClasses();
        }

    }

    public KtoJava( String k, String pkgName ) {
        this( k, pkgName, true );
    }

    public Set< EntityDecl > getTopLevelClasses() {
        return new HashSet< EntityDecl >( JavaConversions.asJavaCollection( Frontend.getEntitiesFromModel( this.model ) ) );
    }

    public Set< EntityDecl > getAllClasses() {
        Deque< EntityDecl > entitiesToGo =
                new ArrayDeque< EntityDecl >( topLevelClasses );
        Set< EntityDecl > allEntities = new HashSet< EntityDecl >();
        EntityDecl entity;
        while ( !entitiesToGo.isEmpty() ) {
            entity = entitiesToGo.pop();
            allEntities.add( entity );
            entitiesToGo.addAll( JavaConversions.asJavaCollection( entity.getEntityDecls() ) );
        }
        return allEntities;
    }

    public void getAllSuperClassNames( String entityName ) {
        Set< String > extendingList = classToParentNames.get( entityName );
        for ( String e : extendingList ) {
            getAllSuperClassNames( e );
            extendingList.addAll( classToParentNames.get( e ) );
        }
    }

    public void buildNestingTable( Map< String, String > nestingTable ) {
        // true nested classes:
        for ( EntityDecl entity : this.allClasses ) {
            for ( EntityDecl innerEntity : JavaConversions.asJavaCollection( entity.getEntityDecls() ) ) {
                nestingTable.put( innerEntity.ident(), entity.ident() );
            }
        }

        for ( EntityDecl entity : this.allClasses ) {
            if ( !nestingTable.containsKey( entity.ident() ) ) {
                nestingTable.put( entity.ident(), globalName );
            }
        }
    }

    public void
           buildParamTable( Map< String, Map< String, ClassData.Param > > paramTable ) {
        Map< String, ClassData.Param > params =
                new TreeMap< String, ClassData.Param >();
        ClassData.Param param;
        addGlobalParams( paramTable );

        for ( EntityDecl entity : this.allClasses ) { // pass 1
            String entityName = getClassName( entity );
            params = new TreeMap< String, ClassData.Param >();
            ArrayList< PropertyDecl > propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getPropertyDecls() ) );
            for ( PropertyDecl p : propertyList ) {
                param = makeParam( p, entity );
                String type = p.ty().toString();
                if ( this.allClassNames.contains( type ) ) {
                    this.instantiatedClassNames.add( type );
                }
                params.put( p.name(), param );
            }
            ArrayList< FunDecl > funList =
                    new ArrayList< FunDecl >( JavaConversions.asJavaCollection( entity.getFunDecls() ) );
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
        for ( EntityDecl entity : this.allClasses ) { // pass 2
            String entityName = getClassName( entity );
            params = paramTable.get( entityName );
            Set< String > extendingList =
                    classToParentNames.get( entity.ident() );
            for ( String e : extendingList ) {
                Map< String, ClassData.Param > otherParams =
                        paramTable.get( getClassName( e ) );
                params.putAll( otherParams );
            }

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
            param = makeParam( p, null );
            String type = p.ty().toString();
            if ( this.allClassNames.contains( type ) ) {
                this.instantiatedClassNames.add( type );
            }
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
        for ( EntityDecl entity : this.allClasses ) {
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
        String typeString =
                JavaToConstraintExpression.typeToClass( funDecl.ty().get()
                                                               .toString() );
        methodDecl.setType( new ClassOrInterfaceType( "Expression<" + typeString
                                                      + ">" ) );
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

    public Boolean isPrimitive( String typeString ) {
        return typeString.equals( "Int" ) || typeString.equals( "Bool" )
               || typeString.equals( "Real" ) || typeString.equals( "String" );
    }

    public ClassData.Param makeParam( PropertyDecl p, EntityDecl e ) {
        String name = p.name();
        String typeOld =
                JavaToConstraintExpression.typeToClass( p.ty().toString() );
        String type = typeOld;
        if ( e != null ) {
            // type = getClassName( type );
        }
        if ( ( typeOld.equals( "Boolean" ) || typeOld.equals( "Double" )
               || typeOld.equals( "Integer" ) || typeOld.equals( "Long" )
               || typeOld.equals( "String" ) ) ) {
            type = typeOld;
        }
        String value;
        if ( p.expr().isEmpty() ) {
            value = "null";
            if ( !( typeOld.equals( "Boolean" ) || typeOld.equals( "Double" )
                    || typeOld.equals( "Integer" ) || typeOld.equals( "Long" )
                    || typeOld.equals( "String" ) ) ) {
                value = "new " + type + "()";
            }
        } else {
            value = p.expr().get().toJavaString();
        }
        return new ClassData.Param( name, type, value );
    }

    public ClassData.Param makeParam( PropertyDecl p, EntityDecl e,
                                      Boolean nullValue ) {
        if ( !nullValue ) {
            return makeParam( p, e );
        }

        String name = p.name();
        String typeOld =
                JavaToConstraintExpression.typeToClass( p.ty().toString() );
        String type = typeOld;
        if ( e != null ) {
            // type = getClassName( type );
        }
        if ( ( typeOld.equals( "Boolean" ) || typeOld.equals( "Double" )
               || typeOld.equals( "Integer" ) || typeOld.equals( "Long" )
               || typeOld.equals( "String" ) ) ) {
            type = typeOld;
        }
        String value;
        if ( p.expr().isEmpty() ) {
            value = "null";
            if ( !( typeOld.equals( "Boolean" ) || typeOld.equals( "Double" )
                    || typeOld.equals( "Integer" ) || typeOld.equals( "Long" )
                    || typeOld.equals( "String" ) ) ) {
                value = "new " + type + "()";
            }
        } else {
            value = "null";

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
        processClassDeclarations( null, justClassDeclarations );
    }

    private void processClassDeclarations( EntityDecl entity,
                                           boolean justClassDeclarations ) {
        ClassOrInterfaceDeclaration classDecl;
        if ( justClassDeclarations ) {
            if ( entity == null ) {
                getClassData().setCurrentCompilationUnit( initClassCompilationUnit( globalName ) );
            }
            classDecl =
                    processClassDeclaration( entity, justClassDeclarations );
            ASTHelper.addTypeDeclaration( getClassData().getCurrentCompilationUnit(),
                                          classDecl );
        } else {
            processClassDeclaration( entity, justClassDeclarations );
        }
    }

    public String getClassName( EntityDecl entity ) {
        if ( entity == null ) {
            return globalName;
        } else {
            return getClassName( entity.ident() );
        }
    }

    public String getClassName( String entityName ) {
        if ( entityName != null ) {
            String className = entityName;
            entityName = getClassData().getNestedToEnclosingClassNames()
                                       .get( entityName );
            while ( entityName != null ) {
                className = entityName + "." + className;
                entityName = getClassData().getNestedToEnclosingClassNames()
                                           .get( entityName );
            }
            return className;
        } else {
            return globalName;
        }
    }

    public ClassOrInterfaceDeclaration
           processClassDeclaration( EntityDecl entity,
                                    boolean justClassDeclarations ) {

        Collection< EntityDecl > innerEntities;
        String enclosingIdent;
        Collection< ClassOrInterfaceDeclaration > innerClassDecls =
                new ArrayList< ClassOrInterfaceDeclaration >();

        if ( entity == null ) {
            innerEntities = this.topLevelClasses;
            enclosingIdent = globalName;
        } else {
            innerEntities =
                    JavaConversions.asJavaCollection( entity.getEntityDecls() );
            enclosingIdent = entity.ident();
        }

        for ( EntityDecl innerEntity : innerEntities ) {
            innerClassDecls.add( processClassDeclaration( innerEntity,
                                                          justClassDeclarations ) );
        }

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

        if ( justClassDeclarations ) {
            for ( ClassOrInterfaceDeclaration innerClassDecl : innerClassDecls ) {
                ASTHelper.addMember( newClassDecl, innerClassDecl );
            }
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
                                                                  true, false );
                        addStatements( body, "return " + aeString + ";" );

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
            addExtends( newClassDecl, "ParameterListenerImpl" );
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

    // right now assumes imports are java imports
    protected void getImports() {
        List< ImportDecl > imports =
                new ArrayList< ImportDecl >( JavaConversions.asJavaCollection( model.imports() ) );
        for ( ImportDecl imp : imports ) {
            addImport( imp.toStringNoImport() );
        }
    }
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

        parameters.addAll( getExpressions( entity, initMembers ) );

        members.addAll( parameters );
        members.addAll( constraints );
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
           getParameters( EntityDecl entity, MethodDeclaration initMembers ) {
        ArrayList< FieldDeclaration > parameters =
                new ArrayList< FieldDeclaration >();
        ArrayList< PropertyDecl > propertyList;
        FieldDeclaration f;
        if ( entity == null ) {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( this.model ) ) );
            // TODO: improve scope resolution and turn this back into
            // allClassNames version
            // for ( String className : allClassNames ) {
            for ( String className : topLevelClassNames ) {
                if ( !instantiatedClassNames.contains( className ) ) {
                    ClassData.Param p =
                            new ClassData.Param( autoGenerated + className,
                                                 className, null );
                    f = createParameterField( p, initMembers );
                    if ( f != null ) {
                        parameters.add( f );
                    }
                }
            }
        } else {
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getPropertyDecls() ) );
        }

        for ( PropertyDecl property : propertyList ) {
            ClassData.Param p = makeParam( property, entity, true );
            f = createParameterField( p, initMembers );
            if ( f != null ) {
                parameters.add( f );
            }

        }

        return parameters;
    }

    public ArrayList< FieldDeclaration >
           getExpressions( EntityDecl entity, MethodDeclaration initMembers ) {
        ArrayList< FieldDeclaration > parameters =
                new ArrayList< FieldDeclaration >();
        if ( entity == null ) {
            ArrayList< ExpressionDecl > expressionList =
                    new ArrayList< ExpressionDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelExpressions( this.model ) ) );
            FieldDeclaration f;
            for ( ExpressionDecl expressionDecl : expressionList ) {
                Exp exp = expressionDecl.exp();
                String name = new String( "expression" + expressionCounter++ );
                // String type =
                // JavaToConstraintExpression.typeToClass(
                // TypeChecker.exp2Type()
                // .get( exp )
                // .toString() );
                String type = "Object";
                ClassData.Param p =
                        new ClassData.Param( name, type, exp.toJavaString() );
                f = createParameterField( p, initMembers );
                if ( f != null ) {
                    parameters.add( f );
                }

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
        ArrayList< PropertyDecl > propertyList;
        if ( entity == null ) {
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelConstraints( this.model ) ) );
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( this.model ) ) );
        } else {
            constraintList =
                    new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( entity.getConstraintDecls() ) );
            propertyList =
                    new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( entity.getPropertyDecls() ) );
        }
        for ( ConstraintDecl constraint : constraintList ) {

            String name = constraint.name().isEmpty() ? null
                                                      : constraint.name().get();
            expression = constraint.exp().toJavaString();

            f = createConstraintField( name, expression, initMembers );
            if ( f != null ) {
                constraints.add( f );
            }

        }

        for ( PropertyDecl property : propertyList ) {

            if ( !property.expr().isEmpty()
                 && isPrimitive( property.ty().toString() ) ) {
                f = createConstraintField( null,
                                           property.name() + " == "
                                                 + property.expr().get()
                                                           .toJavaString(),
                                           initMembers );
                if ( f != null ) {
                    constraints.add( f );
                }
            }
        }

        return constraints;

    }

    public FieldDeclaration createConstraintField( String name,
                                                   String expression ) {

        if ( name == null ) {
            name = new String( "constraint" + constraintCounter++ );
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
            name = new String( "constraint" + constraintCounter++ );
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
        addImport( "gov.nasa.jpl.ae.event.ParameterListenerImpl" );
        addImport( "gov.nasa.jpl.ae.event.TimeVarying" );
        addImport( "gov.nasa.jpl.ae.event.TimeVaryingMap" );
        addImport( "gov.nasa.jpl.ae.event.Timeline" );
        addImport( "gov.nasa.jpl.ae.event.Event" );
        addImport( "gov.nasa.jpl.ae.solver.ObjectDomain" );
        addImport( "gov.nasa.jpl.mbee.util.Utils" );
        addImport( "gov.nasa.jpl.mbee.util.Debug" );
        addImport( "gov.nasa.jpl.mbee.util.ClassUtils" );
        addImport( "java.util.Vector" );
        addImport( "java.util.Map" );
        getImports();
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

//        stmtsMain.append( "Main scenario = new Main();" );
//        stmtsMain.append( "scenario.amTopEventToSimulate = true;" );
//        stmtsMain.append( "System.out.println(\"===FULLOUTPUT===\" );" );
//        stmtsMain.append( "scenario.satisfy( true, null );" );
//        stmtsMain.append( "System.out.println(\"===RESULTS===\" );" );
//        stmtsMain.append( "System.out.println(scenario.kSolutionString());" );
        String y = "CaptureStdoutStderr c = new CaptureStdoutStderr() {\n" +
                "            @Override\n" +
                "            public Object run() {\n" +
                "                Main scenario = new Main();\n" +
                "                scenario.amTopEventToSimulate = true;\n" +
                "                //scenario.redirectStdOut = true;\n" +
                "                scenario.satisfy(true, null);\n" +
                "                return scenario;\n" +
                "            }\n" +
                "        };\n" +
                "        //System.out.println(scenario.simpleString());\n" +
                "\n" +
                "        Main s = (Main) c.result;\n" +
                "\n" +
                "        String out = c.baosOut.toString();\n" +
                "        FileUtils.stringToFile(out, \"" + packageName + "solverOutput.log\");\n" +
                "\n" +
                "        JSONObject json = new JSONObject();\n" +
                "\n" +
                "        JSONObject solution = s.kSolutionJson();\n" +
                "        json.put(\"result\", solution);\n" +
                "\n" +
                "        if ( c.baosErr.toString().length() > 3 ) {\n" +
                "            JSONArray jarr = json.getJSONArray(\"Solver Errors\");\n" +
                "            if (jarr == null) jarr = new JSONArray();\n" +
                "            jarr.put(c.baosErr);\n" +
                "            json.put(\"Solver Errors\", jarr);\n" +
                "        }\n" +
                "\n" +
                "        System.out.println(json.toString(4));";
        stmtsMain.append(y);


        List< Expression > args = new ArrayList< Expression >();

        ASTHelper.addStmt( ctorBody,
                           new ExplicitConstructorInvocationStmt( false, null,
                                                                  args ) );

        addImport( "gov.nasa.jpl.ae.event.Expression" );
        addImport("gov.nasa.jpl.ae.util.CaptureStdoutStderr");
        addImport("gov.nasa.jpl.mbee.util.FileUtils");
        addImport("org.json.JSONArray");
        addImport("org.json.JSONObject");


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

    public static JSONObject
           propertyToJSON( PropertyDecl p,
                           Map< MemberDecl, Tuple2< Object, Object > > map ) {
        JSONObject property = new JSONObject();
        property.put( "name", p.name() );
        property.put( "type", p.ty().toString() );
        Tuple2< Object, Object > numbers = map.get( p );

        property.put( "line", numbers._1() );
        property.put( "char", numbers._2() );

        property.put( "children", new JSONArray() );

        return property;
    }

    public static JSONObject
           functionToJSON( FunDecl f,
                           Map< MemberDecl, Tuple2< Object, Object > > map ) {
        JSONObject function = new JSONObject();
        function.put( "name", f.ident() );
        function.put( "type", "function" );
        Tuple2< Object, Object > numbers = map.get( f );
        function.put( "line", numbers._1() );
        function.put( "char", numbers._2() );
        function.put( "children", new JSONArray() );

        return function;
    }

    public static JSONObject
           entityToJSON( EntityDecl e,
                         Map< MemberDecl, Tuple2< Object, Object > > map ) {
        JSONObject entity = new JSONObject();
        entity.put( "name", e.ident() );
        entity.put( "type", "class" );
        Tuple2< Object, Object > numbers = map.get( e );
        entity.put( "line", numbers._1() );
        entity.put( "char", numbers._2() );
        JSONArray children = new JSONArray();
        List< PropertyDecl > properties =
                new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( e.getPropertyDecls() ) );
        for ( PropertyDecl p : properties ) {
            children.put( propertyToJSON( p, map ) );
        }
        List< FunDecl > functions =
                new ArrayList< FunDecl >( JavaConversions.asJavaCollection( e.getFunDecls() ) );
        for ( FunDecl f : functions ) {
            children.put( functionToJSON( f, map ) );
        }

        List< ConstraintDecl > constraints =
                new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( e.getConstraintDecls() ) );
        for ( ConstraintDecl c : constraints ) {
            JSONObject constraint = constraintToJSON( c, map );
            if ( constraint != null ) {
                children.put( constraint );
            }
        }

        List< EntityDecl > entities =
                new ArrayList< EntityDecl >( JavaConversions.asJavaCollection( e.getEntityDecls() ) );
        for ( EntityDecl ent : entities ) {
            JSONObject entJSON = entityToJSON( ent, map );
            if ( entJSON != null ) {
                children.put( entJSON );
            }
        }

        entity.put( "children", children );

        return entity;
    }

    public static JSONObject
           constraintToJSON( ConstraintDecl c,
                             Map< MemberDecl, Tuple2< Object, Object > > map ) {
        if ( c.name().isEmpty() ) {
            return null;
        }
        JSONObject constraint = new JSONObject();
        constraint.put( "name", c.name().get() );
        constraint.put( "type", "req" );
        Tuple2< Object, Object > numbers = map.get( c );

        constraint.put( "line", numbers._1() );
        constraint.put( "char", numbers._2() );

        constraint.put( "children", new JSONArray() );
        return constraint;
    }

    public static JSONObject kToContainmentTree( String k ) {
        Model m = Frontend.getModelFromString( k );
        Map< MemberDecl, Tuple2< Object, Object > > map =
                JavaConversions.mapAsJavaMap( Frontend.getDeclDict( k ) );
        JSONObject tree = new JSONObject();
        JSONArray topDecls = new JSONArray();
        List< EntityDecl > entities =
                new ArrayList< EntityDecl >( JavaConversions.asJavaCollection( Frontend.getEntitiesFromModel( m ) ) );
        List< PropertyDecl > properties =
                new ArrayList< PropertyDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelProperties( m ) ) );
        List< FunDecl > functions =
                new ArrayList< FunDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelFunctions( m ) ) );
        List< ConstraintDecl > constraints =
                new ArrayList< ConstraintDecl >( JavaConversions.asJavaCollection( Frontend.getTopLevelConstraints( m ) ) );

        for ( EntityDecl e : entities ) {
            JSONObject entity = entityToJSON( e, map );
            topDecls.put( entity );
        }
        for ( FunDecl e : functions ) {
            JSONObject function = functionToJSON( e, map );
            topDecls.put( function );
        }

        for ( ConstraintDecl c : constraints ) {
            JSONObject constraint = constraintToJSON( c, map );
            if ( constraint != null ) {
                topDecls.put( constraint );
            }
        }

        for ( PropertyDecl p : properties ) {
            JSONObject property = propertyToJSON( p, map );
            topDecls.put( property );
        }

        tree.put( "tree", topDecls );
        return tree;
    }


    protected abstract static class CaptureStdoutStderr {
        public abstract Object run();

        public Object result = null;

        PrintStream outPrintStream = null;
        PrintStream errPrintStream = null;

        public ByteArrayOutputStream baosOut = null;
        public ByteArrayOutputStream baosErr = null;

        public CaptureStdoutStderr() {
            baosOut = new ByteArrayOutputStream();
            baosErr = new ByteArrayOutputStream();
            outPrintStream = new PrintStream(baosOut);
            errPrintStream = new PrintStream(baosErr);
            captureRun();
        }

        protected void captureRun() {
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.out.flush();
            System.err.flush();
            System.setOut(outPrintStream);
            System.setErr(errPrintStream);
            result = run();
            System.out.flush();
            System.err.flush();
            System.setOut( oldOut );
            System.setErr( oldErr );
        }

        public CaptureStdoutStderr(String outFileName, String errFileName) {
            try {
                outPrintStream = new PrintStream( outFileName );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                baosOut = new ByteArrayOutputStream();
                outPrintStream = new PrintStream(baosOut);
            }
            try {
                errPrintStream = new PrintStream(errFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                baosErr = new ByteArrayOutputStream();
                errPrintStream = new PrintStream(baosErr);
            }
            captureRun();
        }

    }

    public static void main( String[] args ) {

//        PrintStream oldOut = System.out;
//        PrintStream oldErr = System.err;
//        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
//        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        String packageName = "generatedCode";
        String kToJavaOutLog = "kToJavaOut.log";
        String writeJavaOutLog = "writeJavaOut.log";

//        System.setOut(new PrintStream(baosOut));
//        System.setErr(new PrintStream(baosErr));

        Boolean containmentTree = false;
        Boolean errorInfo = false;
        Boolean translate = false;

        JSONObject json = new JSONObject();


        String kToExecute = "";
        Boolean areFiles = args.length > 0;
        for ( int i = 0; i < args.length; ++i ) {
            String arg = args[ i ];
            if ( arg.contains( "package" ) ) {
                ++i;
                continue;
            }
            if ( !arg.contains( "--" ) && !FileUtils.exists( arg ) ) {
                areFiles = false;
                break;
            }
        }

        for ( int i = 0; i < args.length; ++i ) {
            String arg = args[ i ];
            if ( !arg.contains( "--" ) ) {
                if ( areFiles ) {
                    try {
                        String k;
                        k = FileUtils.fileToString( arg );
                        kToExecute += k + "\n";
                    } catch ( FileNotFoundException e ) {
                        e.printStackTrace();
                    }
                } else {
                    kToExecute += arg + " ";
                }
            } else {
                if ( arg.contains( "tree" ) ) {
                    containmentTree = true;
                }
                if ( arg.contains( "solve" ) ) {
                    errorInfo = true;
                    translate = true;
                    containmentTree = true;
                }
                if ( arg.contains( "error" ) ) {
                    errorInfo = true;
                }
                if ( arg.contains( "package" ) ) {
                    packageName = args[ ++i ];
                }
            }
        }

        if ( !containmentTree && !errorInfo ) {
            containmentTree = true;
            translate = true;
            errorInfo = true;
        }

        if ( containmentTree ) {
            System.out.println( "===TREE===" );
            JSONObject tree = kToContainmentTree( kToExecute );
            System.out.println( tree.toString(4) );
            json.put("tree", tree );

        }

        if ( errorInfo ) {
            //KtoJava kToJava = new KtoJava( kToExecute, packageName, translate );
            KtoJava kToJava = null;
            final String kToExecuteC = kToExecute;
            final String packageNameC = packageName;
            final boolean translateC = translate;
//            String kToJavaErr = "kToJavaErr.log";
//            CaptureStdoutStderr c = new CaptureStdoutStderr(kToJavaOut, kToJavaErr) {
            CaptureStdoutStderr c = new CaptureStdoutStderr() {
                @Override
                public Object run() {
                    return new KtoJava( kToExecuteC, packageNameC, translateC );
                }
            };
            kToJava = (KtoJava)c.result;
            String out = c.baosOut.toString();
            FileUtils.stringToFile(out, kToJavaOutLog);

            Boolean typeCheckCompleted =
                    !c.baosErr.toString().contains( "Type Check" );
            // Add errors to JSON
            if ( !typeCheckCompleted ) {
                JSONArray jarr = new JSONArray();
                jarr.put(c.baosErr.toString());
                json.put("errors", jarr);
            }

            // Syntax errors not working?
            List<String> syntaxErrorList = syntaxErrors(c.baosErr);
            String syntaxErrors = String.join( ",", syntaxErrorList );
            System.out.println( "===ERRORS===" );

            StringBuffer sb = new StringBuffer();

            sb.append( "Syntax Errors: "
                       + ( syntaxErrors.isEmpty() ? "None" : syntaxErrors )
                       + "\n" );
            // Add syntax errors to JSON
            if ( !syntaxErrorList.isEmpty() ) {
                JSONArray jarr = new JSONArray();
                for (String se : syntaxErrorList) {
                    jarr.put(se);
                }
                json.put("Syntax Errors", jarr);
            }

            if ( !typeCheckCompleted ) {
                sb.append( "Input k did not type check\n" );
            }
//            System.out.flush();
//            System.setOut( oldOut );
//            System.setErr( oldErr );
            System.out.println( sb );

            if ( translate ) {
                //kToJava.writeFiles( kToJava, "/Users/bclement/git/kservices" );
                final KtoJava k2j = kToJava;
                c = new CaptureStdoutStderr() {
                    @Override
                    public Object run() {
                        k2j.writeFiles( k2j, "/Users/bclement/git/kservices" );
                        return null;
                    }
                };
                if ( c.baosErr.toString().length() > 3 ) {
                    JSONArray jarr = json.getJSONArray("errors");
                    if (jarr == null) jarr = new JSONArray();
                    jarr.put(c.baosErr);
                    json.put("errors", jarr);
                }
                String outWrite = c.baosOut.toString();
                FileUtils.stringToFile(outWrite, writeJavaOutLog);
            }

        }

    }

    public static List< String > syntaxErrors( ByteArrayOutputStream baos ) {
        return syntaxErrors(baos.toString() );
    }
    public static List< String > syntaxErrors( String baosString ) {
        List< String > errors = new ArrayList< String >();
        Pattern errorPattern = Pattern.compile( "[0-9]+:[0-9]+" );
        Matcher m = errorPattern.matcher( baosString );
        while ( m.find() ) {
            errors.add( m.group( 0 ) );
        }

        return errors;

    }

}
