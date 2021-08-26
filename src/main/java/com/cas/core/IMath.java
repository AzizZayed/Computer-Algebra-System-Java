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
    public String toLatex();

    /**
     * @return a fancy string representation of the mathematical expression. This
     * function uses Unicode when possible
     */
    public String toFancyString();

    @Override
    public String toString();
}
