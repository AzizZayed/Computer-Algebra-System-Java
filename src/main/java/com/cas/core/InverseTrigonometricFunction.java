package com.cas.core;


import net.jafama.FastMath;

/**
 * class for the inverse trigonometric functions like arctan, arccos, arcsin
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class InverseTrigonometricFunction extends TrigonometricFunction {

    public InverseTrigonometricFunction(ExpressionType type, Expression expr) {
        super(type, expr);
    }

    @Override
    public String toLatex() {
        if (needsBrackets())
            return type.toString().substring(3) + "^{-1}\\left(" + expr.toLatex() + "\\right)";
        return type.toString().substring(3) + "^{-1}" + expr.toLatex();
    }

    /*
     * arccosine function
     */
    public static final class ArcCos extends InverseTrigonometricFunction {
        public ArcCos(Expression expr) {
            super(ExpressionType.ARCCOS, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.acos(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // -1 * (1-f^2)^(-1/2) * f'
                    new Constant(-1d), expr.differentiate(var), // -f'
                    new Power( // (1-f^2)^(-1/2)
                            Sum.create( // 1-f^2
                                    new Constant(1d), // 1
                                    Product.create( // -f^2
                                            new Constant(-1d), // -1
                                            new Power(expr, new Constant(2d)) // f^2
                                    ) // end product -f^2
                            ), // end sum of 1-f^2
                            new Constant(-0.5d) // -1/2, the power
                    ) // end power: (1-f^2)^(-1/2)
            ); // end Product -1 * (1-f^2)^(-1/2) * f'
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new ArcCos(expr.simplify()) : eval;
        }
    }

    /*
     * arcsine function
     */
    public static final class ArcSin extends InverseTrigonometricFunction {
        public ArcSin(Expression expr) {
            super(ExpressionType.ARCSIN, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.asin(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // (1-f^2)^(-1/2) * f'
                    expr.differentiate(var), // f'
                    new Power( // (1-f^2)^(-1/2)
                            Sum.create( // 1-f^2
                                    new Constant(1d), // 1
                                    Product.create( // -f^2
                                            new Constant(-1d), // -1
                                            new Power(expr, new Constant(2d)) // f^2
                                    ) // end product -f^2
                            ), // end sum of 1-f^2
                            new Constant(-0.5d) // -1/2, the power
                    ) // end power: (1-f^2)^(-1/2)
            ); // end Product (1-f^2)^(-1/2) * f'
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new ArcSin(expr.simplify()) : eval;
        }
    }

    /*
     * arctangent function
     */
    public static final class ArcTan extends InverseTrigonometricFunction {
        public ArcTan(Expression expr) {
            super(ExpressionType.ARCTAN, expr);
        }

        @Override
        protected double compute(double in) {
            return FastMath.atan(in);
        }

        @Override
        public Expression differentiate(char var) {
            return Product.create( // f' * (1 + f^2)^(-1)
                    expr.differentiate(var), // f'
                    new Power( // (1 + f^2)^(-1)
                            Sum.create( // 1 + f^2
                                    new Constant(1d), // 1
                                    new Power( // f^2
                                            expr, // f
                                            new Constant(2d) // 2: squared
                                    ) // end f^2
                            ), // end 1 + f^2
                            new Constant(-1d) // -1
                    ) // end (1 + f^2)^(-1)
            ); // end of f' * (1 + f^2)^(-1)
        }

        @Override
        public Expression simplify() {
            Constant eval = evaluate();
            return eval == null ? new ArcTan(expr.simplify()) : eval;
        }
    }
}