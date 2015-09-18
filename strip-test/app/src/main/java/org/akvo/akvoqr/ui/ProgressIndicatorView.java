package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.choose_striptest.StripTest;

import java.util.List;

/**
 * Created by linda on 9/4/15.
 */
public class ProgressIndicatorView extends View {

    private Paint strokePaint;
    private Paint neutralPaint;
    private Paint fillPaint;
    private Paint stripPaint;
    private Paint timePaint;
    private TextPaint textPaint;
    private int totalSteps = 1;
    private int stepsTaken = 0;
    private int distance;
    private int duration = 10;
    private int timeLapsed = 0;
    private List<StripTest.Brand.Patch> patches;

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

        timePaint = new Paint(fillPaint);
        timePaint.setColor(getResources().getColor(R.color.mediumblue));

        textPaint = new TextPaint();
        textPaint.setColor(getResources().getColor(R.color.mediumblue));
        textPaint.setAntiAlias(true);

    }

    public void  setTotalSteps(int totalSteps)
    {
        this.totalSteps = totalSteps;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTimeLapsed(int timeLapsed) {
        this.timeLapsed = timeLapsed;
        invalidate();
    }

    public void setStepsTaken(int steps)
    {
        this.stepsTaken = steps;
        invalidate();
    }

    public void setPatches(List<StripTest.Brand.Patch> patches) {
        this.patches = patches;
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
        //distance = (int) Math.round(canvas.getHeight() * 0.75)/totalSteps;
        distance = canvas.getWidth();

        //background
        canvas.drawRect(canvas.getWidth()/2 - 20, 20, canvas.getWidth()/2 + 20, canvas.getHeight(), stripPaint);

        //patches
        for(int i=0;i < totalSteps;i++) {

            Rect rect = new Rect(canvas.getWidth()/2 - 17,
                    (distance * i + distance/2),
                    canvas.getWidth()/2 + 17,
                    (distance * i) + distance/2 + 34);

            if(i < stepsTaken)
            {
                canvas.drawRect(rect, fillPaint);
            }
            else
            {
                canvas.drawRect(rect, neutralPaint);
                canvas.drawRect(rect, strokePaint);
            }

            if(timeLapsed>0 && i == getTimePosition() && (duration-timeLapsed) >=0)
            {
                String countdown = String.valueOf(patches.get(i).getTimeLapse() - timeLapsed);
                float textWidth = textPaint.measureText(countdown);
                        canvas.drawText( countdown,
                        rect.centerX()-textWidth/2, rect.centerY(), textPaint);
            }
        }

        //time lapsed
        double timeScale = (double)canvas.getHeight()/(double)duration;
        canvas.drawRect(canvas.getWidth() - 10, 2, canvas.getWidth(),
                (float) (timeLapsed * timeScale), timePaint);

    }

    private int getTimePosition()
    {
        int pos = 0;
        if(patches!=null) {
            for (int i = 0; i < patches.size(); i++) {
                //System.out.println("***timeLapsed: " + i + " = " + timeLapsed + ": " + patches.get(i).getTimeLapse());
                if (timeLapsed < patches.get(i).getTimeLapse()) {
                    System.out.println("***timeLapsed < patch timelapse: " + i + " = " + timeLapsed + ": " + patches.get(i).getTimeLapse());
                } else {
                    pos = i + 1;
                    // break;
                }
            }
        }

        return pos;
    }
}
