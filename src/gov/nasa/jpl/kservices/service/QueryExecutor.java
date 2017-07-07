package gov.nasa.jpl.kservices.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.kservices.KToAe;
import gov.nasa.jpl.kservices.sysml2k.S2KParseException;
import gov.nasa.jpl.kservices.sysml2k.S2KLearner;
import gov.nasa.jpl.kservices.sysml2k.S2KLearner.TranslationMap;
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
        KToAe k2ae = new KToAe( k  );

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

    public static void main( String[] args ) {
  		try {
  		  // TODO add code for testing
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
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

    
}
