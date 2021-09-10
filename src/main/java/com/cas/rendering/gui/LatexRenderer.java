package com.cas.rendering.gui;

import com.cas.core.Equation;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * class to render expressions and equations in latex
 *
 * @author Abd-El-Aziz Zayed
 */
public class LatexRenderer {

    public static int textSize = 30; // the size of the LaTeX text

    public static BufferedImage toImage(Equation eq, String prefix, String postfix) {
        return toImage(prefix + eq.toLatex() + postfix);
    }

    public static BufferedImage toImage(Equation eq) {
        return toImage(eq.toLatex());
    }

    /**
     * create a buffered image from the given latex string
     *
     * @param latex - the latex to render
     * @return the image
     */
    public static BufferedImage toImage(String latex) {
        TeXFormula formula = new TeXFormula(latex);
        Image image = formula.createBufferedImage(TeXConstants.STYLE_DISPLAY, textSize, Color.WHITE, Color.BLACK);
        return (BufferedImage) image;
    }
}