package org.akvo.caddisfly.model;

import java.util.ArrayList;

public class TestInfo {
    private final String mName;
    private final String mCode;
    private final String mUnit;
    private final ArrayList<ResultRange> mRanges;
    private int mIncrement;

    public TestInfo(String name, String code, String unit) {
        mName = name;
        mCode = code;
        mUnit = unit;
        mRanges = new ArrayList<ResultRange>();
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

    public ArrayList<ResultRange> getRanges() {
        return mRanges;
    }

    public void addRange(ResultRange value) {
        mRanges.add(value);
    }

    public int getIncrement() {
        return mIncrement;
    }

    public void setIncrement(int value) {
        mIncrement = value;
    }
}
