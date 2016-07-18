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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;

/**
 * Created by linda on 11/5/15
 */
public class QualityCheckView extends View {

    final int NUMBER_OF_BARS = 6;
    private final Paint paint;
    float percentage = Float.NaN;
    private int green = 0;
    private int orange = 0;
    private int red = 0;

    public QualityCheckView(Context context) {
        this(context, null);
    }

    public QualityCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QualityCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        if (!isInEditMode()) {
            green = ContextCompat.getColor(context, R.color.spring_green);
            orange = ContextCompat.getColor(context, R.color.orange);
            red = ContextCompat.getColor(context, R.color.clear_bordeaux);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (percentage == Float.NaN) {
            return;
        }
        double number = fromPercentageToNumber();

//        if (number == Double.NaN) {
//            throw new NullPointerException("Method to get number must be overridden in child class.");
//        }

        canvas.save();
        //qualityChecksOK at half of the width the view is going to be
        //each 'dot' has a diameter of half the canvas height
        canvas.translate(canvas.getWidth() / 2 - ((canvas.getHeight() * 0.5f + 0.2f) * 2.5f), 0);
        for (double i = 0; i < NUMBER_OF_BARS; i++) {

            //reset color
            paint.setColor(Color.LTGRAY);

            //check if we have valid data
            if (number >= 0) {

                //change color depending on value of percentage
                if (i < 2) { //0 1

                    // paint red if qualityValue is lower than i+1
                    // so two red dots if qualityValue < 1
                    // one red dot (right, second) if qualityValue > 1
                    if (number <= i + 1)
                        paint.setColor(red);

                } else if (i < 4) { //2 3

                    // paint orange if qualityValue between 1 and 4
                    // so if qualityValue == 1.5 we get a red followed by orange dot,
                    // if qualityValue == 2.5 we get two orange dots
                    // if qualityValue == 3.5 we get an orange dot on the right, second of the two
                    if (number >= i - 1 && number <= i + 2)
                        paint.setColor(orange);

                } else { // 4 5

                    //paint green if qualityValue larger than 4
                    //so if qualityValue == 4.5 we get two green dots
                    // and if qualityValue == 6 also two
                    if (number > i) {

                        //if percentage exceeds optimum
                        if (number > NUMBER_OF_BARS) {
                            //option to show all circles with a yellow color
                            paint.setColor(Color.YELLOW);

                            //trick code into showing the two orange circles
                            //number = NUMBER_OF_BARS * 0.5;
                        } else {
                            paint.setColor(green);
                        }
                    }
                }

            }

            //draw a circle with the paint now in the right color
            canvas.drawCircle(
                    0,
                    canvas.getHeight() * 0.5f,
                    canvas.getHeight() * 0.25f,
                    paint);

            //move to the right: distance of circle radius plus little extra before drawing following circle
            canvas.translate(canvas.getHeight() * 0.5f + 0.2f, 0);
        }
        canvas.restore();

        super.onDraw(canvas);
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        invalidate();
    }

    double fromPercentageToNumber() {
        //Override in child class

        return Double.NaN;
    }
}
