/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.ImageHelper;
import org.akvo.caddisfly.preference.AppPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Set of utility functions to manipulate images
 */
public final class ImageUtil {

    private static final String TAG = "ImageUtil";

    private static final int FOUND_CIRCLE_RADIUS = 50;
    private static final int IMAGE_CENTER_CIRCLE_RADIUS = 20;
    //Custom color matrix to convert to GrayScale
    private static final float[] MATRIX = new float[]{
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0, 0, 0, 1, 0};


    private ImageUtil() {
    }

    /**
     * Decode bitmap from byte array
     *
     * @param bytes the byte array
     * @return the bitmap
     */
    public static Bitmap getBitmap(@NonNull byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Crop a bitmap to a square shape with  given length
     *
     * @param bitmap the bitmap to crop
     * @param length the length of the sides
     * @return the cropped bitmap
     */
    @SuppressWarnings("SameParameterValue")
    public static Bitmap getCroppedBitmap(@NonNull Bitmap bitmap, int length, boolean detectBackdrop) {

        int[] pixels = new int[length * length];

        int centerX = bitmap.getWidth() / 2;
        int centerY = bitmap.getHeight() / 2;
        Point point;

        if (!detectBackdrop || AppPreferences.getNoBackdropDetection()) {
            point = new Point(centerX, centerY);
        } else {
            point = ImageHelper.getCenter(bitmap);
            if (point == null) {
                return null;
            }
        }

        bitmap.getPixels(pixels, 0, length,
                point.x - (length / 2),
                point.y - (length / 2),
                length,
                length);

        Bitmap croppedBitmap = Bitmap.createBitmap(pixels, 0, length,
                length,
                length,
                Bitmap.Config.ARGB_8888);
        croppedBitmap = ImageUtil.getRoundedShape(croppedBitmap, length);
        croppedBitmap.setHasAlpha(true);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawCircle(point.x, point.y, FOUND_CIRCLE_RADIUS, paint);

        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(2);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, IMAGE_CENTER_CIRCLE_RADIUS, paint);

        return croppedBitmap;
    }


    public static Bitmap getGrayscale(@NonNull Bitmap src) {

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(MATRIX);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    /**
     * Crop bitmap image into a round shape
     *
     * @param bitmap   the bitmap
     * @param diameter the diameter of the resulting image
     * @return the rounded bitmap
     */
    private static Bitmap getRoundedShape(@NonNull Bitmap bitmap, int diameter) {

        Bitmap resultBitmap = Bitmap.createBitmap(diameter,
                diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Path path = new Path();
        path.addCircle(((float) diameter - 1) / 2,
                ((float) diameter - 1) / 2,
                (((float) diameter) / 2),
                Path.Direction.CCW
        );

        canvas.clipPath(path);
        resultBitmap.setHasAlpha(true);
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(0, 0, diameter, diameter), null
        );
        return resultBitmap;
    }

    public static void saveImage(@NonNull byte[] data, String subfolder, String fileName) {

        File path = FileHelper.getFilesDir(FileHelper.FileType.IMAGE, subfolder);

        File photo = new File(path, fileName + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(photo.getPath());
            fos.write(data);
        } catch (Exception ignored) {

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public static Bitmap rotateImage(@NonNull Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }
}
