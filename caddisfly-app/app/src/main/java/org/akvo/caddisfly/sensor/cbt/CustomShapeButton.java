package org.akvo.caddisfly.sensor.cbt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomShapeButton extends View {

    Path mPath = new Path();
    Path markerPath = new Path();
    Paint fillPaint = new Paint();
    Paint fillSelectPaint = new Paint();
    Paint greenPaint = new Paint();
    Paint greenSelectPaint = new Paint();
    Paint strokePaint = new Paint();
    Paint markerPaint = new Paint();
    Paint textPaint = new Paint();

    int fillLine;
    int colWidth;
    int bottom;
    int bottom3;
    int bottom2;
    int bottom1;
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
    private String mKey;

    public CustomShapeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        // fill
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(246, 226, 168));
        fillSelectPaint.setColor(Color.rgb(240, 220, 160));

        greenPaint.setColor(Color.rgb(176, 200, 200));
        greenSelectPaint.setColor(Color.rgb(166, 195, 195));

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.rgb(100, 100, 100));
        strokePaint.setStrokeWidth(5);

        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setColor(Color.rgb(219, 210, 39));
        markerPaint.setStrokeWidth(20);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.rgb(30, 30, 30));
        textPaint.setTextSize(45);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {

            float x = event.getX();
            float y = event.getY();

            if (x < colWidth && y < bottom3) {
                area1 = !area1;
                active1 = true;
            } else if (x < colWidth * 2 && y < bottom) {
                area2 = !area2;
                active2 = true;
            } else if (x > colWidth * 4 && y < bottom1) {
                area5 = !area5;
                active5 = true;
            } else if (x > colWidth * 3 && x < colWidth * 4 && y < bottom2) {
                area4 = !area4;
                active4 = true;
            } else if ((x > colWidth * 2 && x < colWidth * 3) ||
                    (x > colWidth * 3 && y > bottom2 && x < (colWidth * 4) + (colWidth / 3))) {
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

        getKey();

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int lineTop = 60;
        fillLine = 80;
        int left = 0;
        colWidth = (getWidth() / 5);
        int halfWidth = colWidth / 2;

        bottom = (int) (getHeight() - strokePaint.getStrokeWidth());

        int fillHeight = bottom - fillLine;
        bottom1 = (int) (fillHeight * 0.2) + lineTop;
        bottom2 = (int) (fillHeight * 0.36) + lineTop;
        bottom3 = (int) (fillHeight * 0.64) + lineTop;

        if (rect1 == null) {
            rect1 = new Rect(left, fillLine, colWidth, bottom3);
        }

        canvas.drawRect(rect1, area1 ? active1 ? greenSelectPaint : greenPaint : (active1 ? fillSelectPaint : fillPaint));

        canvas.drawRect(left, bottom3, colWidth, bottom, area2 ? (active2 ? greenSelectPaint : greenPaint) : (active2 ? fillSelectPaint : fillPaint));
        canvas.drawRect(colWidth, fillLine, colWidth * 2, bottom, area2 ? active2 ? greenSelectPaint : greenPaint : (active2 ? fillSelectPaint : fillPaint));

        canvas.drawRect(colWidth * 2, fillLine, colWidth * 3, bottom, area3 ? active3 ? greenSelectPaint : greenPaint : fillPaint);
        canvas.drawRect(colWidth * 3, bottom2, colWidth * 4 + (colWidth / 3), bottom, area3 ? active3 ? greenSelectPaint : greenPaint : fillPaint);

        canvas.drawRect(colWidth * 3, fillLine, colWidth * 4, bottom2, area4 ? active4 ? greenSelectPaint : greenPaint : fillPaint);

        canvas.drawRect(colWidth * 4, fillLine, colWidth * 5, bottom1, area5 ? active5 ? greenSelectPaint : greenPaint : fillPaint);

        mPath.moveTo(left, lineTop);
        mPath.lineTo(left, bottom);
        mPath.lineTo(colWidth * 2, bottom);
        mPath.lineTo(colWidth * 2, lineTop);
        mPath.moveTo(colWidth, lineTop);
        mPath.lineTo(colWidth, bottom3);
        mPath.lineTo(left, bottom3);

        mPath.moveTo(colWidth * 3, lineTop);
        mPath.lineTo(colWidth * 3, bottom2);
        mPath.lineTo((colWidth * 4) + (colWidth / 3), bottom2);
        mPath.lineTo((colWidth * 4) + (colWidth / 3), bottom);
        mPath.lineTo((colWidth * 2), bottom);

        mPath.moveTo(colWidth * 4, lineTop);
        mPath.lineTo(colWidth * 4, bottom2);

        mPath.moveTo(colWidth * 4, bottom1);
        mPath.lineTo(colWidth * 5, bottom1);

        canvas.drawPath(mPath, strokePaint);

        markerPath.moveTo(left, 30f);
        markerPath.lineTo(getWidth(), 30f);

        canvas.drawPath(markerPath, markerPaint);

        canvas.drawText("1", halfWidth - 10, fillLine + (bottom3 / 2) - 10, textPaint);
        canvas.drawText("2", colWidth - 10, bottom3 + ((bottom - bottom3) / 2), textPaint);
        canvas.drawText("3", (colWidth * 3) + 20, fillLine + (bottom - bottom2), textPaint);
        canvas.drawText("4", halfWidth - 10 + colWidth * 3, fillLine + (bottom2 / 2) - 20, textPaint);
        canvas.drawText("5", halfWidth - 10 + colWidth * 4, fillLine + (bottom1 / 2) - 20, textPaint);

    }

    public String getKey() {
        mKey = (area1 ? "1" : "0") +
                (area2 ? "1" : "0") +
                (area3 ? "1" : "0") +
                (area4 ? "1" : "0") +
                (area5 ? "1" : "0");

        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
        String[] values = mKey.split("");
        area1 = values[1].equals("1");
        area2 = values[2].equals("1");
        area3 = values[3].equals("1");
        area4 = values[4].equals("1");
        area5 = values[5].equals("1");
    }
}