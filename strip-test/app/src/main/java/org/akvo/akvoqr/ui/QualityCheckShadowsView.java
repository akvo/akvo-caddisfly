package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import org.akvo.akvoqr.util.Constant;

/**
 * Created by linda on 11/5/15.
 */
public class QualityCheckShadowsView extends QualityCheckView {


    public QualityCheckShadowsView(Context context) {
        super(context);
    }

    public QualityCheckShadowsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QualityCheckShadowsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (percentage > 100) {
            //no data
            canvas.drawRect(
                    0, 0,
                    canvas.getWidth() * 0.05f,
                    canvas.getHeight(),
                    redPaint);
        } else  {
            int numberOfBars = (int) Math.ceil(10-percentage * 0.1f);

            canvas.save();
            for (int i = 0; i <= numberOfBars; i++) {
                if (i < Constant.MAX_SHADOW_PERCENTAGE * 0.9) {
                    canvas.drawRect(
                            0, 0,
                            canvas.getWidth() * 0.05f,
                            canvas.getHeight(),
                            redPaint);

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
