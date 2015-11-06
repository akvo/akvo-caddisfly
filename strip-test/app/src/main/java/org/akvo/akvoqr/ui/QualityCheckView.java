package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 11/5/15.
 */
public class QualityCheckView extends View {

    protected Paint greenPaint;
    protected Paint redPaint;
    protected float percentage = 101;

    public QualityCheckView(Context context) {
        this(context, null);
    }

    public QualityCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QualityCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.FILL);
        greenPaint.setAntiAlias(true);

        redPaint = new Paint(greenPaint);
        redPaint.setColor(Color.RED);
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        invalidate();
    }
}
