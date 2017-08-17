package gov.nasa.jpl.kservices.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

//import generatedCode.Main;
//import generatedCode.Main;
import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.kservices.KToAe;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.Pair;
import k.frontend.Exp;
import k.frontend.Annotation;
import k.frontend.EntityDecl;
import k.frontend.EntityToken;
import k.frontend.Frontend;
import k.frontend.MemberDecl;
import k.frontend.Type;
import k.frontend.TypeParam;
import scala.Option;
import scala.collection.immutable.List;
import sysml.SystemModel;


public class QueryExecutor< Model extends SystemModel<?,?,?,?,?,?,?,?,?,?,?> > implements sysml.ProblemSolver<String,String,String,String,String,String,String,String,String,String,String>{

    public Model model;
    public Exp kExpression;
    
    /**
     * Update the model with the input k definitions and instances.
     * @param k
     * @return
     */
    public Result<String> updateK( String k ) {
		// TODO  --  Maybe this goes in a different class?
        List<Annotation> annotations = null;
		EntityToken token = null;
		Option<String> keyword = null;
		String ident = null;
		List<TypeParam> typeParams = null;
		List<Type> extending = null;
		List<MemberDecl> members = null;
    	EntityDecl e = new EntityDecl(annotations, token, keyword, ident, typeParams, extending, members);
        return null;
    }

    /**
     * Update the model with the input json definitions and instances.
     * @param json
     * @return
     */
    public Result<String> updateJson( String json ) {
        // TODO -- To handle CRUD should have a sysml json interface or pass through to another server.
        String k = Frontend.json2exp2( json );
        Result<String> result = updateK( k );
        return result;
    }

    /**
     * Evaluate the input k expression and return the result.
     * @param k the input k expression to be evaluated
     * @return a Result object with evaluation result as as string and with any errors.
     */
    public Result<String> kQuery( String k ) {
        KToAe k2ae = new KToAe();

        Object expr = k2ae.astToAeExpr( k, null, true, true, true, true, null );

        Object value = null;
        ArrayList<String> errors = null;

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        
        String error = null;
        try {
            value = Expression.evaluateDeep( expr, null, true, false );
        } catch ( ClassCastException e ) {
            e.printStackTrace( writer );
        } catch ( IllegalAccessException e ) {
            e.printStackTrace( writer );
        } catch ( InvocationTargetException e ) {
            e.printStackTrace( writer );
        } catch ( InstantiationException e ) {
            e.printStackTrace( writer );
        }
        error = stringWriter.toString();
        if (error != null && error.length() > 0) {
            errors = new ArrayList<String>();
            errors.add( error );
        }
        Result<String> result = 
                new Result< String >( errors, "" + value, String.class );
        try {
            writer.close();
            stringWriter.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Evaluate the input json expression and return the result.
     * @param json
     * @return
     */
    public Result<String> jsonQuery( String json ) {
        String k = Frontend.json2exp2( json );
        Result<String> result = kQuery( k );
        return result;
    }

    @Override
    public String getDomainConstraint( String element, String version,
                                       String workspace ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addConstraint( String constraint, String version,
                               String workspace ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addDomainConstraint( String constraint, String version,
                                     Set< String > valueDomainSet,
                                     String workspace ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addDomainConstraint( String constraint, String version,
                                     Pair< String, String > valueDomainRange,
                                     String workspace ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void relaxDomain( String constraint, String version,
                             Set< String > valueDomainSet, String workspace ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void relaxDomain( String constraint, String version,
                             Pair< String, String > valueDomainRange,
                             String workspace ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection< String > getConstraintsOfElement( String element,
                                                         String version,
                                                         String workspace ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection< String >
           getViolatedConstraintsOfElement( String element, String version ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOptimizationFunction( Method method, Object... arguments ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Number getScore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean fixConstraintViolations( String element, String version ) {
        // TODO Auto-generated method stub
        return false;
    }

    protected static void f() {
        try {
            // Connect to the named pipe
            RandomAccessFile pipe = new RandomAccessFile(
                "input", "r"); // "r" or "w" or for bidirectional (Windows only) "rw"
         
            String req = "Request text";
         
            // Write request to the pipe
            //pipe.write(req.getBytes());
         
            // Read response from pipe
            String res = pipe.readLine();
         
            // Close the pipe
            pipe.close();
         
            // do something with res
         
        } catch (Exception e) {
            // do something
        }
    }
    
    protected static void readInputFile() {
        try{
            InputStream fis=new FileInputStream("input");
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
               System.out.println(line);
            }

            br.close();
        }
        catch(Exception e){
            System.err.println("Error: Target File Cannot Be Read");
        }
    }
    
    public static void main( String[] args ) {
        StringBuffer k = new StringBuffer();
        
        // Connect to the named pipe
        RandomAccessFile pipe;
        try {
            pipe = new RandomAccessFile("input", "r");
            while (true) {
                // read input file
                // Read response from pipe
                String line = pipe.readLine();
                if ( line == null ) {
//                    try {
//                        Thread.sleep( 1000 );
//                    } catch ( InterruptedException e ) {
//                        e.printStackTrace();
//                    }
                    continue;
                }
                try {
                //readInputFile();
                System.out.println(line);
                
                // execute command
                if ( line.trim().startsWith( "add" ) ) {
                    String kToAdd = line.trim().substring( 4 );
                    k.append( "\n" + kToAdd );

                    KtoJava kToJava = new KtoJava( k.toString(), "generatedCode", false );
                    kToJava.writeFiles( kToJava, "/Users/ayelaman/git/kservices" );
                }
                if ( line.trim().startsWith( "solve" ) ) {
//                    Main scenario = new Main();
//                    scenario.satisfy(true, null);
//                    System.out.println( ( scenario.isSatisfied( true, null ) ? "Satisfied"
//                                                                             : "Not Satisfied" )
//                                        + "\n" + scenario.executionToString() );
                }
                
                // output result
                } catch (Throwable e) {
					e.printStackTrace();
				}
            }
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // "r" or "w" or for bidirectional (Windows only) "rw"
        catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public Result<String> P( String k ) {
        KToAe k2ae = new KToAe();
        Object expr = k2ae.astToAeExpr( k, null, true, true, true, true, null );

        Object value = null;
        ArrayList<String> errors = null;

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        String error = null;
        try {
            value = Expression.evaluateDeep( expr, null, true, false );
        } catch ( ClassCastException e ) {
            e.printStackTrace( writer );
        } catch ( IllegalAccessException e ) {
            e.printStackTrace( writer );
        } catch ( InvocationTargetException e ) {
            e.printStackTrace( writer );
        } catch ( InstantiationException e ) {
            e.printStackTrace( writer );
        }
        error = stringWriter.toString();
        if (error != null && error.length() > 0) {
            errors = new ArrayList<String>();
            errors.add( error );
        }
        Result<String> result =
                        new Result< String >( errors, "" + value, String.class );
        try {
            writer.close();
            stringWriter.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        
        return result;
    }

}
