package org.akvo.akvoqr.instructions_strip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 9/21/15.
 */
public class InstructionFooterView extends View {

    private int numSteps = 3;
    private int activeStep = 0;
    private int radius = 6;
    private float distance = 24;
    private Paint fillPaint;
    private Paint strokePaint;

    public InstructionFooterView(Context context) {
        this(context, null);
    }

    public InstructionFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstructionFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.DKGRAY);
        fillPaint.setAntiAlias(true);

        strokePaint = new Paint(fillPaint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
    }

    public void setNumSteps(int numSteps) {
        this.numSteps = numSteps;
        invalidate();
    }

    public void setActive(int active)
    {
        this.activeStep = active;
        invalidate();
    }

    public void onMeasure(int w, int h)
    {
        w = (int)Math.ceil(distance * numSteps);
        setMeasuredDimension(w, h);
    }
    @Override
    public void onDraw(Canvas canvas)
    {

        for(int i=0;i<numSteps;i++)
        {
            if(activeStep == i)
                canvas.drawCircle(distance*i + radius , canvas.getHeight()/2, radius, fillPaint);
            else
                canvas.drawCircle(distance*i + radius, canvas.getHeight()/2, radius-strokePaint.getStrokeWidth(), strokePaint);
        }
    }
}
