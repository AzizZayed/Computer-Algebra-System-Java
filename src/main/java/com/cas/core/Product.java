package com.cas.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * a class representing a finite product
 *
 * @author Abd-El-Aziz Zayed
 */
public class Product extends Operator implements IMath {

//	private static final ExpressionSorter SORTER = new ExpressionSorter(false); // sorter

    protected Product(Expression... expressions) {
        super(ExpressionType.PRODUCT, '*', expressions);
        Arrays.sort(children, ExpressionSorter.DEFAULT);
    }

    private Product(HashSet<Character> vars, String... strExpression) {
        super(ExpressionType.PRODUCT, '*', vars, strExpression);
        Arrays.sort(children, ExpressionSorter.DEFAULT);
    }

    /**
     * function to create an expression node with the given children as strings,
     * they still need to be parsed. The point of this function is to perform early
     * refactoring to make life easier later on. It does not have to return a
     * product node if the refactored expression does not require one
     *
     * @param vars          - the set of all variables
     * @param strExpression - the array of strings to be parsed into expressions
     * @return the refactored expression
     */
    public static Expression create(HashSet<Character> vars, String... strExpression) {
        if (strExpression.length == 0)
            return null;
        if (strExpression.length == 1)
            return Parser.generateExpression(strExpression[0], vars);
        return new Product(vars, strExpression);
    }

    /**
     * function to create expression node from the given expressions. The point of
     * this function is to perform early refactoring and simplification to make life
     * easier later on. It does not have to return a product node if the simplified
     * expression does not require one
     *
     * @param expressions - array of expressions
     * @return simplified and refactored expression
     */
    public static Expression create(Expression... expressions) {
//		System.out.println(Arrays.toString(expressions));
        if (expressions.length == 0)
            return null;
        if (expressions.length == 1)
            return expressions[0];

        // remove all the 1s and if there is a zero, return a zero constant
        ArrayList<Expression> valid = new ArrayList<>();
        Constant c0 = new Constant(0d), c1 = new Constant(1d);
        for (int i = 0; i < expressions.length; i++) {
            if (expressions[i].equals(c0))
                return c0;
            if (!expressions[i].equals(c1))
                valid.add(expressions[i]);
        }

        if (valid.isEmpty())
            return c1;
        if (valid.size() == 1)
            return valid.get(0);
        if (valid.size() == 2)
            if (valid.get(0) instanceof Constant && valid.get(1) instanceof Constant) {
                Constant a = (Constant) valid.get(0);
                Constant b = (Constant) valid.get(1);
                return new Constant(a.getValue() * b.getValue());
            }

        Collections.sort(valid, ExpressionSorter.DEFAULT);

        /*
         * expand into sums if appropriate, we only expand if there is a single sum, it
         * is a maximum of 2 expressions long and there is a maximum of 1 expressions to
         * distribute
         */
        int numberOfSums = 0;
        int index = 0, length = 0;
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression exp = valid.get(i);
            if (exp instanceof Sum) {
                numberOfSums++;
                index = i;
                length = ((Sum) exp).children.length;
            }
        }
        if (numberOfSums == 1 && length == 2 && valid.size() < length + 1) {
            Sum sum = (Sum) valid.get(index);
            Expression e1 = new Product(valid.get(0), sum.children[0]);
            Expression e2 = new Product(valid.get(0), sum.children[1]);
            return Sum.create(e1, e2);
        }

        /*
         * transform fractions into products
         */
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression exp = valid.get(i);
            if (exp instanceof Fraction) {
                Fraction frac = (Fraction) exp;
                valid.add(frac.numerator);
                if (frac.denominator instanceof Power) {
                    Power denom = (Power) frac.denominator;
                    valid.add(new Power(denom.expr, create(new Constant(-1d), denom.power)));
                } else {
                    valid.add(new Power(frac.denominator, new Constant(-1d)));
                }
                valid.remove(i);
            }
        }
//		for (int i = 0; i < valid.size(); i++) {
//			valid.set(i, valid.get(i).simplify());
//		}
//		System.out.println("prod: " + valid);

        /*
         * extract inner products if any
         */
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression exp = valid.get(i);
            if (exp instanceof Product) {
                Product prod = (Product) exp;
                for (int j = 0; j < prod.children.length; j++)
                    valid.add(prod.children[j]);
                valid.remove(i);
            }
        }

        ArrayList<Expression> grouped = new ArrayList<>(); // grouped expressions

        /*
         * group constant
         */
        double total = 1d;
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression expression = valid.get(i);
            if (expression instanceof Constant) {
                total *= ((Constant) expression).getValue();
                valid.remove(i);
            }
        }
        if (total != 1d)
            grouped.add(new Constant(total));

        /*
         * transform everything into a power to make simplifications easier
         */
        for (int i = 0; i < valid.size(); i++) {
            Expression exp = valid.get(i);
            if (exp instanceof Power)
                valid.set(i, exp.simplify());
            else
                valid.set(i, new Power(exp.simplify(), new Constant(1d)));
        }

        /*
         * group powers
         */
        ArrayList<Power> powers = new ArrayList<>();
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression expression = valid.get(i);
            if (expression instanceof Power) {
                powers.add((Power) expression);
                valid.remove(i);
            }
        }
        // add powers for common bases: a^x * a^y = a^(x + y)
        for (int i = powers.size() - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                Power p1 = powers.get(i);
                Power p2 = powers.get(j);
                if (p1.expr.equals(p2.expr)) {
                    p1.power = Sum.create(p1.power, p2.power); // add powers
                    powers.remove(j);
                    i--;
                }
            }
        }

        grouped.addAll(powers);

        /*
         * rest of the expressions that were not eligible for simplifications
         */
        grouped.addAll(valid);

        if (grouped.isEmpty()) {
            System.out.println("Empty");
            return new Constant(1d);
        }

        // final simplification of every expression
        for (int i = 0; i < grouped.size(); i++)
            grouped.set(i, grouped.get(i).simplify());

        Collections.sort(grouped, ExpressionSorter.DEFAULT); // final sort

        /*
         * group denominator and numerators if possible
         */
        ArrayList<Expression> denoms = new ArrayList<>(); // denominators
        for (int i = grouped.size() - 1; i >= 0; i--) {
            Expression exp = grouped.get(i);
            if (exp instanceof Power) {
                Power pow = (Power) exp;
                if (pow.hasNegativeExponent()) {
                    denoms.add(pow.toDenominator().simplify());
                    grouped.remove(i);
                }
            }
        }

        // create a fraction if possible
        if (!denoms.isEmpty()) {
            Expression[] num = grouped.toArray(new Expression[0]);
            Expression[] denom = denoms.toArray(new Expression[0]);
            if (num.length == 0)
                return new Fraction(new Constant(1d), Product.create(denom));
            return new Fraction(Product.create(num), Product.create(denom));
        }

        // final expression
        return new Product(grouped.toArray(new Expression[0]));
    }

    @Override
    protected double operate(double a, double b) {
        return a * b;
    }

    @Override
    protected double neutral() {
        return 1d;
    }

    @Override
    protected void print(int index, StringBuilder builder, boolean latex) {
        Constant constant = null;
        if (children[index] instanceof Constant)
            constant = (Constant) children[index];

        if (constant != null && constant.getValue() == -1d)
            builder.append('-');
        else
            builder.append(latex ? children[index].toLatex() : children[index].toFancyString());
    }

    @Override
    protected boolean needsBrackets(Expression e) {
        return e instanceof Sum;
    }

    @Override
    public Expression differentiate(char var) { // product rule for n functions

        /*
         * special case for something like k*f(x) -> k*f'(x)
         */
        if (children.length == 2)
            if (children[0] instanceof Constant)
                return Product.create( // k*f'(x)
                        children[0], // k
                        children[1].differentiate(var) // f'
                ); // end k*f'(x)

        /*
         * (f(x)g(x)h(x))' = f'gh + fg'h + fgh'--------------------------
         * (f(x)g(x)h(x)i(x)) = f'ghi + fg'hi + fgh'i + fghi'------------
         */
        Expression[] sums = new Expression[children.length];

        for (int i = 0; i < sums.length; i++) {
            Expression[] products = new Expression[children.length];
            for (int j = 0; j < products.length; j++) {
                if (i == j)
                    products[j] = children[j].differentiate(var);
                else
                    products[j] = children[j];
            }
            sums[i] = Product.create(products);
        }
        return Sum.create(sums);
    }

    @Override
    public Expression simplify() {
        return create(simplifiedChildren());
    }

    /**
     * @return this product node with the constants removed if any
     */
    public Product removedConstant() {
        int start = 0, length = children.length;
        if (children[0] instanceof Constant) {
            start = 1;
            length--;
        }

        Expression[] exps = new Expression[length];
        for (int i = start; i < children.length; i++)
            exps[i - start] = children[i];
        return new Product(exps);
    }

    /**
     * @return this product node with an added constant if there are none
     */
    public Product addedConstant() {
        int start = 0, length = children.length;
        if (!(children[0] instanceof Constant)) {
            start = 1;
            length++;
        }

        Expression[] exps = new Expression[length];
        if (start == 1)
            exps[0] = new Constant(1d);
        for (int i = 0; i < children.length; i++)
            exps[i + start] = children[i];
        return new Product(exps);
    }
}