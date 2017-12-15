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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

//ref: https://stackoverflow.com/questions/25628258/align-text-around-imagespan-center-vertical
public class CenteredImageSpan extends ImageSpan {
    private WeakReference<Drawable> mDrawableRef;

    public CenteredImageSpan(Context context, final int drawableRes) {
        super(context, drawableRes);
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end,
                       Paint.FontMetricsInt fontMetricsInt) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();
        if (fontMetricsInt != null) {
            Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
            int fontHeight = fmPaint.descent - fmPaint.ascent;
            int drHeight = rect.bottom - rect.top;
            int centerY = fmPaint.ascent + fontHeight / 2;

            fontMetricsInt.ascent = centerY - drHeight / 2;
            fontMetricsInt.top = fontMetricsInt.ascent;
            fontMetricsInt.bottom = centerY + drHeight / 2;
            fontMetricsInt.descent = fontMetricsInt.bottom;
        }
        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start,
                     int end, float x, int top, int y, int bottom,
                     Paint paint) {
        Drawable drawable = getCachedDrawable();
        canvas.save();
        Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
        int fontHeight = fmPaint.descent - fmPaint.ascent;
        int centerY = y + fmPaint.descent - fontHeight / 2;
        int transY = centerY - (drawable.getBounds().bottom - drawable.getBounds().top) / 2;
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }

    // Redefined locally because it is a private member from DynamicDrawableSpan
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null) {
            d = wr.get();
        }

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }
}
