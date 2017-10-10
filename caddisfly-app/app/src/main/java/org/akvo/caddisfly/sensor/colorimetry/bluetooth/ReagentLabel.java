package org.akvo.caddisfly.sensor.colorimetry.bluetooth;

import android.content.Context;
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
    private final Paint subtitleTextPaint = new Paint();
    private final Paint blueTextPaint = new Paint();
    private final Paint redTextPaint = new Paint();
    private final float titleHeight;
    private Rect rect1;

    private float titleWidth;
    private float titleCharWidth;

    private float subtitleWidth;
    private float subtitleCharWidth;


    private float margin;
    private int imageMargin;
    private int imageWidth;
    private int imageHeight;
    private float subtitleTop;
    private float subtitleHeight;
    private float line1Top;
    private float line2Top;

    private String reagentName;
    private String reagentCode;

    public ReagentLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        // stroke
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.rgb(0, 0, 0));
        strokePaint.setStrokeWidth(10);

        titleTextPaint.setStyle(Paint.Style.FILL);
        titleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titleTextPaint.setColor(Color.rgb(30, 30, 30));
        titleTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.md610_label_title));
        titleWidth = titleTextPaint.measureText("Lovibond");
        titleCharWidth = titleTextPaint.measureText("W");
        titleHeight = titleTextPaint.measureText("W");

        superscriptTextPaint.setStyle(Paint.Style.FILL);
        superscriptTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        superscriptTextPaint.setColor(Color.rgb(0, 0, 0));
        superscriptTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.md610_label_superscript));

        subtitleTextPaint.setStyle(Paint.Style.FILL);
        subtitleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subtitleTextPaint.setColor(Color.rgb(0, 0, 0));
        subtitleTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.md610_label_subtitle));
        subtitleWidth = subtitleTextPaint.measureText("Tintometer");
        subtitleCharWidth = titleTextPaint.measureText("W");
        subtitleHeight = titleTextPaint.measureText("W");

        blueTextPaint.setStyle(Paint.Style.FILL);
        blueTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        blueTextPaint.setColor(Color.rgb(12, 68, 150));

        redTextPaint.setStyle(Paint.Style.FILL);
        redTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        redTextPaint.setColor(Color.rgb(240, 0, 9));
        redTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.md610_label_reagent_code));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (rect1 == null) {
            rect1 = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
            margin = (float) (getMeasuredHeight() * 0.1);
            imageMargin = (int) (getMeasuredHeight() * 0.08);

            imageWidth = (int) (getMeasuredWidth() * 0.2);
            imageHeight = (343 * imageWidth) / 440;

            subtitleTop = margin + titleHeight + margin + titleHeight;

            line1Top = margin + subtitleTop + margin + (margin / 2);

            line2Top = margin + line1Top + margin;

            for (int i = 22; i >= 10; i--) {

                blueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        i, getResources().getDisplayMetrics()));
                float width = blueTextPaint.measureText(reagentName);
                if (width < getMeasuredWidth() - margin - margin) {
                    break;
                }
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawRect(rect1, strokePaint);

        canvas.drawText("Lovibond", margin, margin + titleHeight, titleTextPaint);
        canvas.drawText("®", margin + titleWidth, margin + (titleHeight / 2), superscriptTextPaint);
        canvas.drawText("Water Testing", margin + titleWidth + titleCharWidth, margin + titleHeight, titleTextPaint);

        canvas.drawText("Tintometer", margin, subtitleTop, subtitleTextPaint);
        canvas.drawText("®", margin + subtitleWidth, subtitleTop - (subtitleHeight / 3), superscriptTextPaint);
        canvas.drawText("Group", margin + subtitleWidth + subtitleCharWidth, subtitleTop, subtitleTextPaint);

        canvas.drawText(reagentName.toUpperCase(), margin, line1Top, blueTextPaint);

        canvas.drawText(reagentCode, margin, line2Top, redTextPaint);

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