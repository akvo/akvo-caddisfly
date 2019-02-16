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

package org.akvo.caddisfly.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Countdown timer view.
 * based on: https://github.com/maxwellforest/blog_android_timer
 */
public class TimerView extends View {

    private static final int BACKGROUND_COLOR = Color.argb(120, 180, 180, 200);
    private static final int ERASES_COLOR = Color.argb(180, 40, 40, 40);
    private static final int FINISH_ARC_COLOR = Color.argb(255, 0, 245, 120);
    private static final int ARC_START_ANGLE = 270; // 12 o'clock
    private static final float THICKNESS_SCALE = 0.1f;
    @NonNull
    private final Paint mCirclePaint;
    @NonNull
    private final Paint mArcPaint;
    private final Rect rectangle = new Rect();
    @NonNull
    private final Paint mEraserPaint;
    @NonNull
    private final Paint mTextPaint;
    private final Paint mCircleBackgroundPaint;
    private final Paint mSubTextPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private RectF mCircleOuterBounds;
    private RectF mCircleInnerBounds;
    private float mCircleSweepAngle = -1;
    private float mCircleFinishAngle = -1;
    private float mProgress;

    public TimerView(@NonNull Context context) {
        this(context, null);
    }

    public TimerView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int circleColor = Color.RED;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimerView);
            if (ta != null) {
                circleColor = ta.getColor(R.styleable.TimerView_circleColor, circleColor);
                ta.recycle();
            }
        }

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        mTextPaint.setTypeface(typeface);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.progressTextSize));

        mSubTextPaint = new Paint();
        mSubTextPaint.setAntiAlias(true);
        mSubTextPaint.setColor(Color.LTGRAY);
        Typeface subTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        mSubTextPaint.setTypeface(subTypeface);
        mSubTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.progressSubTextSize));

        mCircleBackgroundPaint = new Paint();
        mCircleBackgroundPaint.setAntiAlias(true);
        mCircleBackgroundPaint.setColor(BACKGROUND_COLOR);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(circleColor);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(FINISH_ARC_COLOR);

        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setColor(ERASES_COLOR);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Trick to make the view square
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        if (w != oldWidth || h != oldHeight) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(Color.TRANSPARENT);
            mCanvas = new Canvas(mBitmap);
        }

        super.onSizeChanged(w, h, oldWidth, oldHeight);
        updateBounds();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (mCircleSweepAngle > -1) {

            String text = String.valueOf((int) mProgress);
            mTextPaint.getTextBounds(text, 0, text.length(), rectangle);

            mCanvas.drawArc(mCircleOuterBounds, ARC_START_ANGLE, 360, true, mCircleBackgroundPaint);

            if (mCircleSweepAngle > mCircleFinishAngle) {
                mCanvas.drawArc(mCircleOuterBounds, ARC_START_ANGLE, mCircleSweepAngle, true, mCirclePaint);
                mCanvas.drawArc(mCircleOuterBounds, ARC_START_ANGLE, mCircleFinishAngle, true, mArcPaint);
            } else {
                mCanvas.drawArc(mCircleOuterBounds, ARC_START_ANGLE, mCircleSweepAngle, true, mArcPaint);
            }

            mCanvas.drawOval(mCircleInnerBounds, mEraserPaint);

            float width = mTextPaint.measureText(text);
            mCanvas.drawText(text, (canvas.getWidth() - width) / 2f,
                    ((canvas.getHeight() + Math.abs(rectangle.height())) / 2f) - 10, mTextPaint);

            int mainTextHeight = rectangle.height();

            String subText = getContext().getString(R.string.seconds);
            width = mSubTextPaint.measureText(subText);
            mSubTextPaint.getTextBounds(subText, 0, subText.length(), rectangle);
            mCanvas.drawText(subText, (canvas.getWidth() - width) / 2f,
                    ((canvas.getHeight() + Math.abs(rectangle.height())) / 2f) + mainTextHeight - 10, mSubTextPaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    public void setProgress(int progress, int max) {
        mProgress = progress;
        drawProgress(progress, max);
    }

    private void drawProgress(float progress, float max) {
        mCircleSweepAngle = (progress * 360) / max;
        mCircleFinishAngle = (Constants.GET_READY_SECONDS * 360) / max;
        invalidate();
    }

    private void updateBounds() {
        final float thickness = getWidth() * THICKNESS_SCALE;

        mCircleOuterBounds = new RectF(0, 0, getWidth(), getHeight());
        mCircleInnerBounds = new RectF(
                mCircleOuterBounds.left + thickness,
                mCircleOuterBounds.top + thickness,
                mCircleOuterBounds.right - thickness,
                mCircleOuterBounds.bottom - thickness);

        invalidate();
    }
}
