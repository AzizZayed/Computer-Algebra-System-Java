package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

/**
 * logarithmic functions: log_f(x)[ g(x) ]
 *
 * @author Abd-El-Aziz Zayed
 */
public class Log extends FixedInputFunction {

    protected Expression base; // the expression for the base of the logarithm

    /*
     * constructor with base expression if it's custom, so like log base 5
     */
    public Log(Expression base, Expression expr) {
        super(ExpressionType.LOGARITHM, expr);
        this.base = base;
    }

    /*
     * constructor with base 10
     */
    public Log(Expression expr) {
        this(new Constant(10.0d), expr);
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return FastMath.log(expr.evaluate(varValues)) / FastMath.log(base.evaluate(varValues));
    }

    @Override
    public String toString() {
        return "log_(" + base + ")_(" + expr + ")";
    }

    @Override
    public String toFancyString() {
        if (needsBrackets())
            if (base instanceof Variable || base instanceof Constant)
                return "log_" + base.toFancyString() + "(" + expr.toFancyString() + ")";
            else
                return "log_(" + base.toFancyString() + ")(" + expr.toFancyString() + ")";
        return "log_" + base.toFancyString() + " " + expr.toFancyString();
    }

    @Override
    public String toLatex() {
        if (needsBrackets())
            return "log_{" + base.toLatex() + "}{\\left(" + expr.toLatex() + "\\right)}";
        return "log_{" + base.toLatex() + "}{" + expr.toLatex() + "}";
    }

    /**
     * @return true if the expression needs to be printed with surrounding brackets,
     * false if not
     */
    protected boolean needsBrackets() {
        return expr instanceof Operator || expr instanceof Power || expr instanceof Fraction || expr instanceof Log;
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Log) {
            Log log = (Log) e;
            return expr.equals(log.expr) && base.equals(log.base);
        }
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        boolean baseIsNumber = base instanceof Constant;
        boolean inputIsNumber = expr instanceof Constant;

        if (baseIsNumber && inputIsNumber) // case log_b(k), where b and k are both numbers (constants)
            return new Constant(0d);

        if (baseIsNumber && !inputIsNumber) {// case log_b( f(x) ) where b is a numbers (constants)
            return Product.create( // f' * [ f * lnb ]^(-1)
                    expr.differentiate(var), // f'
                    new Power( // [f * lnb]^(-1)
                            Product.create( // f * lnb
                                    expr, // f
                                    new Ln(base) // lnb
                            ), // end f * lnb
                            new Constant(-1d) // -1
                    ) // end [f * lnb]^(-1)
            ); // end f' * [ f * lnb ]^(-1)
        }

        // otherwise: case log_(g(x))(f(x))
        return new Fraction( // derivative of ln(f(x)) / ln(g(x))
                new Ln(expr), // ln(f(x))
                new Ln(base) // ln(g(x))
        ) // end of ln(f(x)) / ln(g(x))
                .differentiate(var); // end derivative of ln(f(x)) / ln(g(x))
    }

    @Override
    public Expression simplify() {
        if (expr.equals(base))
            return new Constant(1d);
        if (expr instanceof Power) {
            Power pow = (Power) expr;
            if (pow.expr.equals(base))
                return pow.power;
        }
        if (expr instanceof Constant)
            if (((Constant) expr).getValue() == 1d)
                return new Constant(0d);

        return new Log(base.simplify(), expr.simplify());
    }

    /*
     * natural logarithm
     */
    public static final class Ln extends Log {
        public Ln(Expression expr) {
            super(Constant.EXP, expr);
        }

        @Override
        public String toString() {
            return "ln(" + expr + ")";
        }

        @Override
        public String toFancyString() {
            if (needsBrackets())
                return "ln(" + expr.toFancyString() + ")";
            return "ln" + expr.toFancyString();
        }

        @Override
        public String toLatex() {
            if (needsBrackets())
                return "ln\\left(" + expr.toLatex() + "\\right)";
            return "ln" + expr.toLatex();
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // f' * f^(-1)
                    expr.differentiate(var), // f'
                    new Power( // f^(-1)
                            expr, // f
                            new Constant(-1d) // -1
                    ) // end f^(-1)
            ); // end f' * f^(-1)
        }

        @Override
        public Expression simplify() {
            if (expr.equals(base))
                return new Constant(1d);
            if (expr instanceof Power) {
                Power pow = (Power) expr;
                if (pow.expr.equals(base))
                    return pow.power;
            }
            if (expr instanceof Constant) {
                Constant c = (Constant) expr;
                if (c.getValue() == 1d)
                    return new Constant(0d);
            }

            return new Ln(expr.simplify());
        }
    }
}