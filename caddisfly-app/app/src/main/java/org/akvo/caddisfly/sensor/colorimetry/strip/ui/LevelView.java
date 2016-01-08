package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

/**
 * Created by linda on 10/27/15.
 */
public class LevelView extends View {

    private float[] angles;
    private final Paint redPaint;
    private final Bitmap arrowBitmap;

    public LevelView(Context context) {
        this(context, null);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(3);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(angles!=null)
        {
            float degrees = getDegrees(angles);
            if(degrees!=0) {
                canvas.save();
                canvas.rotate(degrees, canvas.getWidth() / 2, canvas.getHeight() / 2);
                canvas.drawBitmap(arrowBitmap, 0, 0, redPaint);
                canvas.restore();
            }

        }

        super.onDraw(canvas);
    }

    public void setAngles(float[] angles) {

        this.angles = angles;

        invalidate();
    }

    private float getDegrees(float[] angles)
    {

        float degrees = 0f;


        if(angles[0] < -Constant.MAX_LEVEL_DIFF)
        {
            degrees = -90;
        }
        else if(angles[0] > Constant.MAX_LEVEL_DIFF)
        {
            degrees = 90;
        }

        if(angles[1] < -Constant.MAX_LEVEL_DIFF)
        {
            degrees = 1;
        }
        else if(angles[1] > Constant.MAX_LEVEL_DIFF)
        {
            degrees = 180;
        }

        return degrees;
    }
}
