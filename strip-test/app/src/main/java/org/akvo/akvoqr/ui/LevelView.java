package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.akvoqr.R;

/**
 * Created by linda on 10/27/15.
 */
public class LevelView extends View {

    float[] angles;
    Paint redPaint;
    Paint greenPaint;
    Bitmap arrowBitmap;
    Bitmap arrowBitmapOK;
    BitmapDrawable arrowDrawable;

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

        greenPaint = new Paint(redPaint);
        greenPaint.setColor(Color.GREEN);

        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.level);
        arrowBitmapOK = BitmapFactory.decodeResource(context.getResources(), R.drawable.level_ok);

        arrowDrawable = new BitmapDrawable(context.getResources(), arrowBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(angles!=null)
        {
            float degrees = getDegrees(angles);
            if(degrees!=0) {
                canvas.save();
                canvas.rotate(degrees, canvas.getWidth() / 2, canvas.getHeight() / 2);
                canvas.drawBitmap(arrowBitmap, 0, 0, greenPaint);
                canvas.restore();
            }
            else
            {
                canvas.drawBitmap(arrowBitmapOK, 0, 0, greenPaint);
            }
        }
        else
        {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getHeight() / 2, redPaint);

        }
    }

    public void setAngles(float[] angles) {

        this.angles = angles;

        invalidate();
    }

    private float getDegrees(float[] angles)
    {

        float degrees = 0f;


        if(angles[0] < -0.9)
        {
            degrees = -90;
        }
        else if(angles[0] > 0.9)
        {
            degrees = 90;
        }

        if(angles[1] < -0.9)
        {
            degrees = 1;
        }
        else if(angles[1] > 0.9)
        {
            degrees = 180;
        }

        return degrees;
    }
}
