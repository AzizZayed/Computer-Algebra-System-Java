package com.cas.core;

import java.util.Comparator;

/**
 * Comparator class to sort expressions. This takes advantage of Java's built-in
 * sorting functions like Arrays.sort and Collections.sort. In order to use
 * those functions, we will need a class like this one
 *
 * @author Abd-El-Aziz Zayed
 */
public class ExpressionSorter implements Comparator<Expression> {

    protected static final ExpressionSorter DEFAULT = new ExpressionSorter(); // main sorter

    @Override
    public int compare(Expression e1, Expression e2) {
        int diff = e1.getType().getOrder() - e2.getType().getOrder();

        if (diff == 0) {
            if (e1 instanceof Operator) {
                Operator op1 = (Operator) e1;
                Operator op2 = (Operator) e2;

                Expression[] op1Children = op1.getChildren();
                Expression[] op2Children = op2.getChildren();

                diff = op1Children.length - op2Children.length;

                int i = 0;
                while (diff == 0 && i < op1Children.length) {
                    diff = compare(op1Children[i], op2Children[i]);
                    i++;
                }
                return diff;
            }
            if (e1 instanceof ManyInputFunction) {
                ManyInputFunction func1 = (ManyInputFunction) e1;
                ManyInputFunction func2 = (ManyInputFunction) e2;
                diff = func1.children.length - func2.children.length;

                int i = 0;
                while (diff == 0 && i < func1.children.length) {
                    diff = compare(func1.children[i], func2.children[i]);
                    i++;
                }
                return diff;
            }
            if (e1 instanceof FixedInputFunction) {
                FixedInputFunction func1 = (FixedInputFunction) e1;
                FixedInputFunction func2 = (FixedInputFunction) e2;
                diff = compare(func1.expr, func2.expr);

                if (diff == 0) {
                    if (func1 instanceof Log) {
                        Log log1 = (Log) func1;
                        Log log2 = (Log) func2;
                        return compare(log1.base, log2.base);
                    }
                    if (func1 instanceof Power) {
                        Power pow1 = (Power) func1;
                        Power pow2 = (Power) func2;
                        return compare(pow1.power, pow2.power);
                    }
                    if (func1 instanceof Mod) {
                        Mod mod1 = (Mod) func1;
                        Mod mod2 = (Mod) func2;
                        return compare(mod1.divisor, mod2.divisor);
                    }
                }
                return diff;
            }
            if (e1 instanceof Fraction) {
                Fraction frac1 = (Fraction) e1;
                Fraction frac2 = (Fraction) e2;
                diff = compare(frac1.numerator, frac2.numerator);

                if (diff == 0)
                    return compare(frac1.denominator, frac2.denominator);

                return diff;
            }
            if (e1 instanceof Variable) {
                Variable var1 = (Variable) e1;
                Variable var2 = (Variable) e2;
                return var1.getSymbol() - var2.getSymbol();
            }
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                Constant c2 = (Constant) e2;
                return (int) (c1.getValue() - c2.getValue());
            }
        }
        return diff;
    }
}