package org.akvo.caddisfly.sensor.cbt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomShapeButton extends View {

    Path mPath = new Path();
    Paint fillPaint = new Paint();
    Paint strokePaint = new Paint();

    public CustomShapeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // fill
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.YELLOW);

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(5);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_UP) {

        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int width = this.getWidth() / 5;
        int height = (int) (this.getHeight() - strokePaint.getStrokeWidth());
        int halfHeight = this.getHeight() / 2;
        int thirdHeight = this.getHeight() / 3;
        int fifthHeight = this.getHeight() / 5;

        canvas.drawRect(0, 0, width, halfHeight, fillPaint);

        canvas.drawRect(0, halfHeight, width, height, fillPaint);
        canvas.drawRect(width, 0, width * 2, height, fillPaint);

        canvas.drawRect(width * 2, 0, width * 3, height, fillPaint);
        canvas.drawRect(width * 3, thirdHeight, width * 4 + (width / 3), height, fillPaint);

        canvas.drawRect(width * 3, 0, width * 4, thirdHeight, fillPaint);

        canvas.drawRect(width * 4, 0, width * 5, fifthHeight, fillPaint);

        mPath.moveTo(0f, 0f);
        mPath.lineTo(0f, height);
        mPath.lineTo(width * 2, height);
        mPath.lineTo(width * 2, 0f);
        mPath.moveTo(width, 0f);
        mPath.lineTo(width, halfHeight);
        mPath.lineTo(0f, halfHeight);

        mPath.moveTo(width * 3, 0f);
        mPath.lineTo(width * 3, thirdHeight);
        mPath.lineTo((width * 4) + (width / 3), thirdHeight);
        mPath.lineTo((width * 4) + (width / 3), height);
        mPath.lineTo((width * 2), height);

        mPath.moveTo(width * 4, 0f);
        mPath.lineTo(width * 4, thirdHeight);

        mPath.moveTo(width * 4, fifthHeight);
        mPath.lineTo(width * 5, fifthHeight);

        canvas.drawPath(mPath, strokePaint);

    }

    public String getKey() {
        return "10101";
    }
}