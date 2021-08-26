package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

/**
 * functioned defined with brackets like the floor function and the ceiling
 * function. This parent class is used for all functions that don't need to be
 * redefined every time, so we or mathematicians define a bracket notation to
 * refer to them
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class BracketFunction extends FixedInputFunction {

    private final char open;
    private final char closed; // open and close brackets for visual purposes

    /**
     * constructor for different open and close brackets
     */
    public BracketFunction(ExpressionType type, Expression expr, char open, char closed) {
        super(type, expr);
        this.open = open;
        this.closed = closed;
    }

    /**
     * constructor for similar open and close brackets
     */
    public BracketFunction(ExpressionType type, Expression expr, char bracket) {
        this(type, expr, bracket, bracket);
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return compute(expr.evaluate(varValues));
    }

    @Override
    public String toString() {
        return type + "(" + expr + ")";
    }

    @Override
    public String toFancyString() {
        return open + expr.toFancyString() + closed;
    }

    @Override
    public String toLatex() {
        return open + expr.toLatex() + closed;
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof BracketFunction) {
            BracketFunction func = (BracketFunction) e;
            if (open == func.open && closed == func.closed)
                return expr.equals(func.expr);
        }
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        return new Constant(0d);
    }

    /**
     * Common simplification between all bracket functions: when the input is a
     * constant, the operation can be computed and returned without needing the
     * bracket function
     *
     * @return a constant node with the result, null if the operation is impossible
     */
    protected Constant evaluate() {
        if (expr instanceof Constant) {
            Constant c = (Constant) expr;
            return new Constant(compute(c.getValue()));
        }
        return null;
    }

    /**
     * calculate the value of the function
     *
     * @param in - the input to the function
     * @return the value returned by the mathematical function
     */
    protected abstract double compute(double in);

    /**
     * the floor function
     */
    public static final class Floor extends BracketFunction {
        public Floor(Expression expr) {
            super(ExpressionType.FLOOR, expr, '\u230A', '\u230B');
        }

        @Override
        protected double compute(double in) {
            return FastMath.floor(in);
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Floor(expr.simplify()) : eval;
        }
    }

    /**
     * the ceiling function
     */
    public static final class Ceiling extends BracketFunction {
        public Ceiling(Expression expr) {
            super(ExpressionType.CEILING, expr, '\u2308', '\u2309');
        }

        @Override
        protected double compute(double in) {
            return FastMath.ceil(in);
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Ceiling(expr.simplify()) : eval;
        }
    }

    /**
     * the absolute value function
     */
    public static final class Abs extends BracketFunction {
        public Abs(Expression expr) {
            super(ExpressionType.ABSOLUTE_VALUE, expr, '|');
        }

        @Override
        protected double compute(double in) {
            return FastMath.abs(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // f/abs(f) * f'
                    new Fraction(expr, this), // f/abs(f)
                    expr.differentiate(var) // f'
            ); // end f/abs(f) * f'
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Abs(expr.simplify()) : eval;
        }
    }
}