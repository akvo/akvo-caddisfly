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
    private final int totalSteps = 4;
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
        distance = canvas.getWidth()/totalSteps;
        for(int i=0;i < totalSteps;i++) {
            if(i < stepsTaken)
            {
                canvas.drawRect(distance*(i-1) + (canvas.getHeight() / 3) * 2, canvas.getHeight()/2 - 5, distance * i, canvas.getHeight()/2 + 5, mediumbluePaint);
                canvas.drawCircle(distance * i + canvas.getHeight() / 3, canvas.getHeight() / 2, canvas.getHeight() / 3, mediumbluePaint);

            }
            else
            {
                canvas.drawRect(distance*(i-1) + (canvas.getHeight() / 3) * 2, canvas.getHeight()/2 - 5, distance * i, canvas.getHeight()/2 + 5, lightbluePaint);
                canvas.drawCircle(distance*i + canvas.getHeight()/3, canvas.getHeight() / 2, canvas.getHeight()/3, lightbluePaint);
            }

        }
    }
}
