package org.akvo.caddisfly.model;

import org.akvo.caddisfly.AppConfig;

public class ResultDetail {
    private final int color;
    private double result;
    private int matchedColor;
    private double distance;
    private int calibrationSteps;
    private AppConfig.ColorModel colorModel;

    public ResultDetail(double result, int color) {
        this.result = result;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public int getMatchedColor() {
        return matchedColor;
    }

    public void setMatchedColor(int matchedColor) {
        this.matchedColor = matchedColor;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCalibrationSteps() {
        return calibrationSteps;
    }

    public void setCalibrationSteps(int calibrationSteps) {
        this.calibrationSteps = calibrationSteps;
    }

    public AppConfig.ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(AppConfig.ColorModel colorModel) {
        this.colorModel = colorModel;
    }
}
