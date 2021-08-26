package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.gui.LatexRenderer;
import com.cas.rendering.util.Grid;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * A wrapper class to hold a function of x and y and its derivatives with
 * respect to x and y
 *
 * @author Abd-El-Aziz Zayed
 */
public class SurfaceTrio {

    /**
     * The function and it's derivatives
     */
    private Surface function;
    private Surface xDerivative;
    private Surface yDerivative;

    public SurfaceTrio(Equation eq) {
        String latex = "z = " + eq.toLatex();
        System.out.println("latexing: " + latex);
        Equation simplified = eq.simplified();
        System.out.println("simplifying: " + simplified);

        BufferedImage latexImage = LatexRenderer.toImage(latex);
        System.out.println("latexing image");

        function = new Surface(simplified, latexImage, true);

        System.out.println("deriving");

        try {
            Equation xDer = simplified.derivative('x');
            latex = "z_x = " + xDer.toLatex();
            xDerivative = new Surface(xDer, LatexRenderer.toImage(latex), false);
        } catch (Exception e) {
            e.printStackTrace();
            xDerivative = null;
        }

        System.out.println("got x der");

        try {
            Equation yDer = simplified.derivative('y');
            latex = "z_y = " + yDer.toLatex();
            yDerivative = new Surface(yDer, LatexRenderer.toImage(latex), false);
        } catch (Exception e) {
            e.printStackTrace();
            yDerivative = null;
        }

        System.out.println("got y der");
    }

    /**
     * update the surfaces
     *
     * @param grid      - coordinate system to render according to
     * @param varValues - value of all the parameters
     */
    public void update(Grid grid, HashMap<Character, Double> varValues) {
        function.update(grid, varValues);
        if (xDerivative != null)
            xDerivative.update(grid, varValues);
        if (yDerivative != null)
            yDerivative.update(grid, varValues);
    }

    /**
     * @return the function
     */
    public Surface getFunction() {
        return function;
    }

    /**
     * @return the xDerivative
     */
    public Surface getxDerivative() {
        return xDerivative;
    }

    /**
     * @return the yDerivative
     */
    public Surface getyDerivative() {
        return yDerivative;
    }

    /**
     * cleanup the GPU memory when not needed anymore
     */
    public void cleanup() {
        function.cleanup();
        if (xDerivative != null)
            xDerivative.cleanup();
        if (yDerivative != null)
            yDerivative.cleanup();
    }
}