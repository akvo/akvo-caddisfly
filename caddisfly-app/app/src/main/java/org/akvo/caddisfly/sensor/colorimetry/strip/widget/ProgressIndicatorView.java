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

package org.akvo.caddisfly.sensor.colorimetry.strip.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.akvo.caddisfly.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 11/16/15
 */
public class ProgressIndicatorView extends LinearLayout {

    private static final int FULLY_OPAQUE = 255;
    private final Bitmap checkedBox;
    private final Bitmap uncheckedBox;
    private final Bitmap background;
    @NonNull
    private final Context context;
    @NonNull
    private final Paint paint;
    @NonNull
    private final TextPaint textPaint;
    private final float horMargin;
    private boolean set;
    private List<Step> steps;
    private float verMargin;

    public ProgressIndicatorView(@NonNull Context context) {
        this(context, null);
    }

    public ProgressIndicatorView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false); //needed for invalidate() to work
        checkedBox = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        uncheckedBox = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box_light);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.unchecked_box_green);

        this.context = context;

        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);

        textPaint = new TextPaint();
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(context.getResources().getDimension(R.dimen.mediumTextSize));
        textPaint.setAntiAlias(true);

        horMargin = getResources().getDimension(R.dimen.activity_horizontal_margin);
        verMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
    }

    public void addStep(int timeLapse, String name) {
        if (steps == null) {
            steps = new ArrayList<>();
        }

        steps.add(new Step(timeLapse, name));
    }

    private void initView() {

        if (!set) {
            if (steps == null) {
                return;
            }

            //sort on timeLapse, shortest first
            Collections.sort(steps, new StepComparator());

            int layoutH = 0;
            for (int i = 0; i < steps.size(); i++) {
                ImageView img = new ImageView(context);
                img.setImageBitmap(uncheckedBox);
                img.setScaleType(ImageView.ScaleType.FIT_START);

                if (i == 0) {
                    verMargin = 0;
                } else {
                    verMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
                }
                img.setMinimumHeight(uncheckedBox.getHeight() + Math.round(verMargin));
                img.setPadding(0, Math.round(verMargin), 0, 0);

                addView(img);

                layoutH += uncheckedBox.getHeight() + Math.round(verMargin);

            }

            LayoutParams params = (LayoutParams) getLayoutParams();
            params.height = layoutH;
            setLayoutParams(params);

            requestLayout();
            invalidate();

            set = true;
        }
    }

    public void setStepsTaken(int stepsTaken) {

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                if (i <= stepsTaken) {
                    ((ImageView) getChildAt(i)).setImageBitmap(checkedBox);
                    steps.get(i).pictureTaken = true;
                }
            }
        }
    }

    public void setTimeLapsed() {
        invalidate();
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get width and height size and mode
        int wSpec = MeasureSpec.getSize(widthMeasureSpec);

        int hSpec = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(wSpec, hSpec);

        initView();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (steps == null) {
            return;
        }

        paint.setAlpha(FULLY_OPAQUE);

        canvas.save();
        for (int i = 0; i < steps.size(); i++) {

            if (i > 0) {
                canvas.translate(0f, verMargin);
            }

            String message;
            if (steps.get(i).pictureTaken) {
                canvas.drawBitmap(checkedBox, 0, 0, paint);
            } else {
                canvas.drawBitmap(background, 0, 0, paint);
            }

            message = steps.get(i).getName();

            float textHeight = Math.abs(textPaint.ascent()) - 3;
            float yPos = background.getHeight() / 2f + textHeight / 2f;
            canvas.drawText(message, background.getWidth() + horMargin, yPos, textPaint);

            canvas.translate(0f, background.getHeight());
        }

        canvas.restore();
    }

    private static class Step {
        private final String name;
        private final int timeLapse;
        private boolean pictureTaken = false;

        Step(int timeLapse, String name) {
            this.timeLapse = timeLapse;
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class StepComparator implements Comparator<Step>, Serializable {

        @Override
        public int compare(@NonNull Step lhs, @NonNull Step rhs) {
            if (lhs.timeLapse < rhs.timeLapse) {
                return -1;
            }
            if (lhs.timeLapse == rhs.timeLapse) {
                return 0;
            }

            return 1;
        }
    }
}
