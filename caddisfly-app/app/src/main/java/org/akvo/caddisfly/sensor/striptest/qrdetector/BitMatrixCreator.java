package org.akvo.caddisfly.sensor.striptest.qrdetector;

/**
 * Uses a very naive threshold: a single fixed black point. As we have good control
 * over the lighting conditions, this should be sufficient.
 */

public class BitMatrixCreator {

    private static BitMatrix matrix;

    public BitMatrixCreator(int width, int height) {
    }

    // the data has the long dimension as width, and the short dimension as height
    public static BitMatrix createBitMatrix(byte[] yDataArray, int rowStride, int dataWidth,
                                            int dataHeight, int hStart, int vStart,
                                            int width, int height) {

        if (hStart + width > dataWidth || vStart + height > dataHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }

        if (rowStride != dataWidth) {
            throw new IllegalArgumentException("Row stride not equal to data width");
        }

        if (matrix == null) {
            matrix = new BitMatrix(width, height);
        } else {
            matrix.clear();
        }

        if (yDataArray == null) return null;

        // lets use the approximate location of the four corners to estimate the black point
        int size = (int) Math.round(0.25 * width);
        int blackTopLeft = estimateBlackPoint(yDataArray, rowStride, 1, 1, size, size);
        int blackTopRight = estimateBlackPoint(yDataArray, rowStride, width - size, 1, width, size);
        int blackBottomLeft = estimateBlackPoint(yDataArray, rowStride, 1, height - size, size, height);
        int blackBottomRight = estimateBlackPoint(yDataArray, rowStride, width - size, height - size, width, height);

        for (int y = 0; y < height / 2; y++) {
            int offset = y * rowStride;
            for (int x = 0; x < width / 2; x++) {
                int pixel = yDataArray[offset + x] & 0xff;
                if (pixel < blackTopLeft) {
                    matrix.set(x, y);
                }
            }
        }

        for (int y = height / 2; y < height; y++) {
            int offset = y * rowStride;
            for (int x = width / 2; x < width; x++) {
                int pixel = yDataArray[offset + x] & 0xff;
                if (pixel < blackBottomRight) {
                    matrix.set(x, y);
                }
            }
        }

        for (int y = 0; y < height / 2; y++) {
            int offset = y * rowStride;
            for (int x = width / 2; x < width; x++) {
                int pixel = yDataArray[offset + x] & 0xff;
                if (pixel < blackTopRight) {
                    matrix.set(x, y);
                }
            }
        }

        for (int y = height / 2; y < height; y++) {
            int offset = y * rowStride;
            for (int x = 0; x < width / 2; x++) {
                int pixel = yDataArray[offset + x] & 0xff;
                if (pixel < blackBottomLeft) {
                    matrix.set(x, y);
                }
            }
        }

        return matrix;
    }


    private static int estimateBlackPoint(byte[] yData, int rowStride,
                                          int xtl, int ytl, int xbr, int ybr) {
        int yMax = 0;
        int yMin = 1000;
        int val;
        for (int y = ytl; y <= ybr; y += 2) {
            int offset = y * rowStride;
            for (int x = xtl; x < xbr; x += 2) {
                val = yData[offset + x] & 0xff;
                yMax = Math.max(yMax, val);
                yMin = Math.min(yMin, val);
            }
        }
        return (int) Math.round(0.33333 * (2 * yMax + yMin));
    }
}
