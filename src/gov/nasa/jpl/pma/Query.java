package gov.nasa.jpl.pma;

public interface Query< Model, Expression, VariableType, Result, Constraint, Objective >
        extends Problem< Model, Expression, VariableType, Constraint, Objective > {

    public Expression getExpression();
    public void setExpression( Expression expression );
    public Result evaluate();
}
