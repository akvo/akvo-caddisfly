package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.akvoqr.R;

/**
 * Created by linda on 9/4/15.
 */
public class ProgressIndicatorView extends View {

    private Paint lightbluePaint;
    private Paint mediumbluePaint;
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

        this.lightbluePaint = new Paint();
        lightbluePaint.setColor(context.getResources().getColor(R.color.lightblue));
        lightbluePaint.setAntiAlias(true);

        mediumbluePaint = new Paint(lightbluePaint);
        mediumbluePaint.setColor(context.getResources().getColor(R.color.mediumblue));

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
        distance = canvas.getHeight()/totalSteps;

        for(int i=0;i < totalSteps;i++) {
            if(i < stepsTaken)
            {
                //canvas.drawRect(distance*(i-1) + (canvas.getWidth() / 3) * 2, canvas.getHeight()/2 - 5, distance * i, canvas.getHeight()/2 + 5, mediumbluePaint);
                canvas.drawCircle(canvas.getWidth() / 2, distance * i + distance/2 , canvas.getWidth() / 3, mediumbluePaint);

            }
            else
            {
                //canvas.drawRect(distance*(i-1) + (canvas.getHeight() / 3) * 2, canvas.getHeight()/2 - 5, distance * i, canvas.getHeight()/2 + 5, lightbluePaint);
                canvas.drawCircle(canvas.getWidth() / 2, distance*i + distance/2 , canvas.getWidth()/3, lightbluePaint);
            }

        }
    }
}
