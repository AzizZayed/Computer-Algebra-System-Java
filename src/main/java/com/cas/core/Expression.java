package com.cas.core;

import java.util.HashMap;

/**
 * class for an arbitrary mathematical expression. All mathematical expression
 * classes are subclasses of this class.
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class Expression implements IMath {

    protected ExpressionType type; // the type of math expression / function, ex: log, exponential, trig

    /**
     * constructor with expression type
     *
     * @param type - type of expression
     */
    public Expression(ExpressionType type) {
        this.type = type;
    }

    public ExpressionType getType() {
        return type;
    }

    /**
     * calculate the value of the mathematical expression
     *
     * @param varValues - map with all the values of each variable in the expression
     * @return the value of the expression
     */
    public abstract double evaluate(HashMap<Character, Double> varValues);

    /**
     * check if a given expression is equal to the current, this simply checks if
     * both trees are the same in memory and does not check if they are
     * mathematically equivalent. Mathematical equivalence is a whole other story.
     *
     * @param e - given expression to check equality with
     * @return true if the expressions are equal, false otherwise
     */
    public abstract boolean equals(Expression e);

    /**
     * differentiate the expression with respect to the given variable
     *
     * @param var - variable to differentiate with respect to
     * @return the derivative of the expression with respect to the given variable
     */
    public abstract Expression differentiate(char var);

    /**
     * @return a simplified version of the first expression, but equivalent
     */
    public abstract Expression simplify();
}