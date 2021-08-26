package com.cas.core;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This is a wrapper class for an expression. It contains all the methods we
 * would need to access outside the data structure package
 *
 * @author Abd-El-Aziz Zayed
 */
public class Equation implements IMath {

    private final Expression root;
    private final HashSet<Character> variables;

    public Equation(Expression root, HashSet<Character> variables) {
        this.root = root;
        this.variables = variables;
    }

    public Equation(String exp, HashSet<Character> variables) {
        exp = Parser.clean(exp);
        System.out.println("parsed");
        root = Parser.parseExpression(exp, variables);
        System.out.println("generated: " + root.toFancyString());
        this.variables = variables;
    }

    /**
     * @return the variables
     */
    public HashSet<Character> getVariables() {
        return variables;
    }

    /**
     * get the value of the expression with the given values for the variables
     *
     * @param varValues - values of each variable
     * @return the value of the expression at the given values
     */
    public double valueAt(HashMap<Character, Double> varValues) {
        return root.evaluate(varValues);
    }

    @Override
    public String toLatex() {
        return root.toLatex();
    }

    @Override
    public String toFancyString() {
        return root.toFancyString();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    /**
     * compute a full simplification of this expression
     *
     * @return a fully simplified version of the root expression
     */
    public Equation simplified() {
        Expression simplified = root;
        Expression previous;

        do {
            previous = simplified;
            simplified = simplified.simplify();
        } while (!simplified.equals(previous));
        simplified = simplified.simplify();

        HashSet<Character> vars = new HashSet<>();
        String simple = simplified.toString();
        simple = Parser.clean(simple);
//		System.out.println(simple);
        Parser.parseExpression(simple, vars); // quick and dirty way to get the variables

        return new Equation(simplified, vars);
    }

    /**
     * compute a fully simplified version of the derivative of this equation
     *
     * @param var - variable we wish to differentiate with respect to
     * @return a fully simplified version of the derivative of this equation
     */
    public Equation derivative(char var) {
        Equation derivative = new Equation(root.differentiate(var), null);
        return derivative.simplified();
    }
}