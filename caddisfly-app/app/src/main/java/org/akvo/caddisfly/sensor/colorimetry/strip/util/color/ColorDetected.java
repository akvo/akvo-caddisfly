package org.akvo.caddisfly.sensor.colorimetry.strip.util.color;

import org.opencv.core.Scalar;

/**
 * Created by linda on 8/26/15
 */
public class ColorDetected {

    private int color;
    private Scalar rgb;
    private Scalar lab;
    private Scalar hsv;
    private int x;
    private double ppm;

    public ColorDetected(int x) {

        this.x = x;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public double getPpm() {
        return ppm;
    }

    public void setPpm(double ppm) {
        this.ppm = ppm;
    }

    public Scalar getRgb() {
        return rgb;
    }

    public void setRgb(Scalar rgb) {
        this.rgb = rgb;
    }

    public Scalar getLab() {
        return lab;
    }

    public void setLab(Scalar lab) {
        this.lab = lab;
    }

    public Scalar getHsv() {
        return hsv;
    }

    public void setHsv(Scalar hsv) {
        this.hsv = hsv;
    }
}
