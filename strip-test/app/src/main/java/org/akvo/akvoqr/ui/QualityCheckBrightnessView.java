package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import org.akvo.akvoqr.util.Constant;

/**
 * Created by linda on 11/5/15.
 */
public class QualityCheckBrightnessView extends QualityCheckView {


    private Paint yellowPaint;

    public QualityCheckBrightnessView(Context context) {
        this(context, null);
    }

    public QualityCheckBrightnessView(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
    }

    public QualityCheckBrightnessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        yellowPaint = new Paint(redPaint);
        yellowPaint.setColor(Color.YELLOW);
    }

    @Override
    public void onDraw(Canvas canvas) {
//        System.out.println("***brightness percentage: " + percentage);

        if (percentage > 100) {
            //no data
            canvas.drawRect(
                    0, 0,
                    canvas.getWidth() * 0.05f,
                    canvas.getHeight(),
                    redPaint);
        } else {
            int numberOfBars = (int) Math.ceil((percentage/Constant.MAX_LUM_PERCENTAGE) * 10f);

            canvas.save();
            for (int i = 0; i < numberOfBars; i++) {

//                System.out.println("***brightness min lum percentage: "+ i + "  = " + Constant.MIN_LUM_PERCENTAGE + "  " + (double)(Constant.MIN_LUM_PERCENTAGE * 0.1d) +
//                "  max lum perc. = " +(double) (Constant.MAX_LUM_PERCENTAGE * 0.1d));

                if (i < Math.floor(Constant.MAX_LUM_PERCENTAGE * 0.1d)) {
                    canvas.drawRect(
                            0, 0,
                            canvas.getWidth() * 0.05f,
                            canvas.getHeight(),
                            redPaint);
                }
                else if(i > 10 )
                {
                    canvas.drawRect(
                            0, 0,
                            canvas.getWidth() * 0.05f,
                            canvas.getHeight(),
                            yellowPaint);
                } else {
                    canvas.drawRect(
                            0, 0,
                            canvas.getWidth() * 0.05f,
                            canvas.getHeight(),
                            greenPaint);
                }
                canvas.translate(canvas.getWidth() * 0.06f, 0);
            }
            canvas.restore();
        }
    }
}
