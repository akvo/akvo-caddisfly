package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.akvo.akvoqr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 11/16/15.
 */
public class ProgressIndicatorViewAnim extends LinearLayout {

    private Bitmap checked;
    private Bitmap unchecked;
    private Bitmap unchecked_light;
    private ImageView img;
    private ImageView overlay;
    private List<Step> steps;
    private int stepsTaken = 0;
    private int timeLapsed = 0;
    private int duration = Integer.MAX_VALUE;
    private LayoutParams params;
    private Context context;
    private float timescale;
    private Paint paint;

    public ProgressIndicatorViewAnim(Context context) {
        this(context, null);
    }

    public ProgressIndicatorViewAnim(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorViewAnim(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        setWillNotDraw(false); //needed for invalidate() to work
        checked = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        unchecked = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box);
        unchecked_light = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box_light);

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 0, 5, 0);

        this.context = context;

        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);
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
            float marginRight = 0;

            for (int i = 0; i < steps.size(); i++) {
                img = new ImageView(context);
                img.setImageBitmap(unchecked_light);

                if(duration>1) {

                    if(i>0) {

                        float timeDiff = steps.get(i).getTimelapse() - steps.get(i - 1).getTimelapse();

                        marginRight = ((float) getMeasuredWidth() / (float) duration) * timeDiff;
                    }
                }

                img.setMinimumWidth(Math.max(img.getWidth(), Math.round(marginRight)));
                img.setScaleType(ImageView.ScaleType.FIT_END);
                addView(img);

                System.out.println("***getMeasuredWidth() " + getMeasuredWidth());
                System.out.println("***duration == " + duration);
                System.out.println("***timelapse == " + steps.get(i).getTimelapse());
                System.out.println("***margin");
                System.out.println("***margin right 1: " + marginRight);
                System.out.println("***margin");


            }

            setDuration(duration);
            set = true;
        }
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;

        for(int i=0;i<steps.size();i++) {
            if (i < stepsTaken) {
                ((ImageView) getChildAt(i)).setImageBitmap(checked);
            }
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTimeLapsed(int timeLapsed) {

        this.timeLapsed = timeLapsed;

        invalidate();

        System.out.println("***anim running: " + running);

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
                    if (i >= stepsTaken) {

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
            System.out.println("***animation start listener: " + i + " " + animation);

            running = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            steps.get(i).animationEnded = true;
            System.out.println("***animation ended listener: " + i + " " + animation);

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
        //background line
        paint.setAlpha(150);
        canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);

        //draw line to indicate time left
        paint.setAlpha(255);
        timescale = (float)canvas.getWidth()/(float)duration;
        canvas.drawLine(0, 0, Math.min(canvas.getWidth(), timescale * timeLapsed), 0, paint);

        if(steps==null)
            return;

        int duration = steps.get(steps.size() - 1).getTimelapse();
        float marginRight = 0;

        for (int i = 0; i < steps.size(); i++) {

            if (duration > 1) {

                if (i > 0) {

                    marginRight = ((float) canvas.getWidth() / (float) duration) * steps.get(i).getTimelapse() ;
                }
            }


            paint.setAlpha(255);
            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.unchecked_box);
            canvas.drawBitmap(background,Math.min(canvas.getWidth() - background.getWidth(), Math.max(0, marginRight )), 0, paint);
        }
    }

    private class Step
    {
        int order;
        int timelapse;
        boolean animationEnded = false;

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
