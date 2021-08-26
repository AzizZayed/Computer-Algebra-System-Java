package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

public class Sign extends FixedInputFunction {

    public Sign(Expression expr) {
        super(ExpressionType.SIGN, expr);
    }

    @Override
    public String toLatex() {
        return type + "\\left(" + expr.toLatex() + "\\right)";
    }

    @Override
    public String toFancyString() {
        return type + "(" + expr.toFancyString() + ")";
    }

    @Override
    public String toString() {
        return type + "(" + expr + ")";
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return FastMath.signum(expr.evaluate(varValues));
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Sign)
            return expr.equals(((Sign) e).expr);
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        throw new IllegalArgumentException("Cannot differentiate sign. Compute numerically instead.");
    }

    @Override
    public Expression simplify() {
        if (expr instanceof Constant)
            return new Constant(FastMath.signum(((Constant) expr).getValue()));
        return new Sign(expr.simplify());
    }
}