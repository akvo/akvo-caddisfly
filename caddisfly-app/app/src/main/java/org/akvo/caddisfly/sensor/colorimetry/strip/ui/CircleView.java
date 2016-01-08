package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 9/4/15.
 */
public class CircleView extends View {

    private Paint paint;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
    }

    public void circleView(int color)
    {
        paint.setColor(color);
        invalidate();
    }

    @Override
    public void onMeasure(int w, int h)
    {
        int smallest = w > h? h: w;

        setMeasuredDimension(smallest, smallest);
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, canvas.getWidth()/3, paint);
    }
}
