package gov.nasa.jpl.pma;

import java.util.Collection;

public interface Problem< Model, Expression, Variable, Constraint, Objective > {
    public Model getModel();
    public Model setModel();
    
    public Objective getObjective();
    public void setObjective( Objective objective );

    public Collection< Variable > getVariables();
    
    public Collection< Variable > getOpenVariables();
    public void setOpenVariables( Collection< Variable > variables );
    public void open( Variable variable );
    public void open( Collection< Variable > variables );

    public Collection< Variable > getFixedVariables();
    public void setFixedVariables( Collection< Variable > variables );
    public void fix( Variable variable );
    public void fix( Collection< Variable > variables );
    
    public Collection< Constraint > getConstraints();
    public void setConstraints( Collection< Constraint > constraints );
    public void addConstraint( Constraint constraints );
    public void addConstraints( Collection< Constraint > constraints );
    public boolean removeConstraint( Constraint constraints );
    public boolean removeConstraints( Collection< Constraint > constraints );
}
