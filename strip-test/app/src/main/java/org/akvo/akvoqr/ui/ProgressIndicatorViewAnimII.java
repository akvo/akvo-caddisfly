package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.PreviewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 11/16/15.
 */
public class ProgressIndicatorViewAnimII extends LinearLayout {

    private Bitmap checked;
    private Bitmap unchecked_light;
    private ImageView img;
    private List<Step> steps;
    private int stepsTaken = 0;
    private int timeLapsed = 0;
    private Context context;
    private Paint paint;
    private TextPaint textPaint;

    public ProgressIndicatorViewAnimII(Context context) {
        this(context, null);
    }

    public ProgressIndicatorViewAnimII(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorViewAnimII(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false); //needed for invalidate() to work
        checked = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        unchecked_light = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box_light);

        this.context = context;

        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);

        textPaint = new TextPaint();
        textPaint.setColor(Color.DKGRAY);
    }



    public void addStep(int order, int timelapse) {
        if(steps==null)
            steps = new ArrayList<>();

        steps.add(new Step(order, timelapse));
    }

    private boolean set;
    public void initView()
    {

        if(!set) {
            if(steps==null)
                return;

            int duration = steps.get(steps.size() - 1).getTimelapse();

            int layoutH = 0;
            for (int i = 0; i < steps.size(); i++) {
                img = new ImageView(context);
                img.setImageBitmap(unchecked_light);

                img.setMinimumHeight(unchecked_light.getHeight() + 5);
                img.setScaleType(ImageView.ScaleType.FIT_START);
                img.setPadding(0, 5, 0, 0);

                addView(img);

                layoutH += unchecked_light.getHeight() + 5;

            }

            LayoutParams params = (LayoutParams) getLayoutParams();
            params.height = layoutH;
            setLayoutParams(params);

            requestLayout();
            invalidate();
            setDuration(duration);
            set = true;
        }
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;

        System.out.println("***steps taken: " + stepsTaken);

        for(int i=0;i<steps.size();i++) {
            if (i < stepsTaken) {
                ((ImageView) getChildAt(i)).setImageBitmap(checked);
                steps.get(i).pictureTaken = true;
            }
        }
        //invalidate();
    }

    public void setDuration(int duration) {
        int duration1 = duration;
    }

    public void setTimeLapsed(int timeLapsed) {

        this.timeLapsed = timeLapsed;

        invalidate();

        if(!running)
            startAnim();
    }

    private boolean running = false;
    public void startAnim()
    {
        for(int i=0;i<steps.size();i++) {

            Animation blink = AnimationUtils.loadAnimation(context, R.anim.blink1);
            blink.setAnimationListener(new BlinkAnimListener(i));

            if (steps.get(i).getTimelapse() - timeLapsed < 3) {
                if (getChildCount() > 0 && getChildAt(i) != null) {
                    if ( i >= stepsTaken) {

                        System.out.println("***animation ended: "+ i + "  " + steps.get(i).animationEnded);

                        if(!steps.get(i).animationEnded) {

                            getChildAt(i).startAnimation(blink);

                        }

                    } else {
                        getChildAt(i).clearAnimation();
                    }
                }
            }
        }
    }
    private class BlinkAnimListener implements Animation.AnimationListener
    {

        private int i;

        public BlinkAnimListener(int i)
        {
            this.i = i;
        }
        @Override
        public void onAnimationStart(Animation animation) {
            //System.out.println("***animation start listener: " + i + " " + animation);

            running = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            steps.get(i).animationEnded = true;
            //System.out.println("***animation ended listener: " + i + " " + animation);

            running = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get width and height size and mode
        int wSpec = MeasureSpec.getSize(widthMeasureSpec);

        int hSpec = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(wSpec, hSpec);

        initView();
    }
    @Override
    public void onDraw(Canvas canvas)
    {

        if(steps==null)
            return;

        //int duration = steps.get(steps.size() - 1).getTimelapse();
        int animTime = 5; //duration of the animation
        String message = "";
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.unchecked_box_green);

        canvas.save();
        for (int i = 0; i < steps.size(); i++) {

            canvas.translate(0f, 5f);
            paint.setAlpha(255);

            canvas.drawBitmap(background, 0, 0, paint);

            if(steps.get(i).pictureTaken)
            {
                message = "OK";
            }
            else if (steps.get(i).animationEnded) {
                message = "Ready for picture " + String.valueOf(i+1);

            }
            else
            {
                try {
                    message = "Waiting " + PreviewUtils.fromSecondsToMMSS(steps.get(i).getTimelapse() - timeLapsed + animTime) + " sec. ";
                } catch (Exception e) {
                    e.printStackTrace();
                    message = e.getMessage();
                }
            }

            canvas.drawText(message, background.getWidth() + 5f, background.getHeight() / 2, textPaint);
            canvas.translate(0f, background.getHeight());
        }

        canvas.restore();
    }

    private class Step
    {
        int order;
        int timelapse;
        boolean animationEnded = false;
        boolean pictureTaken = false;

        public Step(int order, int timelapse)
        {
            this.order = order;
            this.timelapse = timelapse;
        }

        public int getTimelapse() {
            return timelapse;
        }
    }

}
