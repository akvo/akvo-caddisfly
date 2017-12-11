package org.akvo.caddisfly.sensor.striptest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.GroupType;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.utils.ColorUtils;

import java.util.List;

import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueString;

public class SwatchView extends View {

    private final static int COLOR_BAR_HGAP = 10;
    private final static int VAL_BAR_HEIGHT = 20;
    private final static int TEXT_SIZE = 20;

    int blockWidth = 0;
    int lineHeight = 0;
    private TestInfo testInfo;


    public SwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (testInfo != null) {
            for (int resultIndex = 0; resultIndex < testInfo.Results().size(); resultIndex++) {

                List<ColorItem> colors = testInfo.Results().get(resultIndex).getColors();
                if (colors.size() > 0) {
                    int colorCount = colors.size();

                    int[][] rgbCols = new int[colorCount][3];
                    float[] values = new float[colorCount];
                    float[] lab = new float[3];
                    int[] rgb;

                    // get lab colours and turn them to RGB
                    for (int i = 0; i < colorCount; i++) {
                        List<Double> patchColorValues = colors.get(i).getLab();
                        lab[0] = patchColorValues.get(0).floatValue();
                        lab[1] = patchColorValues.get(1).floatValue();
                        lab[2] = patchColorValues.get(2).floatValue();

                        rgb = ColorUtils.XYZtoRGBint(ColorUtils.Lab2XYZ(lab));
                        rgbCols[i] = rgb;
                        values[i] = colors.get(i).getValue().floatValue();
                    }

                    // create paints
                    Paint paintColor = new Paint();
                    paintColor.setStyle(Paint.Style.FILL);
                    Paint blackText = new Paint();
                    blackText.setColor(android.graphics.Color.BLACK);
                    blackText.setTextSize(TEXT_SIZE);
                    blackText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    blackText.setTextAlign(Paint.Align.CENTER);

                    int totWidth = COLOR_BAR_HGAP + blockWidth;
                    for (int i = 0; i < colorCount; i++) {
                        paintColor.setARGB(255, rgbCols[i][0], rgbCols[i][1], rgbCols[i][2]);

                        canvas.drawRect(i * totWidth, resultIndex * lineHeight,
                                i * totWidth + blockWidth, (resultIndex * lineHeight) + blockWidth, paintColor);

                        if (testInfo.getGroupingType() == GroupType.INDIVIDUAL || resultIndex == testInfo.Results().size() - 1) {
                            canvas.drawText(createValueString(values[i]), i * totWidth + blockWidth / 2,
                                    (resultIndex * lineHeight) + blockWidth + VAL_BAR_HEIGHT, blackText);
                        }
                    }
                }
            }
        }
    }

    public void setPatch(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int lineCount = 0;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {

            if (testInfo != null) {
                for (int resultIndex = 0; resultIndex < testInfo.Results().size(); resultIndex++) {
                    List<ColorItem> colors = testInfo.Results().get(resultIndex).getColors();
                    if (colors.size() > 0) {
                        int colorCount = colors.size();

                        if (blockWidth == 0) {
                            blockWidth = Math.round((getMeasuredWidth() - (colorCount - 1) * COLOR_BAR_HGAP) / colorCount);
                            if (testInfo.getGroupingType() == GroupType.GROUP) {
                                lineHeight = blockWidth + (VAL_BAR_HEIGHT / 2);
                            } else {
                                lineHeight = blockWidth + VAL_BAR_HEIGHT + VAL_BAR_HEIGHT;
                            }
                        }
                    }
                }
            }
        }

        if (testInfo != null) {
            lineCount = testInfo.Results().size();
        }
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(( lineCount* lineHeight) +
                        VAL_BAR_HEIGHT, MeasureSpec.EXACTLY));
    }
}