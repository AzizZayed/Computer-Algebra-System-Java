package com.cas.core;

/**
 * this is an Enum type class to identify expressions: give them a name and an
 * order value to be sorted by. This can be used instead of the instanceof
 * operator when it is more practical than overridden methods.
 *
 * @author Abd-El-Aziz Zayed
 */
public enum ExpressionType {

    CONSTANT(0, "constant"), // number
    VARIABLE(1, "variable"), // variable

    POWER(2, "power"), // e^f and f^g
    FRACTION(3, "fraction"), // f / g
    LOGARITHM(4, "logarithm"), // ln(f) or log_f(g)

    ABSOLUTE_VALUE(5, "abs"), // abs(f) or |f|
    FLOOR(6, "floor"), // floor(f) or [f]
    CEILING(7, "ceil"), // ceil(f)

    // basic trig functions
    SIN(8, "sin"),
    COS(9, "cos"),
    TAN(10, "tan"),

    // reciprocal trig functions
    CSC(11, "csc"),
    SEC(12, "sec"),
    COT(13, "cot"),

    // inverse trig
    ARCSIN(14, "arcsin"),
    ARCCOS(15, "arccos"),
    ARCTAN(16, "arctan"),

    SIGN(17, "sign"),
    MODULUS(18, "mod"),

    MAX(19, "max"),
    MIN(20, "min"),

    PRODUCT(21, "product"),
    SUM(22, "sum");

    private final String name;
    private final int order;

    /*
     * constructor with all fields
     */
    ExpressionType(int order, String name) {
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return name;
    }
}