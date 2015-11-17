package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
   // private int distance;
    private int duration = 10;
    private int timeLapsed = 0;
    private Bitmap checkedBox;
    private final Bitmap uncheckedBox;
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
        strokePaint.setColor(Color.GRAY);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(13);
        strokePaint.setAntiAlias(true);

        fillPaint = new Paint(strokePaint);
        fillPaint.setStyle(Paint.Style.FILL);

        neutralPaint = new Paint(fillPaint);
        neutralPaint.setColor(Color.LTGRAY);

        stripPaint = new Paint(fillPaint);
        stripPaint.setColor(Color.WHITE);

        timePaint = new Paint(fillPaint);
        timePaint.setColor(getResources().getColor(R.color.springgreen));

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12);
        textPaint.setAntiAlias(true);

        checkedBox = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        uncheckedBox = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box);

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
        canvas.drawARGB(0, 0, 0, 0);

        //take smallest value from canvas width or height
        //distance = canvas.getWidth()<canvas.getHeight()? canvas.getWidth(): canvas.getHeight();

        //background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), stripPaint);

        canvas.save();
        //distance = (int)Math.round(0.5 * distance);
        //canvas.translate(distance/2, 0f);

        canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, strokePaint);

        //time lapsed
        double timeScale = (double)canvas.getWidth()/(double)duration;
//        canvas.drawRect(canvas.getWidth() - 10, 2, canvas.getWidth(),
//                (float) (timeLapsed * timeScale), timePaint);
        canvas.drawLine(0, canvas.getHeight()/2, (float) (timeLapsed * timeScale), canvas.getHeight()/2, timePaint);

        //patches
        for(int i=0;i < totalSteps;i++) {

            canvas.translate(uncheckedBox.getWidth() + 10f, 0);

            if(i < stepsTaken)
            {
                canvas.drawBitmap(checkedBox, 0, canvas.getHeight()/2 - checkedBox.getHeight()/2, fillPaint);
            }
            else
            {
                canvas.drawBitmap(uncheckedBox, 0, canvas.getHeight()/2 - uncheckedBox.getHeight()/2, fillPaint);

            }

            if(timeLapsed>0  && (duration-timeLapsed) >=0)
            {
                String countdown = String.valueOf(patches.get(i).getTimeLapse() - timeLapsed);
                float textWidth = textPaint.measureText(countdown);
                float textHeight = (textPaint.descent() + textPaint.ascent());
                canvas.drawText( countdown,
                        -textWidth/2, - textHeight, textPaint);
            }

        }

        canvas.restore();



    }

    private int getTimePosition()
    {
        int pos = 0;
        if(patches!=null) {
            for (int i = 0; i < patches.size(); i++) {
                //System.out.println("***timeLapsed: " + i + " = " + timeLapsed + ": " + patches.get(i).getTimeLapse());
                if (timeLapsed < patches.get(i).getTimeLapse()) {
                    //  System.out.println("***timeLapsed < patch timelapse: " + i + " = " + timeLapsed + ": " + patches.get(i).getTimeLapse());
                } else {
                    pos = i + 1;
                    // break;
                }
            }
        }

        return pos;
    }
}
