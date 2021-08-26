package com.cas.core;

import java.util.HashMap;

public class Mod extends FixedInputFunction {

    protected Expression divisor;

    public Mod(Expression in1, Expression in2) {
        super(ExpressionType.MODULUS, in1);
        divisor = in2;
    }

    @Override
    public String toString() {
        return type + "(" + expr + "," + divisor + ")";
    }

    @Override
    public String toFancyString() {
        return type + "(" + expr.toFancyString() + "," + divisor.toFancyString() + ")";
    }

    @Override
    public String toLatex() {
        return type + "\\left(" + expr.toLatex() + "," + divisor.toLatex() + "\\right)";
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return expr.evaluate(varValues) % divisor.evaluate(varValues);
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Mod) {
            Mod mod = (Mod) e;
            return expr.equals(mod.expr) && divisor.equals(mod.divisor);
        }
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        throw new IllegalArgumentException("Cannot differentiate modulus. Compute numerically instead.");
    }

    @Override
    public Expression simplify() {
        return new Mod(expr.simplify(), divisor.simplify());
    }
}