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

package org.akvo.caddisfly.sensor.striptest.widget;

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
import org.akvo.caddisfly.sensor.striptest.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.striptest.qrdetector.PerspectiveTransform;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;

import java.util.List;

/**
 * Created by linda on 9/9/15
 */
public class FinderPatternIndicatorView extends View {
    private static int mGridStepDisplay;
    private static int mGridStepImage;
    private static double mScore = 0.0f;
    private final int GRID_H = 15;
    private final int GRID_V = 15;
    private final Paint paint;
    private final Paint paint2;
    private final Bitmap arrowBitmap;
    private final Bitmap closerBitmap;
    private final Matrix matrix = new Matrix();
    private List<FinderPattern> patterns;
    private boolean shadowGrid[][];
    private int previewScreenHeight;
    private int previewScreenWidth;
    private int decodeHeight;
    private int decodeWidth;
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

        paint2 = new Paint();
        paint2.setColor(Color.BLUE);
        paint2.setAlpha(125);
        paint2.setAntiAlias(false);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);
        closerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closer);
    }

    // Sets the various sizes.
    // screenWidth: actual size of the reported preview screen
    // screenHeight: actual size of the reported preview screen
    // previewWidth: size of the image as taken by the camera
    // previewHeight: size of the image as taken by the camera
    // The crop factor (CROP_FINDER_PATTERN_FACTOR) is about 0.75
    public void setMeasure(int screenWidth, int screenHeight, int previewWidth, int previewHeight) {
        this.previewScreenHeight = screenHeight;
        this.previewScreenWidth = screenWidth;
        this.mFinderPatternViewWidth = screenWidth;
        this.decodeWidth = previewWidth;
        this.decodeHeight = previewHeight;
        this.mFinderPatternViewHeight = (int) Math.round(screenWidth * Constants.CROP_FINDER_PATTERN_FACTOR);

        // we divide the previewHeight into a number of parts
        mGridStepDisplay = Math.round(screenWidth / GRID_H);
        mGridStepImage = Math.round(previewHeight / GRID_H);
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
     * height and width are the actual size of the image as it came from the camera
      */
    public void showPatterns(List<FinderPattern> patternList, int tilt, boolean showTiltMessage, boolean showDistanceMessage, int width, int height) {
        this.patterns = patternList;
        this.decodeWidth = width;
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
        mScore = (4 * mScore) / 5.0;
        invalidate();
    }

    public void showShadow(List<float[]> shadowPoints, PerspectiveTransform cardToImageTransform) {
        shadowGrid = new boolean[GRID_H + 5][GRID_V + 5];
        int xGrid;
        int yGrid;
        if (shadowPoints != null) {
            // the points are in the coordinate system of the card (mm)
            for (float[] point : shadowPoints) {
                float[] points = new float[]{point[0], point[1]};
                // cardToImageTransform transforms from card coordinates (mm) to camera image coordinates
                cardToImageTransform.transformPoints(points);
                xGrid = (int) Math.max(0, Math.floor((this.decodeHeight - points[1]) / mGridStepImage));
                yGrid = (int) Math.floor(points[0] / mGridStepImage);
                shadowGrid[xGrid][yGrid] = true;
            }
        }
        invalidate();
    }

//    public void setColor(int color) {
//        paint.setColor(color);
//    }
//
    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (patterns != null) {
            // The canvas has a rotation of 90 degrees with respect to the camera preview
            //Camera preview size is in landscape mode, canvas is in portrait mode
            //the width of the canvas corresponds to the height of the decodeSize.
            //float ratio = 1.0f * canvas.getWidth() / decodeHeight;
            float hratio = 1.0f * previewScreenWidth / decodeHeight;
            float vratio = 1.0f * previewScreenHeight / decodeWidth;

            for (int i = 0; i < patterns.size(); i++) {
                //The x of the canvas corresponds to the y of the pattern,
                //The y of the canvas corresponds to the x of the pattern.
                float x = previewScreenWidth - patterns.get(i).getY() * hratio;
                float y = patterns.get(i).getX() * vratio;
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
            float hratio = 1.0f * previewScreenWidth / decodeHeight;
            float vratio = 1.0f * previewScreenHeight / decodeWidth;
            float ratioRatio = vratio/hratio;
            int xtop;
            int ytop;
            for (int i = 0; i < GRID_H; i++) {
                for (int j = 0; j < GRID_V; j++) {
                    if (shadowGrid[i][j]) {
                        xtop = Math.round(i * mGridStepDisplay);
                        ytop = Math.round(j * mGridStepDisplay * ratioRatio);
                        canvas.drawRect(xtop, ytop, xtop + mGridStepDisplay, ytop + mGridStepDisplay, paint2);
                    }
                }
            }
        }
        super.onDraw(canvas);
    }
}
