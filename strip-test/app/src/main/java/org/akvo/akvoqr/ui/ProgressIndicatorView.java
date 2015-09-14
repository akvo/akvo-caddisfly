package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 9/4/15.
 */
public class ProgressIndicatorView extends View {

    private Paint strokePaint;
    private Paint neutralPaint;
    private Paint fillPaint;
    private Paint stripPaint;
    private int totalSteps = 1;
    private int stepsTaken = 0;
    private int distance;

    public ProgressIndicatorView(Context context) {
        this(context, null);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.strokePaint = new Paint();
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);
        strokePaint.setAntiAlias(true);

        fillPaint = new Paint(strokePaint);
        fillPaint.setStyle(Paint.Style.FILL);

        neutralPaint = new Paint(fillPaint);
        neutralPaint.setColor(Color.LTGRAY);

        stripPaint = new Paint(fillPaint);
        stripPaint.setColor(Color.WHITE);

    }

    public void  setTotalSteps(int totalSteps)
    {
        this.totalSteps = totalSteps;
    }
    public void setStepsTaken(int steps)
    {
        this.stepsTaken = steps;
        invalidate();
    }

    @Override
    public void onMeasure(int w, int h)
    {
        super.onMeasure(w, h);
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        canvas.drawARGB(255, 0, 0, 0);
        distance = (int) Math.round(canvas.getHeight() * 0.75)/totalSteps;

        canvas.drawRect(canvas.getWidth()/2 - 20, 20, canvas.getWidth()/2 + 20, canvas.getHeight() + 20, stripPaint);

        for(int i=0;i < totalSteps;i++) {
            Rect rect = new Rect(canvas.getWidth()/2 - 18,
                    (distance * i + distance/2),
                    canvas.getWidth()/2 + 18,
                    (distance * i) + distance/2 + 36);

            if(i < stepsTaken)
            {
               // canvas.drawCircle(canvas.getWidth() / 2, distance * i + distance/2 , canvas.getWidth() / 3, fillPaint);

                canvas.drawRect(rect, fillPaint);
            }
            else
            {
                //canvas.drawCircle(canvas.getWidth() / 2, distance*i + distance/2 , canvas.getWidth()/3, strokePaint);

                canvas.drawRect(rect, neutralPaint);
                canvas.drawRect(rect, strokePaint);
            }

        }
    }
}
