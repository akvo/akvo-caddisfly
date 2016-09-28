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
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.PreviewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 11/16/15
 */
public class ProgressIndicatorView extends LinearLayout {

    private final Bitmap checked;
    private final Bitmap unchecked_light;
    private final Bitmap background;
    private final Context context;
    private final Paint paint;
    private final TextPaint textPaint;
    private final float horMargin;
    private boolean set;
    private boolean start;
    private List<Step> steps;
    private int stepsTaken = 0;
    private int timeLapsed = 0;
    private float verMargin;
    private boolean running = false;

    public ProgressIndicatorView(Context context) {
        this(context, null);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false); //needed for invalidate() to work
        checked = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        unchecked_light = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box_light);
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

    public void addStep(int timeLapse) {
        if (steps == null)
            steps = new ArrayList<>();

        steps.add(new Step(timeLapse));
    }

    private void initView() {

        if (!set) {
            if (steps == null)
                return;

            //sort on timeLapse, shortest first
            Collections.sort(steps, new StepComparator());

            int layoutH = 0;
            for (int i = 0; i < steps.size(); i++) {
                ImageView img = new ImageView(context);
                img.setImageBitmap(unchecked_light);
                img.setScaleType(ImageView.ScaleType.FIT_START);

                if (i == 0) {
                    verMargin = 0;
                } else {
                    verMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
                }
                img.setMinimumHeight(unchecked_light.getHeight() + Math.round(verMargin));
                img.setPadding(0, Math.round(verMargin), 0, 0);

                addView(img);

                layoutH += unchecked_light.getHeight() + Math.round(verMargin);

            }

            LayoutParams params = (LayoutParams) getLayoutParams();
            params.height = layoutH;
            setLayoutParams(params);

            requestLayout();
            invalidate();

            set = true;
        }
    }

    public void start() {
        this.start = true;
    }

    public void setStepsTaken(int stepsTaken) {

        this.stepsTaken = stepsTaken;

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                if (i <= stepsTaken) {
                    ((ImageView) getChildAt(i)).setImageBitmap(checked);
                    steps.get(i).pictureTaken = true;
                }
            }
        }
    }

    public void setTimeLapsed(int timeLapsed) {

        this.timeLapsed = timeLapsed;

        invalidate();

        if (!running && start)
            startAnim();
    }

    private void startAnim() {
        if (steps != null) {
            //sort on time lapse ascending
            Collections.sort(steps, new StepComparator());

            for (int i = 0; i < steps.size(); i++) {

                Animation blink = AnimationUtils.loadAnimation(context, R.anim.blink);
                blink.setDuration(Math.min(5000, steps.get(i).timeLapse * 1000));
                blink.setAnimationListener(new BlinkAnimListener(i));

                if (steps.get(i).getTimeLapse() - timeLapsed < 5) {
                    if (getChildCount() > 0 && getChildAt(i) != null) {
                        if (i >= stepsTaken) {
                            if (!steps.get(i).animationEnded) {
                                getChildAt(i).startAnimation(blink);
                            }

                        } else {
                            getChildAt(i).clearAnimation();
                        }
                    }
                }
            }
        }
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
    public void onDraw(Canvas canvas) {
        if (steps == null)
            return;

        if (!start)
            return;

        paint.setAlpha(255);

        canvas.save();
        for (int i = 0; i < steps.size(); i++) {

            if (i > 0) {
                canvas.translate(0f, verMargin);
            }

            String message;
            if (steps.get(i).pictureTaken) {
                message = getContext().getString(R.string.picture_taken);
                canvas.drawBitmap(checked, 0, 0, paint);

            } else if (steps.get(i).animationEnded) {
                message = getContext().getString(R.string.ready_for_picture) + " " + String.valueOf(i + 1);
                canvas.drawBitmap(background, 0, 0, paint);

            } else {

                canvas.drawBitmap(background, 0, 0, paint);

                // If the previous step is finished, either picture taken or not, we start the counter
                if (i > 0) {
                    if (steps.get(i - 1).animationEnded) {
                        try {
                            message = getContext().getString(R.string.waiting) + " " + PreviewUtil.fromSecondsToMMSS(
                                    Math.max(0, steps.get(i).getTimeLapse() - timeLapsed)) + " sec. ";
                        } catch (Exception e) {
                            message = e.getMessage();
                        }
                    } else {
                        message = "";

                    }
                } else {
                    //first one does have a count
                    try {
                        message = getContext().getString(R.string.waiting) + " " + PreviewUtil.fromSecondsToMMSS(Math.max(0, steps.get(i).getTimeLapse() - timeLapsed)) + " sec. ";
                    } catch (Exception e) {
                        message = e.getMessage();
                    }
                }
            }

            float textHeight = Math.abs(textPaint.ascent());
            float yPos = background.getHeight() / 2 + textHeight / 2;
            canvas.drawText(message, background.getWidth() + horMargin, yPos, textPaint);

            canvas.translate(0f, background.getHeight());
        }

        canvas.restore();
    }

    private class BlinkAnimListener implements Animation.AnimationListener {

        private final int i;

        BlinkAnimListener(int i) {
            this.i = i;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            running = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            steps.get(i).animationEnded = true;
            running = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class Step {
        final int timeLapse;
        boolean animationEnded = false;
        boolean pictureTaken = false;

        Step(int timeLapse) {
            this.timeLapse = timeLapse;
        }

        int getTimeLapse() {
            return timeLapse;
        }
    }

    private class StepComparator implements Comparator<Step> {

        @Override
        public int compare(Step lhs, Step rhs) {
            if (lhs.timeLapse < rhs.timeLapse)
                return -1;
            if (lhs.timeLapse == rhs.timeLapse)
                return 0;

            return 1;
        }
    }
}
