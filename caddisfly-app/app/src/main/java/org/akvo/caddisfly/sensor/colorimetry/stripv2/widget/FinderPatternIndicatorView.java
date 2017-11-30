/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.colorimetry.stripv2.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.qrdetector.PerspectiveTransform;

import java.util.List;

/**
 * Created by linda on 9/9/15
 */
public class FinderPatternIndicatorView extends View {
    private static int mGridStep;
    //    private static Float mDeltaE;
    private static double mScore = 0.0f;
    private final int GRID_H = 15;
    private final int GRID_V = 15;
    private final Paint paint;
    //    private final Paint paint2;
    private final Paint paint3;
    //    private final Paint paint5;
//    private final Paint paint6;
//    private final Paint paint7;
    private final Bitmap arrowBitmap;
    private final Bitmap closerBitmap;
    //    PerspectiveTransform cardToImageTransform;
    //    CalibrationCardData calCardData;
    private Matrix matrix = new Matrix();
    //    private Paint paint4;
    private List<FinderPattern> patterns;
    private boolean shadowGrid[][];
    //    private Map<String, int[]> calibratedPatchRGB;
//    private Map<String, int[]> calibrationPatchRGB;
    private int decodeHeight;
    private int mFinderPatternViewWidth = 0;
    private int mFinderPatternViewHeight = 0;
    private int tilt = DecodeProcessor.NO_TILT;
    private boolean showDistanceMessage = false;
    private boolean showTiltMessage;

    public FinderPatternIndicatorView(Context context) {
        this(context, null);
    }

    public FinderPatternIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FinderPatternIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(false);

//        paint2 = new Paint();
//        paint2.setColor(Color.RED);
//        paint2.setTextAlign(Paint.Align.CENTER);
//        paint2.setTextSize(34);
//        paint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//        paint2.setAntiAlias(false);

        paint3 = new Paint();
        paint3.setColor(Color.BLUE);
        paint3.setAlpha(125);
        paint3.setAntiAlias(false);

//        paint4 = new Paint();
//        paint4.setAlpha(255);
//        paint4.setAntiAlias(false);

//        paint5 = new Paint();
//        paint5.setColor(Color.GREEN);
//        paint5.setAntiAlias(false);
//        paint5.setAlpha(180);
//        paint5.setStyle(Paint.Style.STROKE);
//        paint5.setStrokeWidth(4);

//        paint6 = new Paint();
//        paint6.setColor(Color.GREEN);
//        paint6.setAlpha(180);
//        paint6.setAntiAlias(false);

//        paint7 = new Paint();
//        paint7.setAntiAlias(false);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);
        closerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closer);
    }

    // the measure is set with the surfaceTextureWidth, and a crop factor (CROP_FINDER_PATTERN_FACTOR)
    // times the width. The crop factor is about 0.75
    public void setMeasure(int width, int height) {
        this.mFinderPatternViewWidth = width;
        this.mFinderPatternViewHeight = height;
//        mTargetSize = (int) Math.round(mFinderPatternViewWidth * 0.11);
        mGridStep = Math.round(mFinderPatternViewWidth / GRID_H);
//        mBarwidth = (int) Math.round(mFinderPatternViewWidth * 0.05);

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mFinderPatternViewWidth || 0 == mFinderPatternViewHeight) {
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(mFinderPatternViewWidth, mFinderPatternViewHeight);
        }
    }

    /*
    * display finder patterns on screen as green dots.
     */
    public void showPatterns(List<FinderPattern> patternList, int tilt, boolean showTiltMessage, boolean showDistanceMessage, int width, int height) {
        this.patterns = patternList;
//        int decodeWidth = width;
        this.decodeHeight = height;
        this.tilt = tilt;
        this.showDistanceMessage = showDistanceMessage;
        this.showTiltMessage = showTiltMessage;
        invalidate();
    }

    public void clearAll() {
        this.patterns = null;
        this.tilt = DecodeProcessor.NO_TILT;
        this.showDistanceMessage = false;
        this.showTiltMessage = false;
        this.shadowGrid = null;
//        this.calibratedPatchRGB = null;
//        this.calibrationPatchRGB = null;
//        mDeltaE = null;
        mScore = (4 * mScore) / 5.0;
        invalidate();
    }

    public void showShadow(List<float[]> shadowPoints, float percentage, PerspectiveTransform cardToImageTransform) {
//        this.cardToImageTransform = cardToImageTransform;
        shadowGrid = new boolean[GRID_H + 5][GRID_V + 5];
        int xGrid, yGrid;
        if (shadowPoints != null) {
            for (float[] point : shadowPoints) {
                float[] points = new float[]{point[0], point[1]};
                cardToImageTransform.transformPoints(points);
                xGrid = (int) Math.max(0, Math.floor((mFinderPatternViewWidth - points[1]) / mGridStep));
                yGrid = (int) Math.floor(points[0] / mGridStep);
                shadowGrid[xGrid][yGrid] = true;
            }
        }
        invalidate();
    }

//    public void clearShadow() {
//        this.shadowGrid = null;
//        invalidate();
//    }
//
//    public void showCalibration(Map<String, int[]> measuredPatchRGB, Map<String, int[]> calibrationPatchRGB,
//                                CalibrationCardData calCardData, float[] deltaEStats) {
////        this.calibratedPatchRGB = measuredPatchRGB;
////        this.calibrationPatchRGB = calibrationPatchRGB;
//        this.calCardData = calCardData;
//        mDeltaE = deltaEStats[1];
//        // we consider anything lower than 2.5 to be good.
//        // anything higher than 4.5 is red.
//        double score;
//        score = Math.round(9.0 * (1 - Math.min(1.0, (Math.max(0, mDeltaE - 2.5) / 2.0))));
//
//        mScore = (3 * mScore + score) / 4.0;
//        invalidate();
//    }


    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
//        int w = mFinderPatternViewWidth;
//        int s = mTargetSize;
//        int startheight = (int) Math.round((w - s) * Constants.FINDER_PATTERN_ASPECT);

        if (patterns != null) {
            // The canvas has a rotation of 90 degrees with respect to the camera preview
            //Camera preview size is in landscape mode, canvas is in portrait mode
            //the width of the canvas corresponds to the height of the decodeSize.
            float ratio = 1.0f * canvas.getWidth() / decodeHeight;

            for (int i = 0; i < patterns.size(); i++) {
                //The x of the canvas corresponds to the y of the pattern,
                //The y of the canvas corresponds to the x of the pattern.
                float x = canvas.getWidth() - patterns.get(i).getY() * ratio;
                float y = patterns.get(i).getX() * ratio;
                canvas.drawCircle(x, y, 10, paint);
            }
        }

        if (showTiltMessage) {
            matrix.reset();
            matrix.postTranslate(-arrowBitmap.getWidth() / 2, -arrowBitmap.getHeight() / 2); // Centers image
            matrix.postRotate(tilt);
            matrix.postTranslate(mFinderPatternViewWidth / 2f, mFinderPatternViewHeight / 2f);
            canvas.drawBitmap(arrowBitmap, matrix, null);
        }

        if (showDistanceMessage) {
            matrix.reset();
            matrix.postTranslate(-closerBitmap.getWidth() / 2, -closerBitmap.getHeight() / 2); // Centers image
            matrix.postTranslate(mFinderPatternViewWidth / 2f, mFinderPatternViewHeight / 2.5f);
            canvas.drawBitmap(closerBitmap, matrix, null);
        }

        if (shadowGrid != null) {
            float ratio = 1.0f * canvas.getWidth() / decodeHeight;
            int xtop;
            int ytop;
            for (int i = 0; i < GRID_H; i++) {
                for (int j = 0; j < GRID_V; j++) {
                    if (shadowGrid[i][j]) {
                        xtop = Math.round(i * mGridStep * ratio);
                        ytop = Math.round(j * mGridStep * ratio);
                        canvas.drawRect(xtop, ytop, xtop + mGridStep, ytop + mGridStep, paint3);
                    }
                }
            }
        }

//        if (calibratedPatchRGB != null) {
//            float ratio = 1.0f * canvas.getWidth() / decodeHeight;
//            for (String label : this.calibratedPatchRGB.keySet()){
//                int[] RGB = this.calibratedPatchRGB.get(label);
//                CalibrationCardData.Location calLoc= this.calCardData.getLocations().get(label);
//                float[] points = new float[]{calLoc.x,calLoc.y};
//                cardToImageTransform.transformPoints(points);
//                paint4.setARGB(255,RGB[0],RGB[1],RGB[2]);
//                canvas.drawCircle(canvas.getWidth()- points[1] * ratio, points[0] * ratio, 15, paint4);
//            }
//        }
//
//        if (calibrationPatchRGB != null) {
//            float ratio = 1.0f * canvas.getWidth() / decodeHeight;
//            for (String label : this.calibrationPatchRGB.keySet()){
//                int[] RGB = this.calibrationPatchRGB.get(label);
//                CalibrationCardData.Location calLoc= this.calCardData.getLocations().get(label);
//                float[] points = new float[]{calLoc.x,calLoc.y};
//                cardToImageTransform.transformPoints(points);
//                paint4.setARGB(255,RGB[0],RGB[1],RGB[2]);
//                canvas.drawCircle(canvas.getWidth()- points[1] * ratio, points[0] * ratio, 10, paint4);
//            }
//        }

        super.onDraw(canvas);
    }

//    private boolean inRect(float x, float y, int xtl, int ytl, int xbr, int ybr) {
//        return (xtl < x && x < xbr && ytl < y && y < ybr);
//    }

}
