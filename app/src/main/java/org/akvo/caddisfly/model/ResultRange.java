package org.akvo.caddisfly.model;

public class ResultRange {
    private final double mStart;
    private final double mEnd;
    private final double mMultiplier;

    public ResultRange(double rangeStart, double rangeEnd, double dilution) {
        mStart = rangeStart;
        mEnd = rangeEnd;
        mMultiplier = 100 / (100 - dilution);
    }

    public double getStart() {
        return mStart;
    }

    public double getEnd() {
        return mEnd;
    }

    public double getMultiplier() {
        return mMultiplier;
    }

}
