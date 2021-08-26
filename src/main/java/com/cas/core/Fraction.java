package com.cas.core;

import net.jafama.FastMath;

import java.util.HashMap;

/**
 * class to represent a fraction: f(x) / g(x)
 *
 * @author Abd-El-Aziz Zayed
 */
public final class Fraction extends Expression {

    protected Expression numerator, denominator; // components of a fraction

    public Fraction(Expression num, Expression denom) {
        super(ExpressionType.FRACTION);
        numerator = num;
        denominator = denom;
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return numerator.evaluate(varValues) / denominator.evaluate(varValues);
    }

    @Override
    public String toString() {
        return "((" + numerator + ")/(" + denominator + "))";
    }

    @Override
    public String toFancyString() {
        return "(" + numerator.toFancyString() + "/" + denominator.toFancyString() + ")";
    }

    @Override
    public String toLatex() {
        return "\\dfrac{" + numerator.toLatex() + "}{" + denominator.toLatex() + "}";
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof Fraction) {
            Fraction frac = (Fraction) e;
            return numerator.equals(frac.numerator) && denominator.equals(frac.denominator);
        }
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        return new Fraction( // quotient rule
                Sum.create( // f'g - fg'
                        Product.create( // f'g
                                numerator.differentiate(var), // f'g
                                denominator // g
                        ), // end f'*g
                        Product.create( // -fg'
                                new Constant(-1d), // -1
                                numerator, // f
                                denominator.differentiate(var) // g'
                        ) // end -fg'
                ), // end f'g - fg'
                new Power( // g^2
                        denominator, // g
                        new Constant(2d) // 2
                ) // end g^2
        ); // end quotient rule
    }

    @Override
    public Expression simplify() {
        Expression sNum = numerator.simplify();
        Expression sDenom = denominator.simplify();

        boolean numIsFrac = sNum instanceof Fraction;
        boolean denomIsFrac = sDenom instanceof Fraction;

        /*
         * simplify complex fractions
         */
        if (numIsFrac && !denomIsFrac) { // (g / h) / f
            Fraction fNum = (Fraction) sNum;
            return new Fraction(fNum.numerator.simplify(), Product.create(fNum.denominator.simplify(), sDenom));
        }
        if (!numIsFrac && denomIsFrac) { // f / (g / h)
            Fraction fDenom = (Fraction) sDenom;
            return new Fraction(Product.create(sNum, fDenom.denominator.simplify()), fDenom.numerator.simplify());
        }
        if (numIsFrac && denomIsFrac) { // (g / h) / (f / i)
            Fraction fNum = (Fraction) sNum;
            Fraction fDenom = (Fraction) sDenom;
            return new Fraction( // (g * i) / (h * f)
                    Product.create(fNum.numerator.simplify(), fDenom.denominator.simplify()), // g * i
                    Product.create(fNum.denominator.simplify(), fDenom.numerator.simplify()) // h * f
            ); // end (g * i) / (h * f)
        }

        /*
         * simplify if there is sum in numerator
         */
//		if (sNum instanceof Sum && !(sDenom instanceof Sum)) {
//			Sum numSum = (Sum) sNum;
//			Fraction[] fracs = new Fraction[numSum.children.length];
//			for (int i = 0; i < fracs.length; i++)
//				fracs[i] = new Fraction(numSum.children[i].simplify(), sDenom);
//			return Sum.create(fracs).simplify();
//		}

        /*
         * simplify if constants
         */
        if (sNum instanceof Constant && sDenom instanceof Constant) {
            double result = ((Constant) sNum).getValue() / ((Constant) sDenom).getValue();
            if (result == FastMath.floor(result))
                return new Constant(result);
        }

        return Product.create(sNum, new Power(sDenom, new Constant(-1d)).simplify());
//		return new Fraction(sNum, sDenom);
    }
}