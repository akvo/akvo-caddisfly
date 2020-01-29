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
package org.akvo.caddisfly.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.akvo.caddisfly.R
import kotlin.math.ceil

class PageIndicatorView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null,
            defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val fillPaint: Paint
    private val strokePaint: Paint
    private var distanceBetweenBullets = 36f
    private var bulletRadius = 8
    private var activeBulletRadius = 0f
    private var pageCount = 0
    private var activePage = 0
    private var showDots = false
    fun setPageCount(value: Int) {
        pageCount = value
        invalidate()
        if (pageCount < 3) {
            distanceBetweenBullets += bulletRadius.toFloat()
            activeBulletRadius = bulletRadius * 1.2f
        } else if (pageCount > 12) {
            distanceBetweenBullets -= 4f
        }
    }

    fun setActiveIndex(value: Int) {
        activePage = value
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(ceil(distanceBetweenBullets * pageCount.toDouble()).toInt()
                + (activeBulletRadius / 2).toInt(), heightMeasureSpec)
    }

    public override fun onDraw(canvas: Canvas) {
        val margin = distanceBetweenBullets / 4
        if (pageCount > 1) {
            for (i in 0 until pageCount) {
                if (activePage == i) {
                    canvas.drawCircle((distanceBetweenBullets * i + bulletRadius * 2) + margin,
                            height / 2f, activeBulletRadius, fillPaint)
                } else {
                    if (showDots) {
                        canvas.drawCircle((distanceBetweenBullets * i + bulletRadius * 2) + margin,
                                height / 2f, bulletRadius / 2f, fillPaint)
                    } else {
                        canvas.drawCircle((distanceBetweenBullets * i + bulletRadius * 2) + margin,
                                height / 2f, bulletRadius.toFloat(), strokePaint)
                    }
                }
            }
        }
    }

    fun showDots(value: Boolean) {
        showDots = value
        val scale = resources.displayMetrics.density
        setActiveBulletSize(value, scale)
    }

    private fun setActiveBulletSize(dots: Boolean, scale: Float) {
        activeBulletRadius = if (scale <= 1.5) {
            bulletRadius * 1.6f
        } else if (scale >= 3) {
            bulletRadius * 1.4f
        } else {
            if (dots) {
                bulletRadius * 1.4f
            } else {
                bulletRadius * 1.8f
            }
        }
    }

    init {
        val scale = resources.displayMetrics.density
        if (scale <= 1.5) {
            distanceBetweenBullets = 26f
            bulletRadius = 4
        } else if (scale >= 3) {
            distanceBetweenBullets = 46f
            bulletRadius = 12
        }
        setActiveBulletSize(false, scale)
        fillPaint = Paint()
        fillPaint.style = Paint.Style.FILL_AND_STROKE
        fillPaint.strokeWidth = 2f
        fillPaint.color = ContextCompat.getColor(context, R.color.colorAccent)
        fillPaint.isAntiAlias = true
        strokePaint = Paint()
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 2f
        strokePaint.color = ContextCompat.getColor(context, R.color.colorAccent)
        strokePaint.isAntiAlias = true
    }
}