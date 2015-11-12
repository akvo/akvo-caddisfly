package org.akvo.akvoqr.ui;

import android.content.Context;
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

        yellowPaint = new Paint(paint);
        yellowPaint.setColor(Color.YELLOW);

        //set percentage to a value that results in a negative at the start of the onDraw
        percentage = -1;
    }

    @Override
    protected double fromPercentageToNumber(float percentage)
    {
        // calculate the percentage back to value that fits NUMBER_OF_BARS.
        // MAX_LUM_PERCENTAGE is the optimum value that can be reached,
        // larger percentages mean over-exposure.
        // we want the number to range between 0 (= dark) and 6 (bright).
        return (percentage/Constant.MAX_LUM_PERCENTAGE) *  NUMBER_OF_BARS ;
    }

}
