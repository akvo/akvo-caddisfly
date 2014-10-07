/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

import org.akvo.caddisfly.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageUtils {

    private ImageUtils() {
    }

    public static Bitmap getAnalysedBitmap(String filePath) {

        try {

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            int height = options.outHeight;
            int width = options.outWidth;
            int sampleLength = Config.IMAGE_CROP_LENGTH;

            if (height < sampleLength || width < sampleLength) {
                sampleLength = Math.min(height, width);
            }

            int leftMargin = (width - sampleLength) / 2;
            int topMargin = (height - sampleLength) / 2;
            if (leftMargin < 0) {
                leftMargin = 0;
            }
            if (topMargin < 0) {
                topMargin = 0;
            }

            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath, false);
            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inPreferQualityOverSpeed = true;
            options1.inPurgeable = true;
            Rect re = new Rect(leftMargin, topMargin, leftMargin + sampleLength,
                    topMargin + sampleLength);

            return decoder.decodeRegion(re, options1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    * Resizing image size
    */
    public static Bitmap decodeFile(String filePath) {
        try {

            File f = new File(filePath);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            int scale = 1;
            while (o.outWidth / scale / 2 >= Config.IMAGE_CROP_LENGTH
                    && o.outHeight / scale / 2 >= Config.IMAGE_CROP_LENGTH) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

 /*   public static void saveBitmap(Bitmap bitmap, String filename) {

        if (bitmap == null) {
            return;
        }

        OutputStream outStream;

        try {
            outStream = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage, int diameter) {

        Bitmap targetBitmap = Bitmap.createBitmap(diameter,
                diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) diameter - 1) / 2,
                ((float) diameter - 1) / 2,
                (((float) diameter) / 2),
                Path.Direction.CCW
        );

        canvas.clipPath(path);
        targetBitmap.setHasAlpha(true);
        canvas.drawBitmap(scaleBitmapImage,
                new Rect(0, 0, scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight()),
                new Rect(0, 0, diameter, diameter), null
        );
        return targetBitmap;
    }
}
