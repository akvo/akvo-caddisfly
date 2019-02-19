package org.akvo.caddisfly.sensor.manual;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.akvo.caddisfly.model.ColorItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.graphics.ColorUtils;

public class SwatchSelectWidget extends View {

    private final Paint buttonPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint textSelectedPaint = new Paint();
    private final Paint backgroundPaint = new Paint();
    private final Paint textBoxLeftPaint = new Paint();
    private final Paint textBoxRightPaint = new Paint();
    private final Paint nameTextPaint = new Paint();
    private final Paint trianglePaint = new Paint();
    private final Paint subTitlePaint = new Paint();
    private final List<ColorItem> clColors = new ArrayList<>();
    private final List<ColorItem> phColors = new ArrayList<>();
    private final List<Rect> phButtons = new ArrayList<>();
    private final List<Rect> clButtons = new ArrayList<>();
    private final Paint buttonSelectPaint = new Paint();
    private final Paint buttonShadowPaint = new Paint();

    Rect nameBounds = new Rect();
    Path lidPath = new Path();
    private Rect rect1;
    private int buttonWidth;
    private int buttonHeight;
    private int textBoxWidth;
    private int gutterWidth = 10;
    private int radius;
    private int activeLeft = -1;
    private int activeRight = -1;

    public SwatchSelectWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.rgb(100, 100, 100));
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(5);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.rgb(130, 130, 130));

        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setColor(Color.rgb(80, 80, 80));
        trianglePaint.setAntiAlias(true);

        textBoxLeftPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textBoxLeftPaint.setColor(Color.rgb(255, 255, 255));

        textBoxRightPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textBoxRightPaint.setColor(Color.rgb(255, 255, 255));

        nameTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        nameTextPaint.setColor(Color.rgb(50, 50, 50));
        nameTextPaint.setStrokeWidth(2);
        nameTextPaint.setAntiAlias(true);

        //        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
//        nameTextPaint.setTextSize(70);

        subTitlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        subTitlePaint.setColor(Color.rgb(50, 50, 50));
        subTitlePaint.setStrokeWidth(1);
        subTitlePaint.setAntiAlias(true);
//        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
        subTitlePaint.setTextSize(40);

        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setAntiAlias(true);

        buttonSelectPaint.setStyle(Paint.Style.STROKE);
        buttonSelectPaint.setAntiAlias(true);
        buttonSelectPaint.setColor(Color.GREEN);
        buttonSelectPaint.setStrokeWidth(5);

        buttonShadowPaint.setStyle(Paint.Style.STROKE);
        buttonShadowPaint.setAntiAlias(true);
        buttonShadowPaint.setColor(Color.rgb(50, 50, 50));
        buttonShadowPaint.setStrokeWidth(4);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.rgb(110, 110, 110));
//        int sizeInPx = context.getResources().getDimensionPixelSize(R.dimen.cbt_shapes_text_size);
//        textPaint.setTextSize(sizeInPx);

        textSelectedPaint.setStyle(Paint.Style.FILL);
        textSelectedPaint.setColor(Color.rgb(0, 0, 0));
        textSelectedPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        phColors.add(new ColorItem(8.2, 159, 45, 68));
        phColors.add(new ColorItem(7.8, 173, 67, 75));
        phColors.add(new ColorItem(7.6, 184, 96, 83));
        phColors.add(new ColorItem(7.4, 197, 120, 98));
        phColors.add(new ColorItem(7.2, 214, 138, 103));
        phColors.add(new ColorItem(7, 198, 150, 102));
        phColors.add(new ColorItem(6.8, 198, 162, 101));

        clColors.add(new ColorItem(3, 169, 59, 92));
        clColors.add(new ColorItem(2, 181, 78, 110));
        clColors.add(new ColorItem(1.5, 194, 100, 124));
        clColors.add(new ColorItem(1.0, 194, 121, 133));
        clColors.add(new ColorItem(0.6, 209, 149, 160));
        clColors.add(new ColorItem(0.3, 224, 186, 189));
        clColors.add(new ColorItem(0.1, 214, 192, 184));
    }

    //stackoverflow.com/questions/12166476/android-canvas-drawtext-set-font-size-from-width

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint        the Paint to set the text size for
     * @param desiredWidth the desired width
     * @param text         the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {

        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    // stackoverflow.com/questions/5896234/how-to-use-android-canvas-to-draw-a-rectangle-with-only-topleft-and-topright-cor
    public static Path RoundedRect(
            float left, float top, float right, float bottom, float rx, float ry,
            boolean tl, boolean tr, boolean br, boolean bl
    ) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        else {
            path.rLineTo(0, -ry);
            path.rLineTo(-rx, 0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        else {
            path.rLineTo(-rx, 0);
            path.rLineTo(0, ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bl)
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        else {
            path.rLineTo(0, ry);
            path.rLineTo(rx, 0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        else {
            path.rLineTo(rx, 0);
            path.rLineTo(0, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < phButtons.size(); i++) {
                if (phButtons.get(i).contains((int) x, (int) y)) {
                    activeLeft = i;
                    break;
                }
            }

            for (int i = 0; i < clButtons.size(); i++) {
                if (clButtons.get(i).contains((int) x, (int) y)) {
                    activeRight = i;
                    break;
                }
            }

            invalidate();

            return true;
        } else if (action == MotionEvent.ACTION_UP) {

            invalidate();

            performClick();

            return true;
        }

//        getKey();

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
        int top = (int) (getMeasuredHeight() * 0.04);
        int verticalMargin;
        int horizontalMargin = 20;
        int right = getWidth();
        int bottom = (int) (getHeight() - borderPaint.getStrokeWidth());

        buttonWidth = getMeasuredWidth() / 5;
        textBoxWidth = getMeasuredWidth() / 6;
        buttonHeight = (getMeasuredHeight() - top) / 9;
        verticalMargin = (int) (buttonHeight * 0.1);
        radius = buttonHeight / 2;

        if (rect1 == null) {
            rect1 = new Rect(4, top, right - 4, bottom);
        }

        Path borderPath = RoundedRect(rect1.left, rect1.top, rect1.right, rect1.bottom, 30, 30,
                false, false, true, true);
        canvas.drawPath(borderPath, borderPaint);
        canvas.drawPath(borderPath, backgroundPaint);

        lidPath.moveTo(-50, 50);
        lidPath.quadTo(getMeasuredWidth() / 4, -50, getMeasuredWidth() / 2, 50);
        lidPath.quadTo((getMeasuredWidth() / 4) * 3, -50, getMeasuredWidth() + 50, 50);
        canvas.drawPath(lidPath, backgroundPaint);

        int triangleSize = (int) (getMeasuredWidth() * 0.03);
        drawTriangle((getMeasuredWidth() / 2) - triangleSize / 2, top + verticalMargin, triangleSize,
                triangleSize, trianglePaint, canvas);

        phButtons.clear();
        clButtons.clear();

        String name1 = "pH";
        setTextSizeForWidth(nameTextPaint, (float) (textBoxWidth * 0.7), name1);
        nameTextPaint.getTextBounds(name1, 0, name1.length(), nameBounds);
        int titleHeight = nameBounds.height();
        canvas.drawText(name1, left + horizontalMargin, top + titleHeight, nameTextPaint);

        titleHeight = nameBounds.height() + verticalMargin;

        String reagent1 = "Phenol Red";
        setTextSizeForWidth(subTitlePaint, (float) (buttonWidth * 1.2), reagent1);
        canvas.drawText(reagent1, left + horizontalMargin + nameBounds.right + horizontalMargin,
                top + titleHeight, subTitlePaint);

        setTextSizeForWidth(textPaint, (float) (textBoxWidth * 0.4), "2.0");
        setTextSizeForWidth(textSelectedPaint, (float) (textBoxWidth * 0.5), "2.0");

        for (int i = 0; i < 7; i++) {
            String valueString = String.format(Locale.US, "%.1f", phColors.get(i).getValue());
            drawLeftButton(valueString, phColors.get(i).getRgb(), canvas,
                    gutterWidth + titleHeight + top + verticalMargin + (i * (buttonHeight + verticalMargin)),
                    left + horizontalMargin, activeLeft == i);
        }

        left = getMeasuredWidth() - horizontalMargin - buttonWidth - textBoxWidth - gutterWidth;
        canvas.drawText("Cl", left - radius, top + titleHeight, nameTextPaint);
        canvas.drawText("DPD", left + nameBounds.right - radius, top + titleHeight, subTitlePaint);

        String unit = "mg/l";
        subTitlePaint.getTextBounds(unit, 0, unit.length(), nameBounds);
        canvas.drawText(unit, getMeasuredWidth() - horizontalMargin - nameBounds.width(), top + titleHeight, subTitlePaint);

        for (int i = 0; i < 7; i++) {
            String valueString = String.format(Locale.US, "%.1f", clColors.get(i).getValue());
            drawRightButton(valueString, clColors.get(i).getRgb(), canvas,
                    gutterWidth + titleHeight + top + verticalMargin + (i * (buttonHeight + verticalMargin)),
                    left, activeRight == i);
        }
    }

    private void drawLeftButton(String text, int color, Canvas canvas, int top, int left, boolean isActive) {

        Rect textRect = new Rect(left, top, left + textBoxWidth, top + buttonHeight);
        Rect buttonRect = new Rect(left + textBoxWidth + gutterWidth, top,
                left + textBoxWidth + buttonWidth, top + buttonHeight);

        Rect leftRect = new Rect(buttonRect.left - gutterWidth - textBoxWidth, buttonRect.top - 2,
                buttonRect.right, buttonRect.bottom + 5);

        if (activeLeft > -1) {
            textBoxLeftPaint.setColor(phColors.get(activeLeft).getRgb());
            textPaint.setColor(getDarkerColor(phColors.get(activeLeft).getRgb()));
        } else {
            textPaint.setColor(Color.rgb(110, 110, 110));
        }

        canvas.drawRect(textRect, textBoxLeftPaint);

        if (isActive) {

            canvas.drawRect(leftRect, buttonShadowPaint);

            canvas.drawCircle(left + textBoxWidth + buttonWidth, top + radius,
                    radius + 3, buttonSelectPaint);

            canvas.drawRect(new Rect(buttonRect.left - 3 - gutterWidth - textBoxWidth, buttonRect.top - 3,
                    buttonRect.right, buttonRect.bottom + 3), buttonSelectPaint);

            drawRectText(text, textRect, textSelectedPaint, canvas);
        } else {
            drawRectText(text, textRect, textPaint, canvas);
        }

        buttonPaint.setColor(color);
        canvas.drawRect(buttonRect, buttonPaint);
        canvas.drawCircle(left + textBoxWidth + buttonWidth, top + radius, radius, buttonPaint);

        phButtons.add(leftRect);
    }

    private int getDarkerColor(int color) {
        return ColorUtils.blendARGB(color, Color.BLACK, 0.2f);
    }

    private void drawRightButton(String text, int color, Canvas canvas, int top, int left, boolean isActive) {

        Rect textRect = new Rect(left + buttonWidth + gutterWidth, top,
                left + buttonWidth + textBoxWidth + gutterWidth, top + buttonHeight);
        Rect buttonRect = new Rect(left, top, left + buttonWidth, top + buttonHeight);

        Rect rightRect = new Rect(buttonRect.left, buttonRect.top - 2,
                buttonRect.right + gutterWidth + textBoxWidth, buttonRect.bottom + 5);

        if (activeRight > -1) {
            textBoxRightPaint.setColor(clColors.get(activeRight).getRgb());
            textPaint.setColor(getDarkerColor(clColors.get(activeRight).getRgb()));
        } else {
            textPaint.setColor(Color.rgb(110, 110, 110));
        }

        canvas.drawRect(textRect, textBoxRightPaint);

        if (isActive) {
            canvas.drawRect(rightRect, buttonShadowPaint);

            canvas.drawCircle(left, top + radius,
                    radius + 3, buttonSelectPaint);

            canvas.drawRect(new Rect(buttonRect.left, buttonRect.top - 3,
                    buttonRect.right + 3 + gutterWidth + textBoxWidth, buttonRect.bottom + 3), buttonSelectPaint);

            drawRectText(text, textRect, textSelectedPaint, canvas);

        } else {
            drawRectText(text, textRect, textPaint, canvas);
        }

        buttonPaint.setColor(color);
        canvas.drawRect(buttonRect, buttonPaint);
        canvas.drawCircle(left, top + radius, radius, buttonPaint);

        clButtons.add(rightRect);
    }

    public float[] getKey() {
        float[] result = new float[2];

        if (activeLeft > -1) {
            result[0] = phColors.get(activeLeft).getValue().floatValue();
        }
        if (activeRight > -1) {
            result[1] = clColors.get(activeRight).getValue().floatValue();
        }
        return result;
    }

    public void setKey(float[] key) {
        if (key != null) {
            for (int i = 0; i < phColors.size(); i++) {
                if (phColors.get(i).getValue().floatValue() == key[0]) {
                    activeLeft = i;
                }
            }

            for (int i = 0; i < clColors.size(); i++) {
                if (clColors.get(i).getValue().floatValue() == key[1]) {
                    activeRight = i;
                }
            }
        }
    }

    private void drawRectText(String text, Rect r, Paint paint, Canvas canvas) {
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = r.left + (cWidth / 2f - bounds.width() / 2f - bounds.left);
        float y = r.top + (cHeight / 2f + bounds.height() / 2f - bounds.bottom);
        canvas.drawText(text, x, y, paint);
    }

    //stackoverflow.com/questions/3501126/how-to-draw-a-filled-triangle-in-android-canvas
    private void drawTriangle(int x, int y, int width, int height,
                              Paint paint, Canvas canvas) {

        Point p1 = new Point(x, y);
        int pointX = x + width / 2;
        int pointY = y + height;

        Point p2 = new Point(pointX, pointY);
        Point p3 = new Point(x + width, y);


        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();

        canvas.drawPath(path, paint);
    }

    public void setRange(int i) {
        if (i == 2){
            clColors.clear();
            clColors.add(new ColorItem(6, 169, 59, 92));
            clColors.add(new ColorItem(5, 181, 78, 110));
            clColors.add(new ColorItem(3, 194, 100, 124));
            clColors.add(new ColorItem(2, 194, 121, 133));
            clColors.add(new ColorItem(1.5, 209, 149, 160));
            clColors.add(new ColorItem(1, 224, 186, 189));
            clColors.add(new ColorItem(0.5, 214, 192, 184));
        }
    }
}