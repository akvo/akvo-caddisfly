package org.akvo.caddisfly.model;

public class TestInfo {
    private final String mName;
    private final String mCode;
    private final String mUnit;
    private final double mRangeStart;
    private final double mRangeEnd;
    private final double mHighRangeStart;
    private final double mHighRangeEnd;
    private int mIncrement;

    public TestInfo(String name, String code, String unit, double rangeStart, double rangeEnd,
                    double highRangeStart, double highRangeEnd) {
        mName = name;
        mCode = code;
        mUnit = unit;
        mRangeStart = rangeStart;
        mRangeEnd = rangeEnd;
        mHighRangeStart = highRangeStart;
        mHighRangeEnd = highRangeEnd;
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public String getUnit() {
        return mUnit;
    }

    public double getRangeStart() {
        return mRangeStart;
    }

    public double getRangeEnd() {
        return mRangeEnd;
    }

    public double getHighRangeStart() {
        return mHighRangeStart;
    }

    public int getIncrement() {
        return mIncrement;
    }

    public void setIncrement(int value) {
        mIncrement = value;
    }
}
