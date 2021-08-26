package com.cas.core;

/**
 * Interface for all math related structures
 *
 * @author Abd-El-Aziz Zayed
 */
public interface IMath {

    /**
     * @return the latex code for the mathematical expression
     */
    String toLatex();

    /**
     * @return a fancy string representation of the mathematical expression. This
     * function uses Unicode when possible
     */
    String toFancyString();

    @Override
    String toString();
}
