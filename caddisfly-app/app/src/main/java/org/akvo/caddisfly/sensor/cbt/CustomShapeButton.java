package org.akvo.caddisfly.sensor.cbt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomShapeButton extends View {

    Path mPath = new Path();
    Paint fillPaint = new Paint();
    Paint fillSelectPaint = new Paint();
    Paint greenPaint = new Paint();
    Paint greenSelectPaint = new Paint();
    Paint strokePaint = new Paint();
    Paint textPaint = new Paint();

    int top;
    int width;
    int height;
    int halfHeight;
    int thirdHeight;
    int fifthHeight;
    private boolean area1 = false;
    private boolean area2 = false;
    private boolean area3 = false;
    private boolean area4 = false;
    private boolean area5 = false;

    private boolean active1 = false;
    private boolean active2 = false;
    private boolean active3 = false;
    private boolean active4 = false;
    private boolean active5 = false;

    private Rect rect1;

    public CustomShapeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // fill
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(242, 224, 33));
        fillSelectPaint.setColor(Color.rgb(226, 210, 31));

        greenPaint.setColor(Color.rgb(16, 201, 136));
        greenSelectPaint.setColor(Color.rgb(14, 183, 124));

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.rgb(100, 100, 100));
        strokePaint.setTextSize(20);
        strokePaint.setStrokeWidth(5);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.rgb(30, 30, 30));
        textPaint.setTextSize(40);
        textPaint.setStrokeWidth(2);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {

            float x = event.getX();
            float y = event.getY();

            if (x < width && y < halfHeight) {
                area1 = !area1;
                active1 = true;
            } else if (x < width * 2 && y < height) {
                area2 = !area2;
                active2 = true;
            } else if (x > width * 4 && y < fifthHeight) {
                area5 = !area5;
                active5 = true;
            } else if (x > width * 3 && x < width * 4 && y < thirdHeight) {
                area4 = !area4;
                active4 = true;
            } else if ((x > width * 2 && x < width * 3) ||
                    (x > width * 3 && y > thirdHeight && x < (width * 4) + (width / 3))) {
                area3 = !area3;
                active3 = true;
            }

            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            active1 = false;
            active2 = false;
            active3 = false;
            active4 = false;
            active5 = false;
            invalidate();
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        top = 40;
        width = this.getWidth() / 5;
        int halfWidth = width / 2;
        height = (int) (this.getHeight() - strokePaint.getStrokeWidth());
        halfHeight = this.getHeight() / 2;
        thirdHeight = this.getHeight() / 3;
        fifthHeight = this.getHeight() / 5;

        if (rect1 == null) {
            rect1 = new Rect(0, top, width, halfHeight);
        }

        canvas.drawRect(rect1, area1 ? active1 ? greenSelectPaint : greenPaint : ( active1 ? fillSelectPaint : fillPaint));

        canvas.drawRect(0, halfHeight, width, height, area2 ? (active2 ? greenSelectPaint : greenPaint) : ( active2 ? fillSelectPaint : fillPaint));
        canvas.drawRect(width, top, width * 2, height, area2 ? active2 ? greenSelectPaint : greenPaint : ( active2 ? fillSelectPaint : fillPaint));

        canvas.drawRect(width * 2, top, width * 3, height, area3 ? active3 ? greenSelectPaint : greenPaint : fillPaint);
        canvas.drawRect(width * 3, thirdHeight, width * 4 + (width / 3), height, area3 ? active3 ? greenSelectPaint : greenPaint : fillPaint);

        canvas.drawRect(width * 3, top, width * 4, thirdHeight, area4 ? active4 ? greenSelectPaint : greenPaint : fillPaint);

        canvas.drawRect(width * 4, top, width * 5, fifthHeight, area5 ? active5 ? greenSelectPaint : greenPaint : fillPaint);

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

        canvas.drawText("1", halfWidth - 10, 90, textPaint);
        canvas.drawText("2", halfWidth - 10 + width, 90, textPaint);
        canvas.drawText("3", halfWidth - 10 + width * 2, 90, textPaint);
        canvas.drawText("4", halfWidth - 10 + width * 3, 90, textPaint);
        canvas.drawText("5", halfWidth - 10 + width * 4, 90, textPaint);

    }

    public String getKey() {
        return (area1 ? "1" : "0") +
                (area2 ? "1" : "0") +
                (area3 ? "1" : "0") +
                (area4 ? "1" : "0") +
                (area5 ? "1" : "0");
    }
}