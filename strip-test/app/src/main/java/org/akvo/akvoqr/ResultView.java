package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by linda on 6/21/15.
 */
public class ResultView extends View {

    public int maxColor;
    public int minColor;
    private float rangeRed;
    private float rangeGreen;
    private float rangeBlue;
    int maxRed = 80;
    int maxGreen = 42;
    int maxBlue = 56;
    int minRed = 236;
    int minGreen = 232;
    int minBlue = 164;


    Paint paint;
    private float[] hsvMax = new float[3];
    private float[] hsvMin = new float[3];

    public ResultView(Context context) {
        this(context, null);
    }

    public ResultView(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
    }

    public ResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);



        maxColor = Color.argb(255, maxRed, maxGreen, maxBlue);
        Color.colorToHSV(maxColor, hsvMax);


        minColor = Color.argb(255, minRed, minGreen, minBlue);
        Color.colorToHSV(minColor, hsvMin);

        rangeRed = Math.abs(minRed - maxRed);
        rangeGreen = minGreen - maxGreen;
        rangeBlue = minBlue - maxBlue;

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
//        float[] newHSV = new float[3];
//        float diffHue = (hsvMax[0] - hsvMin[0]);
//        float diffSat = Math.abs(hsvMax[1] - hsvMin[1]);
//        float diffVal = Math.abs(hsvMax[2] - hsvMin[2]);
//        float dxHue = diffHue/10;
//        float dxSat = diffSat/10;
//        float dxVal = diffVal/10;

//        System.out.println("*** diffHue = " + diffHue);
//
//        newHSV[0] = hsvMax[0];
//        newHSV[1] = 0.4f;
//        newHSV[2] = hsvMax[2];

        int dRed = Math.round (rangeRed/10);
        int dGreen = Math.round(rangeGreen/10);
        int dBlue = Math.round(rangeBlue/10);

        canvas.save();
        for(int i = 0; i < 10; i+=1)
        {
//            newHSV[0] = newHSV[0] - dxHue;
//            //newHSV[1] = newHSV[1] + dxSat;
//            newHSV[2] = newHSV[2] + dxVal;

           int color = Color.argb(255, maxRed+(dRed*i), maxGreen+dGreen*i, maxBlue+dBlue*i);
           // System.out.println("*** newHSV[0] = " + newHSV[0]);
            paint.setColor(color);
            canvas.drawRect(0, 0, canvas.getWidth()/10, canvas.getHeight(), paint);
            canvas.translate(canvas.getWidth()/10, 0);
        }
        canvas.restore();
    }
}
