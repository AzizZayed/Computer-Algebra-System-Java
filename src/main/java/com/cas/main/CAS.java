package com.cas.main;

import com.cas.rendering.core.Renderer;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.io.IOException;

/**
 * main class, launches the whole application
 *
 * @author Abd-El-Aziz Zayed
 */
public class CAS {
    public static void main(String[] args) throws IOException {
//        TeXFormula formula = new TeXFormula("z = xy");
//        formula.createBufferedImage(TeXConstants.STYLE_DISPLAY, 5, null, null);
        new Renderer();
    }
}