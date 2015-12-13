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
            //canvas has a rotation of 90 degrees in respect to the camera preview
            //Camera preview size is in landscape mode, canvas is in portrait mode
            //the width of the canvas corresponds to the height of the size,
            //the height of the canvas corresponds to the width of the size.
            ratioW = (float)canvas.getWidth() / (float)size.height ;
            ratioH = (float)canvas.getHeight() / (float)size.width;
        }

        // Have the view being transparent
        canvas.drawARGB(0, 0, 0, 0);

        if(patterns!=null) {

            for (int i = 0; i < patterns.size(); i++) {
                //The x of the canvas corresponds to the y of the pattern,
                //The y of the canvas corresponds to the x of the pattern.
                float x = canvas.getWidth() - patterns.get(i).getY() * ratioW;
                float y = patterns.get(i).getX() * ratioH;
                canvas.drawCircle(x, y, 10, paint);
            }
        }

    }
}
