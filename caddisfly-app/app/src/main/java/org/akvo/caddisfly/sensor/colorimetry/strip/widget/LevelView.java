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

package org.akvo.caddisfly.sensor.colorimetry.strip.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

/**
 * Created by linda on 10/27/15
 */
public class LevelView extends View {
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private final Paint drawPaint;
    private final Bitmap arrowBitmap;
    private float[] tilts;

    public LevelView(Context context) {
        this(context, null);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        drawPaint = new Paint();
        drawPaint.setAntiAlias(false);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (tilts != null) {
            float degrees = getDegrees(tilts);
            if (degrees != 0) {
                canvas.save();
                canvas.rotate(degrees, canvas.getWidth() / 2f, canvas.getHeight() / 2f);
                canvas.drawBitmap(arrowBitmap, 0, 0, drawPaint);
                canvas.restore();
            }
        }
        super.onDraw(canvas);
    }

    public void setAngles(float[] tiltValues) {
        this.tilts = tiltValues == null ? null : tiltValues.clone();
        invalidate();
    }

    private float getDegrees(float[] tiltValues) {
        float degrees = 0f;

        // if the horizontal tilt is too large, indicate it
        if (Math.abs(tiltValues[0] - 1) > Constant.MAX_TILT_DIFF) {
            degrees = tiltValues[0] - 1 < 0 ? -DEGREES_90 : DEGREES_90;
        }

        // if the vertical tilt is too large, indicate it
        if (Math.abs(tiltValues[1] - 1) > Constant.MAX_TILT_DIFF) {
            degrees = tiltValues[1] - 1 < 0 ? DEGREES_180 : 1;
        }
        return degrees;
    }
}
