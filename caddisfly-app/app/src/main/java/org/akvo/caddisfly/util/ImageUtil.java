/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

/**
 * Set of utility functions to manipulate images
 */
public class ImageUtil {

    private ImageUtil() {
    }

    /**
     * Decode bitmap from byte array
     *
     * @param bytes the byte array
     * @return the bitmap
     */
    public static Bitmap getBitmap(byte[] bytes) {
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
    public static Bitmap getCroppedBitmap(Bitmap bitmap, int length) {

        int[] pixels = new int[length * length];

        bitmap.getPixels(pixels, 0, length,
                (bitmap.getWidth() - length) / 2,
                (bitmap.getHeight() - length) / 2,
                length,
                length);
        bitmap = Bitmap.createBitmap(pixels, 0, length,
                length,
                length,
                Bitmap.Config.ARGB_8888);
        bitmap = ImageUtil.getRoundedShape(bitmap, length);
        bitmap.setHasAlpha(true);
        return bitmap;
    }

    /**
     * Crop bitmap image into a round shape
     *
     * @param bitmap   the bitmap
     * @param diameter the diameter of the resulting image
     * @return the rounded bitmap
     */
    private static Bitmap getRoundedShape(Bitmap bitmap, int diameter) {

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
}
