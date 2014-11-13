package org.akvo.caddisfly.model;

public class TestInfo {
    private final String mName;
    private final String mCode;
    private final double mRangeStart;
    private final double mRangeEnd;
    private int mIncrement;

    public TestInfo(String name, String code, double rangeStart, double rangeEnd) {
        mName = name;
        mCode = code;
        mRangeStart = rangeStart;
        mRangeEnd = rangeEnd;
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public double getRangeStart() {
        return mRangeStart;
    }

    public double getRangeEnd() {
        return mRangeEnd;
    }

    public int getIncrement() {
        return mIncrement;
    }

    public void setIncrement(int value) {
        mIncrement = value;
    }
}
