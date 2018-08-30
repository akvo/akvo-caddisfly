package org.akvo.caddisfly.sensor.striptest.utils;

import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.List;

/**
 * Created by markwestra on 06/06/2017
 */
public class ImageUtils {

    private static final double LOWER_PERCENTAGE_BOUND = 0.9;
    private static final double UPPER_PERCENTAGE_BOUND = 1.1;

    public static int maxY(DecodeData decodeData, FinderPattern fp) {
        // Compute expected area finder pattern
        float x = fp.getX();
        float y = fp.getY();

        float size = fp.getEstimatedModuleSize();
        int halfWidth = (int) (size * 3.5); // one finder pattern has size 7
        int xTopLeft = Math.max((int) (x - halfWidth), 0);
        int yTopLeft = Math.max((int) (y - halfWidth), 0);
        int xBotRight = Math.min((int) (x + halfWidth), decodeData.getDecodeWidth() - 1);
        int yBotRight = Math.min((int) (y + halfWidth), decodeData.getDecodeHeight() - 1);
        int maxY = 0;

        int rowStride = decodeData.getDecodeWidth();
        byte[] yDataArray = decodeData.getDecodeImageByteArray();

        // iterate over all points and get max Y value
        int i;
        int j;
        int yVal;
        for (j = yTopLeft; j <= yBotRight; j++) {
            int offset = j * rowStride;
            for (i = xTopLeft; i <= xBotRight; i++) {
                yVal = yDataArray[offset + i] & 0xff;
                if (yVal > maxY) {
                    maxY = yVal;
                }
            }
        }
        return maxY;
    }

    // detect strip by multi-step method
    // returns cut-out and rotated resulting strip as mat
    // TODO handle no-strip case
    @SuppressWarnings("UnusedParameters")
    public static float[][][] detectStrip(float[][][] image, int width, int height,
                                          double stripLength, double ratioPixelPerMm) {
        // we need to check if there is a strip on the black area. We
        // combine this with computing a first approximation of line through length of the strip,
        // as we go through all the points anyway.
        final WeightedObservedPoints points = new WeightedObservedPoints();
        final WeightedObservedPoints corrPoints = new WeightedObservedPoints();

        double tot, yTot;
        int totalWhite = 0;
        for (int i = 0; i < width; i++) { // iterate over cols
            tot = 0;
            yTot = 0;
            for (int j = 0; j < height; j++) { // iterate over rows
                // we check if the Y value in XYZ is larger than 50, which we use as cutoff.
                if (image[i][j][1] > 50) {
                    yTot += j;
                    tot++;
                }
            }
            if (tot > 0) {
                points.add((double) i, yTot / tot);
                totalWhite += tot;
            }
        }

        // We should have at least 9% not-black to be sure we have a strip
        if (totalWhite < 0.09 * width * height) {
            return null;
        }

        // fit a line through these points
        // order of coefficients is (b + ax), so [b, a]
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        List<WeightedObservedPoint> pointsList = points.toList();
        final double[] coefficient = fitter.fit(pointsList);

        // second pass, remove outliers
        double estimate, actual;
        for (int i = 0; i < pointsList.size(); i++) {
            estimate = coefficient[1] * pointsList.get(i).getX() + coefficient[0];
            actual = pointsList.get(i).getY();
            if (actual > LOWER_PERCENTAGE_BOUND * estimate && actual < UPPER_PERCENTAGE_BOUND * estimate) {
                //if the point differs less than +/- 10%, keep the point
                corrPoints.add(pointsList.get(i).getX(), pointsList.get(i).getY());
            }
        }

        final double[] coefficientCorr = fitter.fit(corrPoints.toList());
        double slope = coefficientCorr[1];
        double offset = coefficientCorr[0];

        // compute rotation angle
        double rotAngleRad = Math.atan(slope);

        //determine a point on the line, in the middle of strip, in the horizontal middle of the whole image
        int midPointX = width / 2;
        int midPointY = (int) Math.round(midPointX * slope + offset);

        // rotate around the midpoint, to straighten the binary strip
        // compute rotation matrix
        // cosfie -sinfie
        // sinfie cosfie
        float cosfie = (float) Math.cos(rotAngleRad);
        float sinfie = (float) Math.sin(rotAngleRad);

        float xm, ym;
        int xt, yt;

        float[][][] rotatedImage = new float[width][height][3];
        for (int i = 0; i < width; i++) { // iterate over cols
            for (int j = 0; j < height; j++) { // iterate over rows
                xm = i - midPointX;
                ym = j - midPointY;

                xt = Math.round(cosfie * xm - sinfie * ym + midPointX);
                yt = Math.round(sinfie * xm + cosfie * ym + midPointY);

                if (xt < 0 || xt > width - 1 || yt < 0 || yt > height - 1) {
                    rotatedImage[i][j][0] = 0; // X
                    rotatedImage[i][j][1] = 0; // Y
                    rotatedImage[i][j][2] = 0; // Z
                } else {
                    rotatedImage[i][j][0] = image[xt][yt][0];
                    rotatedImage[i][j][1] = image[xt][yt][1];
                    rotatedImage[i][j][2] = image[xt][yt][2];
                }
            }
        }

        // Compute white points in each row
        int[] rowCount = new int[height];
        int rowTot;
        for (int j = 0; j < height; j++) { // iterate over rows
            rowTot = 0;
            for (int i = 0; i < width; i++) { // iterate over cols
                if (rotatedImage[i][j][1] > 50) {
                    rowTot++;
                }
            }
            rowCount[j] = rowTot;
        }

        // find height of strip by finding rising and dropping edges
        // rising edge  = largest positive difference
        // falling edge = largest negative difference
        int risePos = 0;
        int fallPos = 0;
        double riseVal = 0;
        double fallVal = 0;
        for (int i = 0; i < height - 1; i++) {
            if (rowCount[i + 1] - rowCount[i] > riseVal) {
                riseVal = rowCount[i + 1] - rowCount[i];
                risePos = i + 1;
            }
            if (rowCount[i + 1] - rowCount[i] < fallVal) {
                fallVal = rowCount[i + 1] - rowCount[i];
                fallPos = i;
            }
        }

        // now find right end of strip
        // method: first rising edge
        int[] colCount = new int[width];
        int colTotal;
        for (int i = 0; i < width; i++) { // iterate over cols
            colTotal = 0;
            for (int j = risePos; j < fallPos; j++) { // iterate over rows
                if (rotatedImage[i][j][1] > 50) {
                    colTotal++;
                }
            }
            colCount[i] = colTotal;
        }

        // threshold is that half of the rows in a column should be white
        int threshold = (fallPos - risePos) / 2;

        boolean found = false;

        // use known length of strip to determine right side
        double posRight = width - 1;

        // moving from the right, determine the first point that crosses the threshold
        while (!found && posRight > 0) {
            if (colCount[(int) posRight] > threshold) {
                found = true;
            } else {
                posRight--;
            }
        }

        // use known length of strip to determine left side
        int start = (int) Math.round((posRight - (stripLength * ratioPixelPerMm)));
        int end = (int) Math.round(posRight);

        // cut out final strip
        // here, we also transpose the matrix so the rows correspond to the vertical dimension
        float[][][] result = new float[fallPos - risePos + 1][end - start + 1][3];
        for (int i = start; i < end; i++) {
            for (int j = risePos; j < fallPos; j++) {
                result[j - risePos][i - start][0] = rotatedImage[i][j][0];
                result[j - risePos][i - start][1] = rotatedImage[i][j][1];
                result[j - risePos][i - start][2] = rotatedImage[i][j][2];
            }
        }
        return result;
    }
}
