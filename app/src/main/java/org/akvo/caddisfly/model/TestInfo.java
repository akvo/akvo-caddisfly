package org.akvo.caddisfly.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class TestInfo {
    private final Hashtable mNames;
    private final String mCode;
    private final String mUnit;
    private final ArrayList<ResultRange> mRanges;
    private final int mType;
    private int mIncrement;

    public TestInfo(Hashtable names, String code, String unit, int type) {
        mNames = names;
        mType = type;
        mCode = code;
        mUnit = unit;
        mRanges = new ArrayList<>();
    }

    public String getName(String languageCode) {
        if (mNames.containsKey(languageCode)) {
            return mNames.get(languageCode).toString();
        } else if (mNames.containsKey("en")) {
            return mNames.get("en").toString();
        }
        return "";
    }

    public int getType() {
        return mType;
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
