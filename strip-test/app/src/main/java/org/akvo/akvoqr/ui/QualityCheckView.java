package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.akvoqr.R;

/**
 * Created by linda on 11/5/15.
 */
public class QualityCheckView extends View {

    protected Paint paint;
    protected int green;
    protected int orange;
    protected int red;
    protected final int NUMBER_OF_BARS = 6;
    protected float percentage = Float.NaN;

    public QualityCheckView(Context context) {
        this(context, null);
    }

    public QualityCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QualityCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        green = context.getResources().getColor(R.color.springgreen);
        orange = context.getResources().getColor(R.color.orange);
        red = context.getResources().getColor(R.color.clear_bordeaux);

    }

    @Override
    public void onDraw(Canvas canvas) {

        if (percentage == Float.NaN) {
            return;
        }
        double number = fromPercentageToNumber(percentage);

        if (number == Double.NaN) {
            throw new NullPointerException("Method to get number must be overridden in child class.");
        }

        System.out.println("***perc. = " + percentage  + " number = " + number);


        canvas.save();
        //start at half of the width the view is going to be
        //each 'dot' has a diameter of half the canvas height
        canvas.translate(canvas.getWidth() / 2 - ((canvas.getHeight() * 0.5f + 0.2f) * 2.5f), 0);
        for (double i = 0; i < NUMBER_OF_BARS; i++) {

            //reset color
            paint.setColor(Color.LTGRAY);

            //check if we have valid data
            if(number >= 0) {
                //if percentage exceeds optimum
                if(number > NUMBER_OF_BARS)
                {
                    //option to show all circles with a yellow color
                    //paint.setColor(Color.YELLOW);

                    //trick code into showing the two orange circles
                    number = NUMBER_OF_BARS * 0.5;
                }
                else
                //change color depending on value of percentage
                if (i < 2) { //0 1

                    // paint red if percentage is lower than i+1
                    // so two red dots if perc. < 1
                    // one red dot (right, second) if perc. > 1
                    if(number <= i + 1 )
                        paint.setColor(red);

                } else if (i < 4) { //2 3

                    // paint orange if perc. between 1 and 4
                    // so if perc. == 1.5 we get a red followed by orange dot,
                    // if perc. == 2.5 we get two orange dots
                    // if perc. == 3.5 we get an orange dot on the right, second of the two
                    if(number >= i - 1 && number <= i + 2 )
                        paint.setColor(orange);

                } else { // 4 5

                    //paint green if perc. larger than 4
                    //so if perc. == 4.5 we get two green dots
                    // and if perc. == 6 also two
                    if(number > i )
                        paint.setColor(green);
                }

            }

            //draw a circle with the paint now in the right color
            canvas.drawCircle(
                    0,
                    canvas.getHeight() * 0.5f,
                    canvas.getHeight() * 0.25f,
                    paint);

            //move to the right: distance of circle radius plus little extra before drawing following circle
            canvas.translate(canvas.getHeight() * 0.5f + 0.2f, 0);
        }
        canvas.restore();
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        invalidate();
    }

    protected double fromPercentageToNumber(float percentage)
    {
        //Override in child class

        return Double.NaN;
    }
}
