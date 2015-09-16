package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceView;

import org.akvo.akvoqr.detector.FinderPattern;

import java.util.List;

/**
 * Created by linda on 9/9/15.
 */
public class FinderPatternIndicatorView extends SurfaceView {

    Paint paint;
    List<FinderPattern> patterns;
    Camera.Size size;
    float ratioW = 1;
    float ratioH = 1;

    public FinderPatternIndicatorView(Context context) {
        this(context, null);
    }

    public FinderPatternIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FinderPatternIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();
        paint.setColor(Color.RED);
    }

    public void showPatterns(List<FinderPattern> patterns, Camera.Size size)
    {

            this.patterns = patterns;

            if(size!=null) {
                this.size = size;
            }

            invalidate();

    }

    public void setColor(int color)
    {
        paint.setColor(color);
    }

    @Override
    public void onDraw(Canvas canvas)
    {

        if (size!=null)
        {
            ratioW = (float)canvas.getWidth() / (float)size.width ;
            ratioH = (float)canvas.getHeight() / (float)size.height;
        }

        // Have the view being transparent
        canvas.drawARGB(0, 0, 0, 0);

        if(patterns!=null) {

            for (int i = 0; i < patterns.size(); i++) {
                canvas.drawCircle(patterns.get(i).getX()*ratioW, patterns.get(i).getY()*ratioH, 10, paint);
            }
        }
    }
}
