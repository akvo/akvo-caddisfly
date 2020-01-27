package org.akvo.caddisfly.sensor.striptest.models;


import org.akvo.caddisfly.model.Result;

public class PatchResult {

    private int id;
    private boolean measured;
    private float[] Xyz;
    private float[] Lab;
    private float value;
    private String bracket;
    private int index;
    private String unit;
    private Result patch;
    private float[][][] image;

    // constructor
    public PatchResult(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMeasured() {
        return measured;
    }

    public void setMeasured(boolean measured) {
        this.measured = measured;
    }

    public float[] getXyz() {
        return Xyz;
    }

    public void setXyz(float[] xyz) {
        Xyz = xyz;
    }

    public float[] getLab() {
        return Lab;
    }

    public void setLab(float[] lab) {
        Lab = lab;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getBracket() {
        return bracket;
    }

    public void setBracket(String bracket) {
        this.bracket = bracket;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Result getPatch() {
        return patch;
    }

    public void setPatch(Result patch) {
        this.patch = patch;
    }

    public float[][][] getImage() {
        return this.image;
    }

    public void setImage(float[][][] image) {
        this.image = image;
    }
}
