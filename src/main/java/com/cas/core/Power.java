package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

/**
 * class representing exponentials with any base and power expression
 *
 * @author Abd-El-Aziz Zayed
 */
public class Power extends FixedInputFunction {

    protected Expression power; // the expression in the exponent

    /**
     * constructor, example: a^b : a is the base and b is the power/exponent
     *
     * @param base  - expression at the base
     * @param power - expression in the exponent
     */
    public Power(Expression base, Expression power) {
        super(ExpressionType.POWER, base);
        this.power = power;
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return FastMath.pow(expr.evaluate(varValues), power.evaluate(varValues));
    }

    @Override
    public String toString() {
        return "(" + expr + ")^(" + power + ")";
    }

    @Override
    public String toFancyString() {
        if (power instanceof Constant) {
            Constant constant = (Constant) power;
            if (constant.getValue() == 0.5d)
                return "sqrt(" + expr.toFancyString() + ")";
        }

        if (needsBrackets()) {
            if (powerNeedsBrackets())
                return "(" + expr.toFancyString() + ")^(" + power.toFancyString() + ")";
            else
                return "(" + expr.toFancyString() + ")^" + power.toFancyString();
        } else {
            if (powerNeedsBrackets())
                return expr.toFancyString() + "^(" + power.toFancyString() + ")";
            else
                return expr.toFancyString() + "^" + power.toFancyString();
        }

    }

    @Override
    public String toLatex() {
        if (power instanceof Constant) {
            Constant constant = (Constant) power;
            if (constant.getValue() == 0.5d)
                return "\\sqrt{" + expr.toLatex() + "}";
        }

        if (needsBrackets())
            return "\\left(" + expr.toLatex() + "\\right)^{" + power.toLatex() + "}";
        else
            return expr.toLatex() + "^{" + power.toLatex() + "}";
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Power) {
            Power pow = (Power) e;
            return expr.equals(pow.expr) && power.equals(pow.power);
        }
        return false;
    }

    /**
     * @return if the input needs surrounding brackets
     */
    private boolean needsBrackets() {
        return !(expr instanceof Variable || expr instanceof Constant || expr instanceof BracketFunction);
    }

    /**
     * @return if the exponent needs surrounding brackets
     */
    private boolean powerNeedsBrackets() {
        return power instanceof Fraction || power instanceof Operator || power instanceof Power;
    }

    @Override
    public Expression differentiate(char var) {
        boolean baseIsNumber = expr instanceof Constant;
        boolean powerIsNumber = power instanceof Constant;

        if (baseIsNumber && powerIsNumber) // case b^k, where b and k are both numbers (constants)
            return new Constant(0d);

        if (!baseIsNumber && powerIsNumber) // case [ f(x) ]^k, where k is a constant
            return Product.create( // k*f^(k-1)*f'
                    power, // k
                    expr.differentiate(var), // f'
                    new Power( // f^(k - 1)
                            expr, // f
                            new Constant(((Constant) power).getValue() - 1d) // end f^(k - 1)
                    ) // end f^(k - 1)
            ); // end k*f^(k-1)*f'

        if (baseIsNumber && !powerIsNumber) // case k^[ f(x) ], where k is a constant
            return Product.create( // k^f * lnk + f'
                    this, // a^f
                    power.differentiate(var), // f'
                    new Log.Ln(expr) // lnk
            ); // end k^f * lnk + f'

        // otherwise: case [ f(x) ]^[ g(x) ], here we use the generalized power rule
        return Product.create( // [f(x)]^[g(x)] * ( g'*lnf + g*f'*(f)^(-1) )
                this, // [f(x)]^[g(x)]
                Sum.create( // g'*lnf + g*f'*(f)^(-1)
                        Product.create( // g'*lnf
                                power.differentiate(var), // g'
                                new Log.Ln(expr) // lnf
                        ), // end g'*lnf
                        Product.create( // g*f'*(f)^(-1)
                                power, // g
                                expr.differentiate(var), // f'
                                new Power( // (f)^(-1)
                                        expr, // f
                                        new Constant(-1d) // -1
                                ) // end (f)^(-1)
                        ) // end g*f'*(f)^(-1)
                ) // end g'*lnf + g*f'*(f)^(-1)
        ); // end [f(x)]^[g(x)] * ( g'*lnf + g*f'*(f)^(-1) )
    }

    @Override
    public Expression simplify() {
        if (expr instanceof Fraction) {
            Fraction inner = (Fraction) expr;
            return new Fraction(new Power(inner.numerator, power), new Power(inner.denominator, power));

        } else if (expr instanceof Power) {
            Power inner = (Power) expr;
            return new Power(inner.expr, Product.create(power, inner.power));

        } else if (expr instanceof Product) {

            Product inner = (Product) expr;
            Expression[] exps = new Expression[inner.children.length];
            for (int i = 0; i < exps.length; i++)
                exps[i] = new Power(inner.children[i], power);

            Expression prod = Product.create(exps);
            return prod;

        } else if (power instanceof Log) {
            Log log = (Log) power;
            if (log.base.equals(expr))
                return log.expr;

        } else if (power instanceof Constant) {
            Constant pow = (Constant) power;
            if (expr instanceof Constant) {
                Constant base = (Constant) expr;
                double res = FastMath.pow(base.getValue(), pow.getValue());
                if (FastMath.floor(res) == res && res < 1000d)
                    return new Constant(res);
            }
            if (pow.getValue() == 0d)
                return new Constant(1d);
            if (pow.getValue() == 1d)
                return expr;
        }
        return new Power(expr.simplify(), power.simplify());
    }

    /**
     * @return true if the exponent is negative
     */
    protected boolean hasNegativeExponent() {
        if (power instanceof Constant)
            return (((Constant) power).getValue() < 0);
        if (power instanceof Product) {
            Product prod = (Product) power;
            if (prod.children[0] instanceof Constant)
                return (((Constant) prod.children[0]).getValue() < 0);
        }
        return false;
    }

    /**
     * @return this expression but with a negated power
     */
    protected Expression toDenominator() {
        return new Power(expr, Product.create(new Constant(-1d), power));
    }

    /*
     * natural exponential with e, so e^x, where x is any expression
     */
    public static final class Exp extends Power {
        public Exp(Expression power) {
            super(Constant.EXP, power);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // e^(f) * f'
                    this, // e^(f)
                    power.differentiate(var) // f'
            ); // end e^(f) * f'
        }

        @Override
        public Expression simplify() {
            if (power instanceof Constant) {
                Constant pow = (Constant) power;
                if (pow.getValue() == 0d)
                    return new Constant(1d);
                if (pow.getValue() == 1d)
                    return expr;
            } else if (power instanceof Log) {
                Log log = (Log) power;
                if (log.base.equals(expr))
                    return log.expr;
            }

            return new Exp(power.simplify());
        }
    }
}