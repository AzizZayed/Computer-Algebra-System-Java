package com.cas.core;

import com.cas.core.InverseTrigonometricFunction.ArcCos;
import com.cas.core.InverseTrigonometricFunction.ArcSin;
import com.cas.core.InverseTrigonometricFunction.ArcTan;
import net.jafama.FastMath;

import java.util.HashMap;

/**
 * class inherited by all trigonometric functions like cosine, sine, tangent
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class TrigonometricFunction extends FixedInputFunction {

    /**
     * constructor
     *
     * @param expr - expression inside the function (to compute)
     */
    public TrigonometricFunction(ExpressionType type, Expression expr) {
        super(type, expr);
    }

    @Override
    public double evaluate(HashMap<Character, Double> varValues) {
        return compute(expr.evaluate(varValues));
    }

    @Override
    public String toString() {
        return type + "(" + expr + ")";
    }

    @Override
    public String toFancyString() {
        if (needsBrackets())
            return type + "(" + expr.toFancyString() + ")";
        return type + expr.toFancyString();
    }

    @Override
    public String toLatex() {
        if (needsBrackets())
            return "\\" + type + "{\\left(" + expr.toLatex() + "\\right)}";
        return "\\" + type + "{" + expr.toLatex() + "}";
    }

    @Override
    public boolean equals(Expression e) {
        if (e instanceof TrigonometricFunction) {
            TrigonometricFunction func = (TrigonometricFunction) e;
            if (type.equals(func.type))
                return expr.equals(func.expr);
        }
        return false;
    }

    /**
     * @return if the input needs surrounding brackets
     */
    protected boolean needsBrackets() {
        return expr instanceof Operator || expr instanceof Power || expr instanceof Fraction || expr instanceof Log;
    }

    /**
     * @return evaluation of the trigonometric function if input is a constant and
     * the result is an integer
     */
    protected Constant evaluate() {
        if (expr instanceof Constant) {
            double result = compute(((Constant) expr).getValue());
            if (FastMath.floor(result) == result)
                return new Constant(result);
        }
        return null;
    }

    /**
     * @param in - input to the trigonometric function
     * @return the computed value from the trigonometric function
     */
    protected abstract double compute(double in);

    /*
     * cosine function
     */
    public static final class Cos extends TrigonometricFunction {
        public Cos(Expression expr) {
            super(ExpressionType.COS, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.cos(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // -1 * f' * sin(f)
                    new Constant(-1d), // -1
                    expr.differentiate(var), // f'
                    new Sin(expr) // sin(f)
            ); // end -1 * f' * sin(f)
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            if (eval != null)
                return eval;
            if (expr instanceof InverseTrigonometricFunction.ArcCos)
                return ((ArcCos) expr).expr.simplify();
            return new Cos(expr.simplify());
        }
    }

    /*
     * sine function
     */
    public static final class Sin extends TrigonometricFunction {
        public Sin(Expression expr) {
            super(ExpressionType.SIN, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.sin(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // f' * cos(f)
                    expr.differentiate(var), // f'
                    new Cos(expr) // cos(f)
            ); // end f' * cos(f)
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            if (eval != null)
                return eval;
            if (expr instanceof ArcSin)
                return ((ArcSin) expr).expr.simplify();
            return new Sin(expr.simplify());
        }
    }

    /*
     * tangent function
     */
    public static final class Tan extends TrigonometricFunction {
        public Tan(Expression expr) {
            super(ExpressionType.TAN, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.tan(in);
        }

        @Override
        public Expression differentiate(char var) {
//			System.out.println("in tan");

            Expression p = Product.create( // f' * (sec(f))^2
                    expr.differentiate(var), // f'
                    new Power( // (sec(f))^2
                            new Sec(expr), // sec(f)
                            new Constant(2d) // 2
                    ) // end (sec(f))^2
            ); // end f' * (sec(f))^2
//			System.out.println(p);
            return p;
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            if (eval != null)
                return eval;
            if (expr instanceof ArcTan)
                return ((ArcTan) expr).expr.simplify();
            return new Tan(expr.simplify());
        }
    }

    /*
     * cosecant function
     */
    public static final class Csc extends TrigonometricFunction {
        public Csc(Expression expr) {
            super(ExpressionType.CSC, expr);
        }

        @Override
        protected double compute(double in) {
            return 1.0d / FastMath.sin(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // -1 * csc(f) * cot(f) * f'
                    new Constant(-1d), // -1
                    expr.differentiate(var), // f'
                    new Csc(expr), // csc(f)
                    new Cot(expr) // cot(f)
            ); // end csc(f) * cot(f) * f'
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Csc(expr.simplify()) : eval;
        }
    }

    /*
     * secant function
     */
    public static final class Sec extends TrigonometricFunction {
        public Sec(Expression expr) {
            super(ExpressionType.SEC, expr);
        }

        @Override
        protected double compute(double in) {
            return 1.0d / FastMath.cos(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // sec(f) * tan(f) * f'
                    expr.differentiate(var), // f'
                    new Sec(expr), // sec(f)
                    new Tan(expr) // tan(f)
            ); // end sec(f) * tan(f) * f'
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Sec(expr.simplify()) : eval;
        }
    }

    /*
     * cotangent function
     */
    public static final class Cot extends TrigonometricFunction {
        public Cot(Expression expr) {
            super(ExpressionType.COT, expr);
        }

        @Override
        protected double compute(double in) {
            return 1.0d / FastMath.tan(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // -1 * f' * (csc(f))^2
                    new Constant(-1d), // -1
                    expr.differentiate(var), // f'
                    new Power( // (csc(f))^2
                            new Csc(expr), // csc(f)
                            new Constant(2d) // 2
                    ) // end (csc(f))^2
            ); // end f' * (csc(f))^2
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new Cot(expr.simplify()) : eval;
        }
    }
}