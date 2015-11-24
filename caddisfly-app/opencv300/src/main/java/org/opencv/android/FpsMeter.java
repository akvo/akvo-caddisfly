/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.opencv.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.core.Core;

import java.text.DecimalFormat;

public class FpsMeter {
    private static final String TAG = "FpsMeter";
    private static final int STEP = 20;
    private static final DecimalFormat FPS_FORMAT = new DecimalFormat("0.00");
    Paint mPaint;
    boolean mIsInitialized = false;
    int mWidth = 0;
    int mHeight = 0;
    private int mFramesCouner;
    private double mFrequency;
    private long mprevFrameTime;
    private String mStrfps;

    public void init() {
        mFramesCouner = 0;
        mFrequency = Core.getTickFrequency();
        mprevFrameTime = Core.getTickCount();
        mStrfps = "";

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setTextSize(20);
    }

    public void measure() {
        if (!mIsInitialized) {
            init();
            mIsInitialized = true;
        } else {
            mFramesCouner++;
            if (mFramesCouner % STEP == 0) {
                long time = Core.getTickCount();
                double fps = STEP * mFrequency / (time - mprevFrameTime);
                mprevFrameTime = time;
                if (mWidth != 0 && mHeight != 0)
                    mStrfps = FPS_FORMAT.format(fps) + " FPS@" + Integer.valueOf(mWidth) + "x" + Integer.valueOf(mHeight);
                else
                    mStrfps = FPS_FORMAT.format(fps) + " FPS";
                Log.i(TAG, mStrfps);
            }
        }
    }

    public void setResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void draw(Canvas canvas, float offsetx, float offsety) {
        Log.d(TAG, mStrfps);
        canvas.drawText(mStrfps, offsetx, offsety, mPaint);
    }

}
