package org.akvo.akvoqr.ui;

import android.content.Context;
import android.util.AttributeSet;

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

        //set percentage to a value that results in a negative at the start of the onDraw
        percentage = 101;
    }

    @Override
    protected double fromPercentageToNumber(float percentage)
    {
        // calculate the percentage back to value that fits NUMBER_OF_BARS
        // we want the number to range between 0 (= heavy shadow) and 6 (no shadow)
        return  (100 - percentage) * NUMBER_OF_BARS * 0.01 ;

    }
}
