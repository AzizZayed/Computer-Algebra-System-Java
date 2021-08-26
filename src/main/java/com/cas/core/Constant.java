package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

/**
 * Class to represent any constant/number as a double. This is basically a fancy
 * wrapper for the double data type
 *
 * @author Abd-El-Aziz Zayed
 */
public final class Constant extends Expression {

    public static final Constant PI = new Constant(3.141592653589793d); // PI constant
    public static final Constant EXP = new Constant(2.718281828459045d); // Euler's number
    public static final Constant GOLDEN_RATIO = new Constant(1.618033988749895d); // phi constant

    private double value; // the value of the constant

    public Constant(double val) {
        super(ExpressionType.CONSTANT);
        value = val;
    }

    /**
     * @return the value of the constant
     */
    public double getValue() {
        return value;
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return value;
    }

    @Override
    public String toString() {
        return toFancyString();
    }

    @Override
    public String toFancyString() {
        if (this == PI || value == PI.value)
            return "\u03C0";
        if (this == EXP || value == EXP.value)
            return "e";
        if (this == GOLDEN_RATIO || value == GOLDEN_RATIO.value)
            return "\u03D5";

        String strValue = Double.toString(value);
        if (value == FastMath.floor(value)) // check if integer and if so, display 2 instead of 2.0 (example)
            return strValue.substring(0, strValue.indexOf('.'));
        return strValue;
    }

    @Override
    public String toLatex() {
        if (this == PI || value == PI.value)
            return "\\pi";
        if (this == EXP || value == EXP.value)
            return "e";
        if (this == GOLDEN_RATIO || value == GOLDEN_RATIO.value)
            return "\\varphi";

        String strValue = Double.toString(value);
        if (value == FastMath.floor(value)) // check if integer and if so, display 2 instead of 2.0
            return strValue.substring(0, strValue.indexOf('.'));
        return strValue;
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Constant)
            return value == (((Constant) e).value);
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        return new Constant(0d);
    }

    @Override
    public Expression simplify() {
        return this;
    }
}