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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 11/16/15.
 */
public class ProgressIndicatorView extends LinearLayout {

    private Bitmap checked;
    private Bitmap unchecked_light;
    private Bitmap background;
    private ImageView img;
    private boolean set;
    private boolean start;
    private List<Step> steps;
    private int stepsTaken = 0;
    private int timeLapsed = 0;
    private String message;
    private Context context;
    private Paint paint;
    private TextPaint textPaint;

    public ProgressIndicatorView(Context context) {
        this(context, null);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false); //needed for invalidate() to work
        checked = BitmapFactory.decodeResource(context.getResources(), R.drawable.checked_box);
        unchecked_light = BitmapFactory.decodeResource(context.getResources(), R.drawable.unchecked_box_light);
        background = BitmapFactory.decodeResource(getResources(), R.drawable.unchecked_box_green);

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


    public void initView()
    {

        if(!set) {
            if(steps==null)
                return;

            //sort on timelapse, shortest first
            Collections.sort(steps, new StepComparator());

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

            set = true;
        }
    }

    public void setStart(boolean start)
    {
        this.start = start;
    }

    public void setStepsTaken(int stepsTaken) {

        this.stepsTaken = stepsTaken;

       //System.out.println("***xxxsteps taken: " + stepsTaken);

        if(steps!=null) {
            for (int i = 0; i < steps.size(); i++) {
                if (i <= stepsTaken) {
                    ((ImageView) getChildAt(i)).setImageBitmap(checked);
                    steps.get(i).pictureTaken = true;
                }
            }
        }
    }

    public void setTimeLapsed(int timeLapsed) {

        this.timeLapsed = timeLapsed;

        invalidate();

        if(!running && start)
            startAnim();
    }

    private boolean running = false;
    public void startAnim()
    {
        if(steps!=null) {
            //sort on time lapse ascending
            Collections.sort(steps, new StepComparator());

            for (int i = 0; i < steps.size(); i++) {

                Animation blink = AnimationUtils.loadAnimation(context, R.anim.blink);
                blink.setDuration(Math.min(5000, steps.get(i).timelapse * 1000));
                blink.setAnimationListener(new BlinkAnimListener(i));

                if (steps.get(i).getTimelapse() - timeLapsed < 5) {
                    if (getChildCount() > 0 && getChildAt(i) != null) {
                        if (i >= stepsTaken) {

                           // System.out.println("***xxxanimation ended: " + i + "  " + steps.get(i).timelapse);

                            if (!steps.get(i).animationEnded) {

                                getChildAt(i).startAnimation(blink);

                            }

                        } else {
                            getChildAt(i).clearAnimation();
                        }
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


            running = true;

            //System.out.println("***animation qualityChecksOK listener: " + i + " " + animation.getDuration());
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            steps.get(i).animationEnded = true;
            //System.out.println("***animation ended listener: " + i + " " + animation.getDuration());

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

        if(!start)
            return;

        canvas.save();
        for (int i = 0; i < steps.size(); i++) {

            canvas.translate(0f, 5f);
            paint.setAlpha(255);


            if(steps.get(i).pictureTaken)
            {
                message = getContext().getString(R.string.picture_taken);
                canvas.drawBitmap(checked, 0, 0, paint);

            }
            else if (steps.get(i).animationEnded) {
                message = getContext().getString(R.string.ready_for_picture) + String.valueOf(i+1);
                canvas.drawBitmap(background, 0, 0, paint);

            }
            else
            {

                canvas.drawBitmap(background, 0, 0, paint);

                //if the previous step is finished, either picture taken or not, we qualityChecksOK the counter
                //otherwise we do not show any message
                if(i>0) {
                    if (steps.get(i - 1).animationEnded) {
                        try {
                            message = getContext().getString(R.string.waiting) + PreviewUtils.fromSecondsToMMSS(Math.max(0,steps.get(i).getTimelapse() - timeLapsed )) + " sec. ";
                        } catch (Exception e) {
                            message = e.getMessage();
                        }
                    }
                    else
                    {
                        message = "";

                    }
                }
                else
                {
                    //first one does have a count
                    try {
                        message = getContext().getString(R.string.waiting) + PreviewUtils.fromSecondsToMMSS(Math.max(0,steps.get(i).getTimelapse() - timeLapsed )) + " sec. ";
                    } catch (Exception e) {
                        message = e.getMessage();
                    }
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

    private class StepComparator implements Comparator<Step>
    {

        @Override
        public int compare(Step lhs, Step rhs) {
            if(lhs.timelapse < rhs.timelapse)
                return -1;
            if (lhs.timelapse == rhs.timelapse)
            return 0;

            return 1;
        }


    }
}
