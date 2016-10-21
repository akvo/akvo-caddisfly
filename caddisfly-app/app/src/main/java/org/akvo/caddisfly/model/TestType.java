package org.akvo.caddisfly.model;

/**
 * The different types of testing methods
 */
public enum TestType {
    /**
     * Liquid reagent is mixed with sample and color is analysed from the resulting
     * color change in the solution
     */
    COLORIMETRIC_LIQUID,

    /**
     * Strip paper is dipped into the sample and color is analysed from the resulting
     * color change on the strip paper
     */
    COLORIMETRIC_STRIP,

    /**
     * External sensors connected to the phone/device
     */
    SENSOR,

    /**
     * Measure of turbidity in the liquid
     */
    TURBIDITY_COLIFORMS
}

