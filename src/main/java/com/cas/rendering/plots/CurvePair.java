package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.gui.LatexRenderer;
import com.cas.rendering.util.Grid;

import java.util.HashMap;

/**
 * A wrapper class to hold a function of x and its derivative with respect to x
 *
 * @author Abd-El-Aziz Zayed
 */
public class CurvePair {

    private final Curve function;
    private Curve derivative; // the function and its derivative

    public CurvePair(Equation eq) {
        String latex = "y = " + eq.toLatex();
        Equation simplified = eq.simplified();

        function = new Curve(simplified, LatexRenderer.toImage(latex), true);
        try {
            Equation der = simplified.derivative('x');
            latex = "y_x = " + der.toLatex();
            derivative = new Curve(der, LatexRenderer.toImage(latex), false);
        } catch (Exception e) {
            e.printStackTrace();
            derivative = null;
        }

    }

    /**
     * update the curves
     *
     * @param grid      - coordinate system to render according to
     * @param varValues - value of all the parameters
     */
    public void update(Grid grid, HashMap<Character, Double> varValues) {
        function.update(grid, varValues);
        if (derivative != null)
            derivative.update(grid, varValues);
    }

    /**
     * @return the function
     */
    public Curve getFunction() {
        return function;
    }

    /**
     * @return the derivative
     */
    public Curve getDerivative() {
        return derivative;
    }

    /**
     * cleanup the GPU memory when not needed anymore
     */
    public void cleanup() {
        function.cleanup();
        if (derivative != null)
            derivative.cleanup();
    }
}