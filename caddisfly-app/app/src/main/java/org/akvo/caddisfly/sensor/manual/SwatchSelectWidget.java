package org.akvo.caddisfly.sensor.manual;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.akvo.caddisfly.R;

import java.util.Locale;

public class SwatchSelectWidget extends View {

    //    private final Path mPath = new Path();
    //    private final Paint fillPaint = new Paint();
//    private final Paint fillSelectPaint = new Paint();
    private final Paint buttonPaint = new Paint();
    //    private final Paint greenSelectPaint = new Paint();
    private final Paint strokePaint = new Paint();
    //    private final Paint markerPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint backgroundPaint = new Paint();
    private final Paint textBoxPaint = new Paint();
    private final Paint nameTextPaint = new Paint();
    private final Paint subTitlePaint = new Paint();
    //    Rect textRect;
    Rect nameBounds = new Rect();
    private boolean area1 = false;
    private boolean area2 = false;
    private boolean area3 = false;
    private boolean area4 = false;
    private boolean area5 = false;
    private boolean active1 = false;
    private Rect rect1;
    private String mKey;
    private int buttonWidth;
    private int buttonHeight;
    private int textBoxWidth;
    private int gutterWidth = 10;
    private int radius;
//    /**
//     * Sets the text size for a Paint object so a given string of text will be a
//     * given width.
//     *
//     * @param paint        the Paint to set the text size for
//     * @param desiredWidth the desired width
//     * @param text         the text that should be that width
//     */
//    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
//
//        final float testTextSize = 48f;
//
//        // Get the bounds of the text, using our testTextSize.
//        paint.setTextSize(testTextSize);
//        Rect bounds = new Rect();
//        paint.getTextBounds(text, 0, text.length(), bounds);
//
//        // Calculate the desired size as a proportion of our testTextSize.
//        float desiredTextSize = testTextSize * desiredWidth / bounds.width();
//
//        // Set the paint for that size.
//        paint.setTextSize(desiredTextSize);
//    }

    public SwatchSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.rgb(100, 100, 100));
        strokePaint.setStrokeWidth(5);

        // fill
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(130, 130, 130));

        textBoxPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textBoxPaint.setColor(Color.rgb(255, 255, 255));
        textBoxPaint.setStrokeWidth(1);

        nameTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        nameTextPaint.setColor(Color.rgb(50, 50, 50));
        nameTextPaint.setStrokeWidth(2);
        nameTextPaint.setAntiAlias(true);
//        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
        nameTextPaint.setTextSize(70);

        subTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        subTitlePaint.setColor(Color.rgb(50, 50, 50));
        subTitlePaint.setStrokeWidth(1);
        subTitlePaint.setAntiAlias(true);
//        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
        subTitlePaint.setTextSize(40);


        // fill
//        fillPaint.setStyle(Paint.Style.FILL);
//        fillPaint.setColor(Color.rgb(229, 239, 97));
//        fillSelectPaint.setColor(Color.rgb(240, 250, 97));

        buttonPaint.setColor(Color.rgb(200, 200, 200));
        buttonPaint.setAntiAlias(true);
//        greenSelectPaint.setColor(Color.rgb(200, 200, 200));
//
//        markerPaint.setStyle(Paint.Style.STROKE);
//        markerPaint.setColor(Color.rgb(219, 210, 39));
//        markerPaint.setStrokeWidth(20);

        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.rgb(30, 30, 30));
        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
        textPaint.setTextSize(sizeInPx);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            invalidate();

            return true;
        } else if (action == MotionEvent.ACTION_UP) {

            active1 = false;
            invalidate();

            performClick();

            return true;
        }

        getKey();

        return false;
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

        int left = 0;
        int top = 6;
        int verticalMargin = 20;
        int horizontalMargin = 20;
        int right = getWidth();
        int bottom = (int) (getHeight() - strokePaint.getStrokeWidth());

        buttonWidth = getMeasuredWidth() / 5;
        textBoxWidth = getMeasuredWidth() / 6;
        buttonHeight = getMeasuredHeight() / 10;
        radius = buttonHeight / 2;

        if (rect1 == null) {
            rect1 = new Rect(0, 0, right, bottom);
        }

        canvas.drawRect(rect1, backgroundPaint);
        canvas.drawRect(rect1, strokePaint);

//        if (textRect == null) {
//            textRect = new Rect(left + horizontalMargin, top, left + gutterWidth, top + buttonHeight);
//        }

//        drawRectText("pH", textRect, nameTextPaint, canvas);

//        Paint.FontMetrics fm = nameTextPaint.getFontMetrics();
//        int titleHeight = (int) (fm.bottom - fm.top + fm.leading);

        nameTextPaint.getTextBounds("pH", 0, "pH".length(), nameBounds);
//        float value = nameTextPaint.measureText("pH");
        int titleHeight = nameBounds.height();
//
        canvas.drawText("pH", left + horizontalMargin, top + titleHeight, nameTextPaint);
        canvas.drawText("Phenol Red", left + horizontalMargin + nameBounds.right + horizontalMargin,
                top + titleHeight, subTitlePaint);

        double value = 8.0;
        for (int i = 0; i < 7; i++) {
            String valueString = String.format(Locale.US, "%.1f", value);
            drawLeftButton(valueString, canvas, gutterWidth + titleHeight + top + verticalMargin + (i * (buttonHeight + verticalMargin)),
                    left + horizontalMargin);
            value -= 0.2;
        }


        left = getMeasuredWidth() - horizontalMargin - buttonWidth - textBoxWidth - gutterWidth;
        canvas.drawText("Cl", left - radius, top + titleHeight, nameTextPaint);
        canvas.drawText("DPD", left + nameBounds.right - radius, top + titleHeight, subTitlePaint);
        canvas.drawText("mg/l", getMeasuredWidth() - horizontalMargin - 80, top + titleHeight, subTitlePaint);

        value = 3;
        for (int i = 0; i < 7; i++) {
            String valueString = String.format(Locale.US, "%.1f", value);
            drawRightButton(valueString, canvas, gutterWidth + titleHeight + top + verticalMargin + (i * (buttonHeight + verticalMargin)),
                    left);
            value -= .5;
        }
    }

    private void drawLeftButton(String text, Canvas canvas, int top, int left) {

        Rect textRect = new Rect(left, top, left + textBoxWidth, top + buttonHeight);
        drawRectText(text, textRect, textPaint, canvas);

        Rect buttonRect = new Rect(left + textBoxWidth + gutterWidth, top,
                left + textBoxWidth + buttonWidth, top + buttonHeight);
        canvas.drawRect(buttonRect, buttonPaint);
        canvas.drawCircle(left + textBoxWidth + buttonWidth, top + radius, radius, buttonPaint);
    }

    private void drawRightButton(String text, Canvas canvas, int top, int left) {

        Rect buttonRect = new Rect(left, top,
                left + buttonWidth, top + buttonHeight);
        canvas.drawRect(buttonRect, buttonPaint);
        canvas.drawCircle(left, top + radius, radius, buttonPaint);

        Rect textRect = new Rect(left + buttonWidth + gutterWidth, top,
                left + buttonWidth + textBoxWidth + gutterWidth, top + buttonHeight);
        drawRectText(text, textRect, textPaint, canvas);

    }

    public String getKey() {
        mKey = (area1 ? "1" : "0")
                + (area2 ? "1" : "0")
                + (area3 ? "1" : "0")
                + (area4 ? "1" : "0")
                + (area5 ? "1" : "0");

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

    private void drawRectText(String text, Rect r, Paint paint, Canvas canvas) {
        canvas.drawRect(r, textBoxPaint);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = r.left + (cWidth / 2f - bounds.width() / 2f - bounds.left);
        float y = r.top + (cHeight / 2f + bounds.height() / 2f - bounds.bottom);
        canvas.drawText(text, x, y, paint);
    }
}