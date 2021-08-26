package com.cas.core;

import net.jafama.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * a class representing a finite sum
 *
 * @author Abd-El-Aziz Zayed
 */
public class Sum extends Operator {

//	private static final ExpressionSorter SORTER = new ExpressionSorter(true); // sorter

    private Sum(Expression... expressions) {
        super(ExpressionType.SUM, '+', expressions);
        reverseSortChildren();
    }

    private Sum(HashSet<Character> vars, String... strExpression) {
        super(ExpressionType.SUM, '+', vars, strExpression);
        reverseSortChildren();
    }

    /**
     * sort the children in reverse
     */
    private void reverseSortChildren() {
        Arrays.sort(children, ExpressionSorter.DEFAULT);
        List<Expression> reversed = Arrays.asList(children);
        Collections.reverse(reversed);
        children = reversed.toArray(children);
    }

    /**
     * function to create an expression node with the given children as strings,
     * they still need to be parsed. The point of this function is to perform early
     * refactoring to make life easier later on. It does not have to return a sum
     * node if the refactored expression does not require a sum node
     *
     * @param vars          - the set of all variables
     * @param strExpression - the array of strings to be parsed into expressions
     * @return the refactored expression
     */
    public static Expression create(HashSet<Character> vars, String... strExpression) {
        if (strExpression.length == 0)
            return null;
        else if (strExpression.length == 1)
            return Parser.generateExpression(strExpression[0], vars);
        return new Sum(vars, strExpression);
    }

    /**
     * function to create expression node from the given expressions. The point of
     * this function is to perform early refactoring and simplification to make life
     * easier later on. It does not have to return a sum node if the simplified
     * expression does not require a sum node
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

        ArrayList<Expression> valid = new ArrayList<>();
        Constant c0 = new Constant(0d);
        for (int i = 0; i < expressions.length; i++)
            if (!expressions[i].equals(c0))
                valid.add(expressions[i]);

        if (valid.isEmpty())
            return c0;
        if (valid.size() == 1)
            return valid.get(0);
        if (valid.size() == 2)
            if (valid.get(0) instanceof Constant && valid.get(1) instanceof Constant) {
                Constant a = (Constant) valid.get(0);
                Constant b = (Constant) valid.get(1);
                return new Constant(a.getValue() + b.getValue());
            }

        Collections.sort(valid, ExpressionSorter.DEFAULT);

        /*
         * extract inner sums if any
         */
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression exp = valid.get(i);
            if (exp instanceof Sum) {
                Sum sum = (Sum) exp;
                for (int j = 0; j < sum.children.length; j++)
                    valid.add(sum.children[j]);
                valid.remove(i);
            }
        }

        /*
         * ArrayList to hold the grouped and simplified expressions/children
         */
        ArrayList<Expression> grouped = new ArrayList<>(); // grouped expressions

        /*
         * group constant
         */
        double total = 0d;
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression expression = valid.get(i);
            if (expression instanceof Constant) {
                total += ((Constant) expression).getValue();
                valid.remove(i);
            }
        }
        if (total != 0d)
            grouped.add(new Constant(total));

        /*
         * transform everything into a product: 1*f(x)
         */
        for (int i = 0; i < valid.size(); i++) {
            Expression exp = valid.get(i);
            if (exp instanceof Product) {
                Product p = (Product) exp;
                if (!(p.children[0] instanceof Constant))
                    valid.set(i, p.addedConstant());
            } else {
                valid.set(i, new Product(new Constant(1d), exp));
            }
        }

        /*
         * group common terms
         */
        ArrayList<Expression> products = new ArrayList<>();
        for (int i = valid.size() - 1; i >= 0; i--) {
            Expression expression = valid.get(i);
            if (expression instanceof Product) {
                products.add((Product) expression);
                valid.remove(i);
            }
        }
        // add common expressions: 3*f(x) + 1*f(x) = 4*f(x)
        for (int i = products.size() - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                Product p1 = (Product) products.get(i);
                Product p2 = (Product) products.get(j);

                Constant c1 = (Constant) p1.children[0];
                Constant c2 = (Constant) p2.children[0];

                Product rest1 = p1.removedConstant();
                Product rest2 = p2.removedConstant();

                if (rest1.equals(rest2)) {
                    products.set(i, Product.create(new Constant(c1.getValue() + c2.getValue()), rest1));
                    products.remove(j);
                    i--;
                }
            }
        }
        grouped.addAll(products);

        /*
         * rest of the expressions that were not eligible for simplifications
         */
        grouped.addAll(valid);

        if (grouped.isEmpty()) {
            System.out.println("Empty");
            return new Constant(0d);
        }

        // final simplification of every expression
        for (int i = 0; i < grouped.size(); i++)
            grouped.set(i, grouped.get(i).simplify());

        Collections.sort(grouped, ExpressionSorter.DEFAULT); // sort

        // final expression
        return new Sum(grouped.toArray(new Expression[0]));
    }

    @Override
    protected double operate(double a, double b) {
        return a + b;
    }

    @Override
    protected double neutral() {
        return 0d;
    }

    @Override
    protected void print(int index, StringBuilder builder, boolean latex) {
        boolean putSymbol = true;

        // don't put + sign if next element is negated, like -sinx : -1*sinx in memory
        if (children[index] instanceof Product) {
            Product product = (Product) children[index];
            if (product.children[0] instanceof Constant) {
                Constant constant = (Constant) product.children[0];
                putSymbol = !(FastMath.signum(constant.getValue()) < 0);
            }
        }

        if (putSymbol && index != 0)
            builder.append(symbol);

        String add = latex ? children[index].toLatex() : children[index].toFancyString();
        builder.append(add);
    }

    @Override
    protected boolean needsBrackets(Expression e) {
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        Expression[] derivatives = new Expression[children.length];
        for (int i = 0; i < children.length; i++)
            derivatives[i] = children[i].differentiate(var);
        return Sum.create(derivatives);
    }

    @Override
    public Expression simplify() {
        return create(simplifiedChildren());
    }
}