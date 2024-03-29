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

import static org.akvo.caddisfly.helper.FileHelper.getUnitTestImagesFolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import timber.log.Timber;

/**
 * Set of utility functions to manipulate images.
 */
public final class ImageUtil {

    private ImageUtil() {
    }

    public static boolean saveImage(Bitmap bitmap, String filename) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)) {
                return true;
            }
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void checkOrientation(String originalImage, String resizedImage) {
        try {
            ExifInterface exif1 = new ExifInterface(originalImage);
            ExifInterface exif2 = new ExifInterface(resizedImage);

            final String orientation1 = exif1.getAttribute(ExifInterface.TAG_ORIENTATION);
            final String orientation2 = exif2.getAttribute(ExifInterface.TAG_ORIENTATION);

            if (orientation1 != null && !TextUtils.isEmpty(orientation1)
                    && !orientation1.equals(orientation2)) {
                Timber.d("Orientation property in EXIF does not match. Overriding it with original value...");
                exif2.setAttribute(ExifInterface.TAG_ORIENTATION, orientation1);
                exif2.saveAttributes();
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /**
     * resizeImage handles resizing a too-large image file from the camera.
     */
    public static void resizeImage(String origFilename, String outFilename, int width) {
        int reqWidth;
        int reqHeight;
        reqWidth = width;
        reqHeight = 960;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(origFilename, options);

        // If image is in portrait mode, we swap the maximum width and height
        if (options.outHeight > options.outWidth) {
            int tmp = reqHeight;
            //noinspection SuspiciousNameCombination
            reqHeight = reqWidth;
            reqWidth = tmp;
        }

        Timber.d("Orig Image size: %d x %d", options.outWidth, options.outHeight);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(origFilename, options);

        if (bitmap != null && ImageUtil.saveImage(bitmap, outFilename)) {
            ImageUtil.checkOrientation(origFilename, outFilename);// Ensure the EXIF data is not lost
            // Timber.d("Resized Image size: %d x %d", bitmap.getWidth(), bitmap.getHeight());
        }
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * load the  bytes from a file.
     *
     * @param name the file name
     * @return the loaded bytes
     */
    public static byte[] loadImageBytes(String name) {
        File file = new File(getUnitTestImagesFolder(), name + ".yuv");
        if (file.exists()) {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                DataInputStream dis = new DataInputStream(bis);
                dis.readFully(bytes);
            } catch (IOException e) {
                Timber.e(e);
            }
            return bytes;
        }

        return new byte[0];
    }

    /**
     * Save an image in yuv format
     *
     * @param data     the image data
     * @param fileName the name of the file
     */
    public static void saveYuvImage(@NonNull byte[] data, String fileName) {
        File file = new File(getUnitTestImagesFolder(), fileName + ".yuv");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file.getPath());
            fos.write(data);
        } catch (Exception ignored) {
            // do nothing
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }
    }
}
