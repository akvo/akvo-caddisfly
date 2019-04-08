package org.akvo.caddisfly.sensor.bluetooth;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import org.akvo.caddisfly.R;

public class ReagentLabel extends View {

    private final Paint strokePaint = new Paint();
    private final Paint titleTextPaint = new Paint();
    private final Paint superscriptTextPaint = new Paint();
    private final Paint superscript2TextPaint = new Paint();
    private final Paint subtitleTextPaint = new Paint();
    private final Paint blueTextPaint = new Paint();
    private final Paint redTextPaint = new Paint();
    private float titleHeight;
    private Rect rect1;

    private float titleWidth;
    private float titleCharWidth;

    private float subtitleWidth;
    private float subtitleCharWidth;

    private float verticalMargin;
    private int imageMargin;
    private int imageWidth;
    private int imageHeight;
    private float subtitleY;
    private float subtitleHeight;
    private float line1Top;
    private float line2Top;
    private int leftMargin = 50;

    private String reagentName;
    private String reagentCode;
    private float paddingLeft;

    public ReagentLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        int height = Resources.getSystem().getDisplayMetrics().heightPixels;

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.rgb(100, 100, 100));
        strokePaint.setStrokeWidth((float) Math.min(5, height * 0.005));

        titleTextPaint.setStyle(Paint.Style.FILL);
        titleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titleTextPaint.setColor(Color.rgb(80, 80, 80));

        superscriptTextPaint.setStyle(Paint.Style.FILL);
        superscriptTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        superscriptTextPaint.setColor(Color.rgb(0, 0, 0));

        subtitleTextPaint.setStyle(Paint.Style.FILL);
        subtitleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subtitleTextPaint.setColor(Color.rgb(0, 0, 0));

        blueTextPaint.setStyle(Paint.Style.FILL);
        blueTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        blueTextPaint.setColor(Color.rgb(12, 68, 150));

        redTextPaint.setStyle(Paint.Style.FILL);
        redTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        redTextPaint.setColor(Color.rgb(240, 0, 9));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (rect1 == null) {
            rect1 = new Rect(leftMargin, 0, getMeasuredWidth() - 12, getMeasuredHeight());
            paddingLeft = (float) (getMeasuredWidth() * 0.02);
            verticalMargin = (float) (getMeasuredHeight() * 0.06);
            imageMargin = (int) (getMeasuredHeight() * 0.08);

            imageWidth = (int) (getMeasuredWidth() * 0.18);
            imageHeight = (343 * imageWidth) / 440;

            int baseHeight = 22;
            for (int i = baseHeight; i >= 10; i--) {
                baseHeight = i;
                titleTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        i, getResources().getDisplayMetrics()));

                float width = titleTextPaint.measureText("Lovibond® Water Testing");
                if (width < getMeasuredWidth() - (leftMargin * 2) - imageWidth - paddingLeft) {
                    break;
                }
            }

            superscriptTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    baseHeight - 4, getResources().getDisplayMetrics()));

            subtitleTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    baseHeight - 3, getResources().getDisplayMetrics()));

            superscript2TextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    baseHeight - 5, getResources().getDisplayMetrics()));

            for (int i = baseHeight - 1; i >= 10; i--) {

                baseHeight = i;
                blueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        i, getResources().getDisplayMetrics()));
                float width = blueTextPaint.measureText(reagentName);
                if (width < getMeasuredWidth() - leftMargin - (paddingLeft * 3)) {
                    break;
                }
            }

            redTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    baseHeight - 1, getResources().getDisplayMetrics()));

            titleWidth = titleTextPaint.measureText("Lovibond");
            titleCharWidth = titleTextPaint.measureText("W");
            titleHeight = titleTextPaint.measureText("W");

            subtitleWidth = subtitleTextPaint.measureText("Tintometer");
            subtitleCharWidth = titleTextPaint.measureText("W");
            subtitleHeight = titleTextPaint.measureText("W");

            subtitleY = imageHeight;

            line1Top = (verticalMargin * 3) + titleHeight + subtitleHeight + titleHeight;
            line2Top = (verticalMargin * 4) + titleHeight + subtitleHeight + (titleHeight * 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawRect(rect1, strokePaint);

        canvas.drawText("Lovibond", leftMargin + paddingLeft, verticalMargin + titleHeight, titleTextPaint);
        canvas.drawText("®", leftMargin + paddingLeft + titleWidth, verticalMargin + (titleHeight / 2), superscriptTextPaint);
        canvas.drawText("Water Testing", leftMargin + paddingLeft + titleWidth + titleCharWidth, verticalMargin + titleHeight, titleTextPaint);

        canvas.drawText("Tintometer", leftMargin + paddingLeft, subtitleY, subtitleTextPaint);
        canvas.drawText("®", leftMargin + paddingLeft + subtitleWidth, subtitleY - (subtitleHeight / 3), superscript2TextPaint);
        canvas.drawText("Group", leftMargin + paddingLeft + subtitleWidth + subtitleCharWidth, subtitleY, subtitleTextPaint);

        canvas.drawText(reagentName.toUpperCase(), leftMargin + paddingLeft, line1Top, blueTextPaint);

        canvas.drawText(reagentCode, leftMargin + paddingLeft, line2Top, redTextPaint);

        Drawable d = getResources().getDrawable(R.drawable.lovibond_logo);
        d.setBounds(getMeasuredWidth() - imageMargin - imageWidth, imageMargin,
                getMeasuredWidth() - imageMargin, imageHeight + imageMargin);
        d.draw(canvas);

    }

    public void setReagentName(String reagentName) {
        this.reagentName = reagentName;
    }

    public void setReagentCode(String reagentCode) {
        this.reagentCode = reagentCode;
    }

}