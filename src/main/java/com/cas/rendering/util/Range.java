package com.cas.rendering.util;

/**
 * This class represents a range of numbers.
 *
 * @author Abd-El-Aziz Zayed
 */
public class Range {

    private double min, max; // upper and lower bound of an interval

    public Range(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * set the min and max of this range to the given values
     *
     * @param min
     * @param max
     */
    public void set(double min, double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * @return the length of the range
     */
    public double getLength() {
        return max - min;
    }

    /**
     * checks if the given value is in the range described by this class
     *
     * @param value - value to check
     * @return true if the value is in range
     */
    public boolean inRange(double value) {
        return value >= min && value < max;
    }
}