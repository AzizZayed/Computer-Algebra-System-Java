package com.cas.core;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * a class that parents any function that can take an arbitrary number of inputs
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class ManyInputFunction extends Expression {

    protected Expression[] children; // input expressions

    public ManyInputFunction(ExpressionType type, Expression[] expressions) {
        super(type);
        children = expressions;
        Arrays.sort(children, ExpressionSorter.DEFAULT);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("(");
        for (int i = 0; i < children.length - 1; i++) {
            sb.append(children[i]);
            sb.append(",");
        }
        sb.append(children[children.length - 1]);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toFancyString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("(");
        for (int i = 0; i < children.length - 1; i++) {
            sb.append(children[i].toFancyString());
            sb.append(", ");
        }
        sb.append(children[children.length - 1].toFancyString());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toLatex() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\");
        sb.append(type);
        sb.append("\\left(");
        for (int i = 0; i < children.length - 1; i++) {
            sb.append(children[i].toLatex());
            sb.append(", ");
        }
        sb.append(children[children.length - 1].toLatex());
        sb.append("\\right)");
        return sb.toString();
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        double[] values = new double[children.length];
        for (int i = 0; i < values.length; i++)
            values[i] = children[i].evaluate(varValues);
        return compute(values);
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof ManyInputFunction) {
            ManyInputFunction func = (ManyInputFunction) e;
            if (type.equals(func.type)) {
                for (int i = 0; i < children.length; i++)
                    if (!children[i].equals(func.children[i]))
                        return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression differentiate(char var) {
        throw new IllegalArgumentException(
                "Cannot differentiate a function with arbitrary amount of inputs like min and max. Compute numerically instead.");
    }

    /**
     * @return array of the inputs simplified
     */
    protected Expression[] simplifyChildren() {
        Expression[] simplified = new Expression[children.length];
        for (int i = 0; i < children.length; i++)
            simplified[i] = children[i].simplify();
        return simplified;
    }

    /**
     * Common simplification: when the inputs are constants, the operation can be
     * computed and returned without needing the function node
     *
     * @return a constant node with the result, null if the operation is impossible
     */
    protected Constant evaluate() {
        double[] result = new double[children.length];
        for (int i = 0; i < children.length; i++)
            if (children[i] instanceof Constant)
                result[i] = ((Constant) children[i]).getValue();
            else
                return null;
        return new Constant(compute(result));
    }

    /**
     * Common simplification: when a few of the inputs are constants, the operation
     * can be computed for those and simplify the function
     *
     * @return a constant node with the result, null if simplification is useless
     */
    protected Constant partialEvaluate() {
        int nConstants = 0;
        ArrayList<Double> resultList = new ArrayList<>();
        for (int i = 0; i < children.length; i++)
            if (children[i] instanceof Constant) {
                nConstants++;
                resultList.add(((Constant) children[i]).getValue());
            }
        if (nConstants < 2)
            return null;
        double[] result = new double[resultList.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = resultList.get(i);
        return new Constant(compute(result));
    }

    protected abstract double compute(double[] in);

    /**
     * a min function with arbitrary number of inputs
     *
     * @author Abd-El-Aziz Zayed
     */
    public static final class Min extends ManyInputFunction {
        public Min(Expression... expressions) {
            super(ExpressionType.MIN, expressions);
        }

        @Override
        protected double compute(double[] in) {
            return NumberUtils.min(in);
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Min(simplifyChildren()) : eval;
        }
    }

    /**
     * a max function with arbitrary number of inputs
     *
     * @author Abd-El-Aziz Zayed
     */
    public static final class Max extends ManyInputFunction {
        public Max(Expression... expressions) {
            super(ExpressionType.MAX, expressions);
        }

        @Override
        protected double compute(double[] in) {
            return NumberUtils.max(in);
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Max(simplifyChildren()) : eval;
        }
    }
}