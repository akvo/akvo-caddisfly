package org.akvo.akvoqr.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.akvo.akvoqr.detector.FinderPattern;

import java.util.List;

/**
 * Created by linda on 9/9/15.
 */
public class FinderPatternIndicatorView extends ImageView {

    Paint paint;
    List<FinderPattern> patterns;
    Camera.Size size;

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
        if(patterns!=null) {
            this.patterns = patterns;

            if(size!=null)
                this.size = size;
            invalidate();
        }
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        // Have the view being transparent
        canvas.drawARGB(0, 0, 0, 0);

        float ratioW = 1;
        float ratioH = 1;
        if(patterns!=null) {
            if (size!=null)
            {
//                ratioW = (canvas.getWidth() - size.width)/size.width ;
//                ratioH = (canvas.getHeight() - size.height)/size.height;
            }
            for (int i = 0; i < patterns.size(); i++) {
//                canvas.drawCircle(patterns.get(i).getX() + patterns.get(i).getX() * ratioW, patterns.get(i).getY() + patterns.get(i).getY() * ratioH, 10, paint);
                canvas.drawCircle(patterns.get(i).getX(), patterns.get(i).getY(), 10, paint);
            }
        }
    }
}
