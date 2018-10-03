package org.akvo.caddisfly.util;

import android.graphics.Color;

// http://myhexaville.com/2017/06/14/android-animated-status-bar-color/
public class AnimatedColor {
    private final int mStartColor, mEndColor;
    private final float[] mStartHSV, mEndHSV;
    private float[] mMove = new float[3];


    public AnimatedColor(int start, int end) {
        mStartColor = start;
        mEndColor = end;
        mStartHSV = toHSV(start);
        mEndHSV = toHSV(end);
    }

    public int with(float delta) {
        if (delta <= 0) return mStartColor;
        if (delta >= 1) return mEndColor;
        return Color.HSVToColor(move(delta));
    }

    private float[] move(float delta) {
        mMove[0] = (mEndHSV[0] - mStartHSV[0]) * delta + mStartHSV[0];
        mMove[1] = (mEndHSV[1] - mStartHSV[1]) * delta + mStartHSV[1];
        mMove[2] = (mEndHSV[2] - mStartHSV[2]) * delta + mStartHSV[2];
        return mMove;
    }

    private float[] toHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }
}