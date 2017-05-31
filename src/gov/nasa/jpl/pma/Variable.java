package gov.nasa.jpl.kservices;

public interface Variable< T, Domain, Type > {
    
    public Type getType();
    public void getType( Type type );
    
    
}
