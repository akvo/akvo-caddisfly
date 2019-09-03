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

import org.akvo.caddisfly.R;

public class CustomShapeButton extends View {

    private final Path mPath = new Path();
    private final Path markerPath = new Path();
    private final Paint yellowPaint = new Paint();
    private final Paint yellowSelectPaint = new Paint();
    private final Paint selectPaint = new Paint();
    private final Paint activePaint = new Paint();
    private final Paint disabledPaint = new Paint();
    private final Paint strokePaint = new Paint();
    private final Paint markerPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint disabledTextPaint = new Paint();

    private int colWidth;
    private int bottom;
    private int bottom3;
    private int bottom2;
    private int bottom1;
    private int area1 = 0;
    private int area2 = 0;
    private int area3 = 0;
    private int area4 = 0;
    private int area5 = 0;

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
        yellowPaint.setStyle(Paint.Style.FILL);
        yellowPaint.setColor(Color.rgb(255, 238, 170));
        yellowSelectPaint.setColor(Color.rgb(255, 248, 180));

        disabledPaint.setColor(Color.rgb(69, 159, 159));

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
        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
        textPaint.setTextSize(sizeInPx);

        disabledTextPaint.setStyle(Paint.Style.FILL);
        disabledTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        disabledTextPaint.setColor(Color.rgb(120, 120, 120));
        disabledTextPaint.setTextSize(sizeInPx);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_UP) {

            if (x < colWidth && y < bottom3) {
                area1 = toggle(area1);
            } else if (x < colWidth * 2 && y < bottom) {
                area2 = toggle(area2);
            } else if (x > colWidth * 4 && y < bottom1) {
                area5 = toggle(area5);
            } else if (x > colWidth * 3 && x < colWidth * 4 && y < bottom2) {
                area4 = toggle(area4);
            } else if ((x > colWidth * 2 && x < colWidth * 3)
                    || (x > colWidth * 3 && y > bottom2 && x < (colWidth * 4) + (colWidth / (double) 3))) {
                area3 = toggle(area3);
            }

            active1 = false;
            active2 = false;
            active3 = false;
            active4 = false;
            active5 = false;
            invalidate();

            performClick();

            return true;
        } else if (action == MotionEvent.ACTION_DOWN) {

            if (x < colWidth && y < bottom3) {
                active1 = true;
            } else if (x < colWidth * 2 && y < bottom) {
                active2 = true;
            } else if (x > colWidth * 4 && y < bottom1) {
                active5 = true;
            } else if (x > colWidth * 3 && x < colWidth * 4 && y < bottom2) {
                active4 = true;
            } else if ((x > colWidth * 2 && x < colWidth * 3)
                    || (x > colWidth * 3 && y > bottom2 && x < (colWidth * 4) + (colWidth / (double) 3))) {
                active3 = true;
            }

            invalidate();
            return true;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            active1 = false;
            active2 = false;
            active3 = false;
            active4 = false;
            active5 = false;
            invalidate();
            return true;
        }

        getKey();

        return false;
    }

    private int toggle(int area1) {
        if (area1 != 2) {
            if (area1 == 0) {
                return 1;
            } else {
                return 0;
            }
        }
        return area1;
    }

    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();

        // Handle the action for the custom click here

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        int lineTop = 60;
        int fillLine = 80;
        int left = 0;
        colWidth = (getWidth() / 5);

        bottom = (int) (getHeight() - strokePaint.getStrokeWidth());

        int fillHeight = bottom - fillLine;
        bottom1 = (int) (fillHeight * 0.2) + lineTop;
        bottom2 = (int) (fillHeight * 0.36) + lineTop;
        bottom3 = (int) (fillHeight * 0.64) + lineTop;

        if (rect1 == null) {
            rect1 = new Rect(left, fillLine, colWidth, bottom3);
        }

        canvas.drawRect(rect1, getPaint(area1, active1));

        canvas.drawRect(left, bottom3, colWidth, bottom, getPaint(area2, active2));

        canvas.drawRect(colWidth, fillLine, colWidth * 2, bottom, getPaint(area2, active2));

        canvas.drawRect(colWidth * 2, fillLine, colWidth * 3, bottom, getPaint(area3, active3));

        canvas.drawRect(colWidth * 3, bottom2, colWidth * 4 + (colWidth / 3), bottom, getPaint(area3, active3));

        canvas.drawRect(colWidth * 3, fillLine, colWidth * 4, bottom2, getPaint(area4, active4));

        canvas.drawRect(colWidth * 4, fillLine, colWidth * 5, bottom1, getPaint(area5, active5));

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
        mPath.lineTo(colWidth * 5, lineTop);

        canvas.drawPath(mPath, strokePaint);

        // Yellow marker on the top of the bag
        markerPath.moveTo(left, 30f);
        markerPath.lineTo(getWidth(), 30f);
        canvas.drawPath(markerPath, markerPaint);

        int halfWidth = colWidth / 2;
        canvas.drawText("1", halfWidth - 10, fillLine + (bottom3 / 2) - 10, getTextPaint(area1));
        canvas.drawText("2", colWidth - 10, bottom3 + ((bottom - bottom3) / 2), getTextPaint(area2));
        canvas.drawText("3", (colWidth * 3) + 20, fillLine + (bottom - bottom2), getTextPaint(area3));
        canvas.drawText("4", halfWidth - 10 + colWidth * 3, fillLine + (bottom2 / 2) - 20, getTextPaint(area4));
        canvas.drawText("5", halfWidth - 10 + colWidth * 4, fillLine + (bottom1 / 2) - 26, getTextPaint(area5));
    }

    public String getKey() {
        mKey = String.valueOf(area1)
                + area2
                + area3
                + area4
                + area5;

        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
        String[] values = mKey.split("");
        area1 = Integer.parseInt(values[1]);
        area2 = Integer.parseInt(values[2]);
        area3 = Integer.parseInt(values[3]);
        area4 = Integer.parseInt(values[4]);
        area5 = Integer.parseInt(values[5]);
    }

    private Paint getPaint(int value, boolean active) {
        switch (value) {
            case 0:
                return active ? yellowSelectPaint : yellowPaint;
            case 1:
                return active ? activePaint : selectPaint;
        }
        return disabledPaint;
    }

    private Paint getTextPaint(int value) {
        if (value == 2) {
            return disabledTextPaint;
        }
        return textPaint;
    }

    public void useBlueSelection(boolean value) {
        if (value) {
            selectPaint.setColor(Color.rgb(34, 206, 255));
            activePaint.setColor(Color.rgb(20, 170, 235));
        } else {
            selectPaint.setColor(Color.rgb(69, 159, 159));
            activePaint.setColor(Color.rgb(79, 165, 165));
        }
    }
}