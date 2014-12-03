package org.akvo.caddisfly.model;

public class ResultRange {
    private final double mStart;
    private final double mEnd;
    private final double mMultiplier;

    public ResultRange(double rangeStart, double rangeEnd, double multiplier) {
        mStart = rangeStart;
        mEnd = rangeEnd;
        mMultiplier = multiplier;
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
