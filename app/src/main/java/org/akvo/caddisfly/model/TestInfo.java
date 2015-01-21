package org.akvo.caddisfly.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class TestInfo {
    private final Hashtable mNames;
    private final String mCode;
    private final String mUnit;
    private final ArrayList<ResultRange> mRanges;
    private int mIncrement;

    public TestInfo(Hashtable names, String code, String unit) {
        mNames = names;
        mCode = code;
        mUnit = unit;
        mRanges = new ArrayList<ResultRange>();
    }

    public String getName(String languageCode) {
        if (mNames.containsKey(languageCode)) {
            return mNames.get(languageCode).toString();
        } else {
            return mNames.get("en").toString();
        }

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
