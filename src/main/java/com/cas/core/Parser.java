package com.cas.core;

import com.cas.core.BracketFunction.Abs;
import com.cas.core.BracketFunction.Ceiling;
import com.cas.core.BracketFunction.Floor;
import com.cas.core.InverseTrigonometricFunction.ArcCos;
import com.cas.core.InverseTrigonometricFunction.ArcSin;
import com.cas.core.InverseTrigonometricFunction.ArcTan;
import com.cas.core.Log.Ln;
import com.cas.core.ManyInputFunction.Max;
import com.cas.core.ManyInputFunction.Min;
import com.cas.core.Power.Exp;
import com.cas.core.TrigonometricFunction.Cos;
import com.cas.core.TrigonometricFunction.Cot;
import com.cas.core.TrigonometricFunction.Csc;
import com.cas.core.TrigonometricFunction.Sec;
import com.cas.core.TrigonometricFunction.Sin;
import com.cas.core.TrigonometricFunction.Tan;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

/**
 * A class with static functions that help with parsing a mathematical
 * expression
 *
 * @author Abd-El-Aziz Zayed
 */
public class Parser {

    /**
     * clean up the given expression to parse more efficiently
     *
     * @param exp - given expression
     * @return the clean expression
     */
    public static String clean(String exp) {
        exp = exp.trim();
        exp = exp.replace(" ", "");
        exp = exp.replace("[", "(");
        exp = exp.replace("]", ")");
        exp = exp.replace("{", "(");
        exp = exp.replace("}", ")");
        exp = exp.replace("()", "");
        return exp;
    }

    /**
     * setup the expression to be ready for parsing
     *
     * @param exp - the expression to setup
     * @return the ready expression
     */
    private static String setup(String exp) {
        exp = exp.replace("-", "+-1*");
        exp = exp.replace(")(", ")*(");
        // put * signs where needed
        exp = removeUnnecessaryBracket(exp);
        return exp;
    }

    /**
     * parse the given expression into an expression tree
     *
     * @param exp  - expression to parse
     * @param vars - set with all the used variables
     * @return expression tree
     */
    public static Expression parseExpression(String exp, HashSet<Character> vars) {
        exp = setup(exp);

        if (!isValidExpression(exp))
            throw new IllegalArgumentException("Invalid Expression");

        return generateExpression(exp, vars);
    }

    /**
     * remove the unnecessary brackets from an expression
     *
     * @param exp - expression
     * @return the clean expression
     */
    private static String removeUnnecessaryBracket(String exp) {
        int[] firstBrackets = getFirstPairOfBrackets(exp);

        while (firstBrackets != null && firstBrackets.length > 0 && firstBrackets[0] == 0
                && firstBrackets[1] == exp.length() - 1) {
            exp = exp.substring(1, exp.length() - 1);
            firstBrackets = getFirstPairOfBrackets(exp);
        }
        return exp;
    }

    /**
     * generate an expression tree from the given string expression
     *
     * @param strExp - given expression
     * @param vars   - set with all the used variables
     * @return the expression tree
     */
    protected static Expression generateExpression(String strExp, HashSet<Character> vars) {
        strExp = removeUnnecessaryBracket(strExp);

        if (strExp.contains("(")) {
            int[] firstBrackets = getFirstPairOfBrackets(strExp);

            String cut = strExp; // where we will cut out the inside of the brackets
            LinkedList<String> remove = new LinkedList<String>();
            while (firstBrackets != null) { // run until no more brackets to analyze
                String rem = cut.substring(firstBrackets[0] + 1, firstBrackets[1]);
                remove.add(rem);
                cut = StringUtils.replaceOnce(cut, '(' + rem + ')', "()");
                firstBrackets = getFirstPairOfBrackets(cut);
            }

            if (cut.contains("+")) { // parse addition
                String[] exps = StringUtils.split(cut, "+");

                // replace all the () with the respective expression
                for (int i = 0; i < exps.length; i++)
                    while (exps[i].contains("()"))
                        exps[i] = StringUtils.replaceOnce(exps[i], "()", "(" + remove.pop() + ")");

                return Sum.create(vars, exps); // finally create sum node with recursively generated children
            } else if (cut.contains("*")) { // parse multiplication
                String[] exps = StringUtils.split(cut, "*");

                // replace all the () with the respective expression
                for (int i = 0; i < exps.length; i++)
                    while (exps[i].contains("()"))
                        exps[i] = StringUtils.replaceOnce(exps[i], "()", "(" + remove.pop() + ")");

                return Product.create(vars, exps); // finally create product node with recursively generated children
            } else if (cut.contains("/")) { // parse division
                int sign = cut.indexOf('/');
                String num = cut.substring(0, sign);
                String denum = cut.substring(sign + 1);

                while (num.contains("()"))
                    num = StringUtils.replaceOnce(num, "()", "(" + remove.pop() + ")");
                while (denum.contains("()"))
                    denum = StringUtils.replaceOnce(denum, "()", "(" + remove.pop() + ")");

                return new Fraction(generateExpression(num, vars), generateExpression(denum, vars)); // create fraction
            } else if (cut.contains("^")) { // parse powers
                int sign = cut.indexOf('^');
                String base = cut.substring(0, sign);
                String power = cut.substring(sign + 1);

                while (base.contains("()"))
                    base = StringUtils.replaceOnce(base, "()", "(" + remove.pop() + ")");
                while (power.contains("()"))
                    power = StringUtils.replaceOnce(power, "()", "(" + remove.pop() + ")");

                if (base.equals("e"))
                    return new Exp(generateExpression(power, vars));

                return new Power(generateExpression(base, vars), generateExpression(power, vars));
            } else if (cut.equals("sqrt()")) // square root
                return new Power(generateExpression(remove.pop(), vars), new Constant(0.5d));

            else if (cut.equals("sin()")) // sin
                return new Sin(generateExpression(remove.pop(), vars));
            else if (cut.equals("cos()")) // cos
                return new Cos(generateExpression(remove.pop(), vars));
            else if (cut.equals("tan()")) // tan
                return new Tan(generateExpression(remove.pop(), vars));

            else if (cut.equals("csc()")) // csc
                return new Csc(generateExpression(remove.pop(), vars));
            else if (cut.equals("sec()")) // sec
                return new Sec(generateExpression(remove.pop(), vars));
            else if (cut.equals("cot()")) // cot
                return new Cot(generateExpression(remove.pop(), vars));

            else if (cut.equals("arcsin()")) // arcsin
                return new ArcSin(generateExpression(remove.pop(), vars));
            else if (cut.equals("arccos()")) // arccos
                return new ArcCos(generateExpression(remove.pop(), vars));
            else if (cut.equals("arctan()")) // arctan
                return new ArcTan(generateExpression(remove.pop(), vars));

            else if (cut.equals("abs()")) // absolute value
                return new Abs(generateExpression(remove.pop(), vars));

            else if (cut.equals("floor()")) // floor function
                return new Floor(generateExpression(remove.pop(), vars));
            else if (cut.equals("ceil()")) // ceiling function
                return new Ceiling(generateExpression(remove.pop(), vars));

            else if (cut.equals("max()")) { // max function
                String in = remove.pop();
                String[] ins = StringUtils.split(in, ",");
                if (ins.length == 1)
                    return generateExpression(ins[0], vars);

                Expression[] exps = new Expression[ins.length];
                for (int i = 0; i < exps.length; i++)
                    exps[i] = generateExpression(ins[i], vars);
                return new Max(exps);
            } else if (cut.equals("min()")) { // min function
                String in = remove.pop();
                String[] ins = StringUtils.split(in, ",");
                if (ins.length == 1)
                    return generateExpression(ins[0], vars);

                Expression[] exps = new Expression[ins.length];
                for (int i = 0; i < exps.length; i++)
                    exps[i] = generateExpression(ins[i], vars);
                return new Min(exps);

            } else if (cut.equals("mod()")) {
                String in = remove.pop();
                String[] ins = StringUtils.split(in, ",");

                if (ins.length != 2)
                    throw new IllegalArgumentException("Invalid Expression");

                Expression in1 = generateExpression(ins[0], vars);
                Expression in2 = generateExpression(ins[1], vars);

                return new Mod(in1, in2);

            } else if (cut.equals("sign()")) {
                return new Sign(generateExpression(remove.pop(), vars));
            } else if (cut.equals("log()")) // logarithm
                return new Log(generateExpression(remove.pop(), vars));
            else if (cut.equals("ln()")) // natural logarithm
                return new Ln(generateExpression(remove.pop(), vars));
            else if (cut.startsWith("log_")) { // log with specified base
                String log = cut.substring(4);
                int sign = log.indexOf('_');
                String base = log.substring(0, sign);
                String num = log.substring(sign + 1);

                while (base.contains("()"))
                    base = StringUtils.replaceOnce(base, "()", "(" + remove.pop() + ")");
                while (num.contains("()"))
                    num = StringUtils.replaceOnce(num, "()", "(" + remove.pop() + ")");

                return new Log(generateExpression(base, vars), generateExpression(num, vars));
            }

        } else if (strExp.equals("e")) // e constant
            return new Constant(Constant.EXP.getValue());
        else if (strExp.equals("pi")) // pi constant
            return new Constant(Constant.PI.getValue());
        else if (strExp.equals("phi")) // phi constant
            return new Constant(Constant.GOLDEN_RATIO.getValue());
        else if (strExp.length() == 1 && Character.isAlphabetic(strExp.charAt(0))) {
            char c = strExp.charAt(0);
            vars.add(c);
            return new Variable(c);
        } else if (NumberUtils.isParsable(strExp)) // numbers
            return new Constant(NumberUtils.createDouble(strExp));

        else if (strExp.contains("+")) { // parse addition
            String[] exps = StringUtils.split(strExp, "+");
            return Sum.create(vars, exps);

        } else if (strExp.contains("*")) { // parse multiplication
            String[] exps = StringUtils.split(strExp, "*");
            return Product.create(vars, exps);

        } else if (strExp.contains("/")) { // parse divisions
            int sign = strExp.indexOf('/');
            String num = strExp.substring(0, sign);
            String denum = strExp.substring(sign + 1);
            return new Fraction(generateExpression(num, vars), generateExpression(denum, vars));

        } else if (strExp.contains("^")) { // parse powers
            int sign = strExp.indexOf('^');
            String base = strExp.substring(0, sign);
            String power = strExp.substring(sign + 1);
            if (base.equals("e"))
                return new Exp(generateExpression(power, vars));
            return new Power(generateExpression(base, vars), generateExpression(power, vars));
        } else if (strExp.startsWith("ln")) // natural log
            return new Ln(generateExpression(strExp.substring(2), vars));

        else if (strExp.startsWith("sin")) // sin
            return new Sin(generateExpression(strExp.substring(3), vars));
        else if (strExp.startsWith("cos")) // cos
            return new Cos(generateExpression(strExp.substring(3), vars));
        else if (strExp.startsWith("tan")) // tan
            return new Tan(generateExpression(strExp.substring(3), vars));

        else if (strExp.startsWith("csc")) // csc
            return new Csc(generateExpression(strExp.substring(3), vars));
        else if (strExp.startsWith("sec")) // sec
            return new Sec(generateExpression(strExp.substring(3), vars));
        else if (strExp.startsWith("cot")) // cot
            return new Cot(generateExpression(strExp.substring(3), vars));

        else if (strExp.startsWith("abs")) // absolute value
            return new Abs(generateExpression(strExp.substring(3), vars));

        else if (strExp.startsWith("log") && strExp.length() <= 4) // log
            return new Log(generateExpression(strExp.substring(3), vars));
        else if (strExp.startsWith("log_")) { // log where brackets are not needed
            int sign = strExp.substring(4).indexOf('_') + 4;
            String base = strExp.substring(4, sign);
            String num = strExp.substring(sign + 1);
            return new Log(generateExpression(base, vars), generateExpression(num, vars));

        } else if (strExp.startsWith("ceil")) // ceiling function
            return new Ceiling(generateExpression(strExp.substring(4), vars));
        else if (strExp.startsWith("sqrt")) // sqrt function
            return new Power(generateExpression(strExp.substring(4), vars), new Constant(0.5d));
        else if (strExp.startsWith("floor")) // floor function
            return new Floor(generateExpression(strExp.substring(5), vars));

        else if (strExp.startsWith("arcsin")) // arcsin
            return new ArcSin(generateExpression(strExp.substring(6), vars));
        else if (strExp.startsWith("arccos")) // arccos
            return new ArcCos(generateExpression(strExp.substring(6), vars));
        else if (strExp.startsWith("arctan")) // arctan
            return new ArcTan(generateExpression(strExp.substring(6), vars));

        return null;
    }

    /**
     * determine the indices of the first pair of brackets in the given expression
     *
     * @param expr - the given expression
     * @return an array of 2 with the indices to the pair of brackets
     */
    private static int[] getFirstPairOfBrackets(String expr) {
        String exp = expr.replace("()", "");
        if (exp == null || exp.isEmpty())
            return null;
        ArrayList<Integer> open = new ArrayList<Integer>();
        ArrayList<Integer> closed = new ArrayList<Integer>();
        Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < exp.length(); i++) {
            char c = exp.charAt(i);
            if (c == '(') {
                stack.add(c);
                open.add(i);
                int j;
                for (j = i + 1; j < exp.length() && !stack.isEmpty(); j++) {
                    c = exp.charAt(j);
                    if (c == '(')
                        stack.add(c);
                    else if (c == ')')
                        stack.pop();
                }
                closed.add(j - 1);
            }
        }

        if (open.isEmpty())
            return null;

        int l = StringUtils.countMatches(expr, "()");
        return new int[]{open.get(0) + 2 * l, closed.get(0) + 2 * l};
    }

    /**
     * check if a given expression is mathematically valid
     *
     * @param exp - given expression to check
     * @return true if valid, false otherwise
     */
    private static boolean isValidExpression(String exp) {
        // test for unbalanced brackets
        if (exp == null || exp.isEmpty())
            return false;

        // test for balanced brackets
        if (!balancedBrackets(exp))
            return false;

        // test undefined duplicates
        return !exp.contains("//") && !exp.contains("^^") && !exp.contains("**") && !exp.contains("++") && !exp.contains("--");
    }

    /**
     * check if the brackets in an expression are balanced
     *
     * @param exp - given expression to run check on
     * @return true if balanced, false if otherwise
     */
    private static boolean balancedBrackets(String exp) {
        Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < exp.length(); i++) {
            if (exp.charAt(i) == '(')
                stack.push(exp.charAt(i));
            else if (exp.charAt(i) == ')') {
                if (stack.isEmpty())
                    return false;
                else {
                    char c1 = stack.pop();
                    char c2 = exp.charAt(i);
                    if (!(c1 == '(' && c2 == ')'))
                        return false;
                }
            }
        }
        return stack.isEmpty();
    }
}