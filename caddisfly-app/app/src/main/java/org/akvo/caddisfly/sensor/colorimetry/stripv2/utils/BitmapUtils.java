package org.akvo.caddisfly.sensor.colorimetry.stripv2.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.PatchResult;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils.createValueString;
import static org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.ResultUtils.createValueUnitString;

/**
 * Created by markwestra on 06/10/2017
 */
@SuppressWarnings("WeakerAccess")
public class BitmapUtils {
    private final static int IMG_WIDTH = 500;
    private final static int VALUE_HEIGHT = 40;
    private final static int TRIANGLE_WIDTH = 30;
    private final static int TRIANGLE_HEIGHT = 30;
    private final static int COLOR_DROP_CIRCLE_RADIUS = 20;
    private final static int COLOR_BAR_HEIGHT = 50;
    private final static int COLOR_BAR_VGAP = 10;
    private final static int COLOR_BAR_HGAP = 10;
    private final static int VAL_BAR_HEIGHT = 25;
    private final static int TEXT_SIZE = 20;
    //    final static int TEXT_SIZE_LARGE = 35;
    private final static int SPACING = 10;

    // creates image for a strip consisting of one or more individual patches
    // creates individual parts of the result image, and concatenates them
    public static Bitmap createResultImageSingle(PatchResult patchResult, StripTest.Brand brand) {
        Bitmap triangle = createTriangleBitmap(patchResult, brand);
        Bitmap strip = createStripBitmap(patchResult);
        Bitmap colourDrop = createColourDropBitmapSingle(patchResult);
        Bitmap colourBars = createColourBarsBitmapSingle(patchResult);
        return concatAllBitmaps(triangle, strip, colourDrop, colourBars);
    }

    public static Bitmap createErrorImage() {
        Bitmap resultImage = Bitmap.createBitmap(500, 40, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultImage);

        Paint blackPaint = new Paint();
        blackPaint.setColor(android.graphics.Color.BLACK);

        // create paint
        Paint redText = new Paint();
        redText.setColor(android.graphics.Color.RED);
        redText.setTextSize(30);
        redText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        redText.setTextAlign(Paint.Align.CENTER);
        canvas.drawRect(0, 0, 500, 40, blackPaint);
        canvas.drawText("No strip found", 250, 35, redText);
        return resultImage;
    }

    // creates image for a grouped-style strip
    // creates individual parts of the result image, and concatenates them
    public static Bitmap createResultImageGroup(List<PatchResult> patchResultList) {
        Bitmap strip = createStripBitmap(patchResultList.get(0));
        Bitmap colourDrop = createColourDropBitmapGroup(patchResultList);
        Bitmap colourBars = createColourBarsBitmapGroup(patchResultList);
        return concatAllBitmaps(null, strip, colourDrop, colourBars);
    }

    // Concatenate two bitmaps
    public static Bitmap concatTwoBitmaps(Bitmap bmp1, Bitmap bmp2) {
        int height = bmp1.getHeight() + bmp2.getHeight() + SPACING;
        Bitmap resultImage = Bitmap.createBitmap(IMG_WIDTH, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultImage);
        canvas.drawBitmap(bmp1, 0f, 0f, null);
        canvas.drawBitmap(bmp2, 0f, bmp1.getHeight() + SPACING, null);
        return resultImage;
    }

    // concatenate all the individual bitmaps
    // result is a single bitmap.
    public static Bitmap concatAllBitmaps(Bitmap triangle, Bitmap strip, Bitmap colourDrop, Bitmap colourBars) {
        int height = strip.getHeight() + colourDrop.getHeight() + colourBars.getHeight() + SPACING;
        if (triangle != null) {
            height += triangle.getHeight();
        }

        Bitmap resultImage = Bitmap.createBitmap(IMG_WIDTH, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultImage);
        int totalHeight = 0;

        // paint on all the bitmaps
        if (triangle != null) {
            canvas.drawBitmap(triangle, 0f, 0f, null);
            totalHeight = triangle.getHeight();
        }

        canvas.drawBitmap(strip, 0f, totalHeight, null);
        totalHeight += strip.getHeight() + SPACING;

        canvas.drawBitmap(colourDrop, 0f, totalHeight, null);
        totalHeight += colourDrop.getHeight();

        canvas.drawBitmap(colourBars, 0f, totalHeight, null);

        return resultImage;
    }

    // creates bitmap with description, value and unit.
    // it is only included when we send the image to the server.
    public static Bitmap createValueBitmap(PatchResult patchResult) {
        Bitmap resultImage = Bitmap.createBitmap(IMG_WIDTH, VALUE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultImage);
        String unit = patchResult.getPatch().getUnit();
        String valueString = createValueUnitString(patchResult.getValue(), unit);
        valueString = patchResult.getPatch().getDesc() + ": " + valueString + "  " + patchResult.getBracket();

        // create paint
        Paint blackText = new Paint();
        blackText.setColor(android.graphics.Color.BLACK);
        blackText.setTextSize(TEXT_SIZE);
        blackText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        blackText.setTextAlign(Paint.Align.LEFT);

        canvas.drawText(valueString, 10, 35, blackText);
        return resultImage;
    }

    // create bitmap from the calibrated strip image
    public static Bitmap createStripBitmap(PatchResult patchResult) {
        float[][][] xyzImg = patchResult.getImage();

        // get dimensions
        int rows = xyzImg.length;
        int cols = xyzImg[0].length;

        // convert to RGB
        int[] imgRGB = new int[rows * cols];
        int[] RGB;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                RGB = ColorUtils.XYZtoRGBint(xyzImg[i][j]);
                imgRGB[i * cols + j] = Color.rgb(RGB[0], RGB[1], RGB[2]);
            }
        }

        int height = Math.round(rows * IMG_WIDTH / cols);
        Bitmap bmpRGB = Bitmap.createBitmap(imgRGB, 0, cols, cols, rows, Bitmap.Config.ARGB_8888);

        // resize
        return Bitmap.createScaledBitmap(bmpRGB, IMG_WIDTH, height, false);
    }

    // ----------------------------------------- individual ----------------------------------------

    // create bitmap with a black triangle that indicates the position of the patch for this measurement.
    public static Bitmap createTriangleBitmap(PatchResult patchResult, StripTest.Brand brand) {
        float patchPos = (float) patchResult.getPatch().getPosition();
        float stripWidth = (float) brand.getStripLength();

        float xPos = (patchPos / stripWidth) * IMG_WIDTH;
        Bitmap result = Bitmap.createBitmap(IMG_WIDTH, TRIANGLE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        Paint black = new Paint();

        black.setColor(android.graphics.Color.BLACK);
        black.setStyle(Paint.Style.FILL);

        // compute points of triangle:
        int halfWidth = TRIANGLE_WIDTH / 2;

        Path path = new Path();
        path.moveTo(xPos - halfWidth, 0); // top left
        path.lineTo(xPos + halfWidth, 0); // top right
        path.lineTo(xPos, TRIANGLE_HEIGHT); // bottom
        path.lineTo(xPos - halfWidth, 0); // back to top left
        path.close();

        canvas.drawPath(path, black);

        return result;
    }

    // Create bitmap for coloured 'drop', which indicates the position of the
    // measured colour, and has as its own colour the measured colour.
    public static Bitmap createColourDropBitmapSingle(PatchResult patchResult) {
        Bitmap result = Bitmap.createBitmap(IMG_WIDTH, 3 * COLOR_DROP_CIRCLE_RADIUS, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        float[] xyz = patchResult.getXyz();
        int[] rgb = ColorUtils.XYZtoRGBint(xyz);

        // compute central location of marker, using the index of the matched colour
        JSONArray colors = patchResult.getPatch().getColors();
        int numColors = colors.length();
        int blockWidth = Math.round((IMG_WIDTH - (numColors - 1) * COLOR_BAR_HGAP) / numColors);
        int xrange = IMG_WIDTH - blockWidth;
        int totIndex = ResultUtils.INTERPOLATION_NUMBER * (numColors - 1) + 1;
        int xpos = (blockWidth / 2 + xrange * patchResult.getIndex() / totIndex);

        Paint paintColor = new Paint();
        paintColor.setStyle(Paint.Style.FILL);
        paintColor.setARGB(255, rgb[0], rgb[1], rgb[2]);

        // compute points of triangle:
        Path path = new Path();
        path.moveTo(xpos - COLOR_DROP_CIRCLE_RADIUS, COLOR_DROP_CIRCLE_RADIUS); // top left
        path.lineTo(xpos + COLOR_DROP_CIRCLE_RADIUS, COLOR_DROP_CIRCLE_RADIUS); // top right
        path.lineTo(xpos, 3 * COLOR_DROP_CIRCLE_RADIUS); // bottom
        path.lineTo(xpos - COLOR_DROP_CIRCLE_RADIUS, COLOR_DROP_CIRCLE_RADIUS); // back to top left
        path.close();

        canvas.drawCircle(xpos, COLOR_DROP_CIRCLE_RADIUS, COLOR_DROP_CIRCLE_RADIUS, paintColor);
        canvas.drawPath(path, paintColor);

        return result;
    }

    // Creates bitmap with colour blocks and values.
    public static Bitmap createColourBarsBitmapSingle(PatchResult patchResult) {
        Bitmap result = Bitmap.createBitmap(IMG_WIDTH, COLOR_BAR_HEIGHT + VAL_BAR_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        JSONArray colors = patchResult.getPatch().getColors();
        int numColors = colors.length();

        int[][] rgbCols = new int[numColors][3];
        float[] values = new float[numColors];
        float[] lab = new float[3];
        int[] rgb;

        try {
            // get lab colours and turn them to RGB
            for (int i = 0; i < numColors; i++) {
                JSONArray patchColorValues = colors.getJSONObject(i).getJSONArray(SensorConstants.LAB);
                lab[0] = (float) patchColorValues.getDouble(0);
                lab[1] = (float) patchColorValues.getDouble(1);
                lab[2] = (float) patchColorValues.getDouble(2);

                rgb = ColorUtils.XYZtoRGBint(ColorUtils.Lab2XYZ(lab));
                rgbCols[i] = rgb;
                values[i] = (float) colors.getJSONObject(i).getDouble(SensorConstants.VALUE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create paints
        Paint paintColor = new Paint();
        paintColor.setStyle(Paint.Style.FILL);
        Paint blackText = new Paint();
        blackText.setColor(android.graphics.Color.BLACK);
        blackText.setTextSize(TEXT_SIZE);
        blackText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        blackText.setTextAlign(Paint.Align.CENTER);

        // cycle over colours and create blocks and value
        int blockWidth = Math.round((IMG_WIDTH - (numColors - 1) * COLOR_BAR_HGAP) / numColors);
        int totWidth = COLOR_BAR_HGAP + blockWidth;
        for (int i = 0; i < numColors; i++) {
            paintColor.setARGB(255, rgbCols[i][0], rgbCols[i][1], rgbCols[i][2]);
            canvas.drawRect(i * totWidth, 0, i * totWidth + blockWidth, COLOR_BAR_HEIGHT, paintColor);
            String val = createValueString(values[i]);
            canvas.drawText(val, i * totWidth + blockWidth / 2, COLOR_BAR_HEIGHT + VAL_BAR_HEIGHT, blackText);
        }

        return result;
    }

    // ------------------------------------------ group ---------------------------------------------

    // create bitmap for coloured 'drop', which indicates the position of the
    // measured colour, and has as its own colours the measured colours.
    public static Bitmap createColourDropBitmapGroup(List<PatchResult> patchResultList) {
        int numPatches = patchResultList.size();
        int[][] rgbCols = new int[numPatches][3];
        float index = patchResultList.get(0).getIndex();
        float[] xyz;

        // get measured RGB colours for all patches
        for (int j = 0; j < numPatches; j++) {
            xyz = patchResultList.get(j).getXyz();
            rgbCols[j] = ColorUtils.XYZtoRGBint(xyz);
        }

        // compute central location of marker, using the index of the matched colour
        JSONArray colors = patchResultList.get(0).getPatch().getColors();
        int numColors = colors.length();
        int blockWidth = Math.round((IMG_WIDTH - (numColors - 1) * COLOR_BAR_HGAP) / numColors);
        int halfBlockWidth = blockWidth / 2;
        int xrange = IMG_WIDTH - blockWidth;
        int totIndex = ResultUtils.INTERPOLATION_NUMBER * (numColors - 1) + 1;
        int xpos = (int) (halfBlockWidth + xrange * index / totIndex);

        // create empty bitmap
        Bitmap result = Bitmap.createBitmap(IMG_WIDTH, (numPatches + 1) * blockWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // create paint
        Paint paintColor = new Paint();
        paintColor.setStyle(Paint.Style.FILL);
        int yStart;

        // iterate over patches and add coloured blocks
        for (int j = 0; j < numPatches; j++) {
            paintColor.setARGB(255, rgbCols[j][0], rgbCols[j][1], rgbCols[j][2]);
            yStart = j * blockWidth;
            canvas.drawRect(xpos - halfBlockWidth, yStart, xpos + halfBlockWidth,
                    yStart + blockWidth, paintColor);
        }

        // finish with triangle, which is filled with the last colour
        yStart = numPatches * blockWidth;

        Path path = new Path();
        path.moveTo(xpos - halfBlockWidth, yStart); // top left
        path.lineTo(xpos + halfBlockWidth, yStart); // top right
        path.lineTo(xpos, yStart + blockWidth); // bottom
        path.lineTo(xpos - halfBlockWidth, yStart); // back to top left
        path.close();

        // draw triangle
        canvas.drawPath(path, paintColor);

        return result;
    }

    // creates bitmap with colour blocks and values.
    public static Bitmap createColourBarsBitmapGroup(List<PatchResult> patchResultList) {
        JSONArray colors = patchResultList.get(0).getPatch().getColors();
        int numColors = colors.length();
        int numPatches = patchResultList.size();
        int[][][] rgbCols = new int[numPatches][numColors][3];
        float[] values = new float[numColors];
        float[] lab = new float[3];
        int[] rgb;

        // get colors from json, and turn them into sRGB
        try {
            for (int j = 0; j < numPatches; j++) {
                colors = patchResultList.get(j).getPatch().getColors();
                // get lab colours and turn them to RGB
                for (int i = 0; i < numColors; i++) {
                    JSONArray patchColorValues = colors.getJSONObject(i).getJSONArray(SensorConstants.LAB);
                    lab[0] = (float) patchColorValues.getDouble(0);
                    lab[1] = (float) patchColorValues.getDouble(1);
                    lab[2] = (float) patchColorValues.getDouble(2);

                    rgb = ColorUtils.XYZtoRGBint(ColorUtils.Lab2XYZ(lab));
                    rgbCols[j][i] = rgb;
                    values[i] = (float) colors.getJSONObject(i).getDouble(SensorConstants.VALUE);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create empty bitmap
        Bitmap result = Bitmap.createBitmap(IMG_WIDTH, numPatches * (COLOR_BAR_HEIGHT + COLOR_BAR_VGAP)
                + VAL_BAR_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // create paints
        Paint paintColor = new Paint();
        paintColor.setStyle(Paint.Style.FILL);

        Paint blackText = new Paint();
        blackText.setColor(android.graphics.Color.BLACK);
        blackText.setTextSize(TEXT_SIZE);
        blackText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        blackText.setTextAlign(Paint.Align.CENTER);

        int blockWidth = Math.round((IMG_WIDTH - (numColors - 1) * COLOR_BAR_HGAP) / numColors);
        int totWidth = COLOR_BAR_HGAP + blockWidth;
        int yStart;

        // cycle over patches
        for (int j = 0; j < numPatches; j++) {
            // cycle over colours and create blocks and value
            yStart = j * (COLOR_BAR_HEIGHT + COLOR_BAR_VGAP);
            for (int i = 0; i < numColors; i++) {
                paintColor.setARGB(255, rgbCols[j][i][0], rgbCols[j][i][1], rgbCols[j][i][2]);
                canvas.drawRect(i * totWidth, yStart, i * totWidth + blockWidth,
                        yStart + COLOR_BAR_HEIGHT, paintColor);

                if (i == numColors - 1) {
                    String val = createValueString(values[i]);
                    yStart = numColors * (COLOR_BAR_HEIGHT + COLOR_BAR_VGAP) + VAL_BAR_HEIGHT;
                    canvas.drawText(val, i * totWidth + blockWidth / 2, yStart, blackText);
                }
            }
        }
        return result;
    }
}
