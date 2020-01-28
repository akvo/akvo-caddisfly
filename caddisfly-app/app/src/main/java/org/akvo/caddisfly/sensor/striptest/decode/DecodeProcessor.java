package org.akvo.caddisfly.sensor.striptest.decode;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardData;
import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.akvo.caddisfly.sensor.striptest.qrdetector.BitMatrix;
import org.akvo.caddisfly.sensor.striptest.qrdetector.BitMatrixCreator;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternFinder;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.qrdetector.PerspectiveTransform;
import org.akvo.caddisfly.sensor.striptest.ui.StriptestHandler;
import org.akvo.caddisfly.sensor.striptest.utils.CalibrationCardUtils;
import org.akvo.caddisfly.sensor.striptest.utils.CalibrationUtils;
import org.akvo.caddisfly.sensor.striptest.utils.ColorUtils;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.striptest.utils.ImageUtils;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DecodeProcessor {

    public static final int NO_TILT = -1;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_0 = 0;
    // holds reference to the striptestHandler, which we need to pass messages
    private final StriptestHandler striptestHandler;

    /********************************** check exposure ******************************************/
    private final Runnable runExposureQualityCheck = () -> {
        try {
            checkExposureQuality();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };
    /*********************************** check shadow quality ***********************************/
    private final Runnable runShadowQualityCheck = () -> {
        try {
            checkShadowQuality();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };
    /********************************** calibration *********************************************/
    private final Runnable runCalibration = () -> {
        try {
            calibrateCard();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };
    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;
    // instance of BitMatrixCreator
    private BitMatrixCreator mBitMatrixCreator;
    /******************************************* find possible centers **************************/
    private final Runnable runFindPossibleCenters = () -> {
        try {
            findPossibleCenters();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
            //throw new RuntimeException("Can't start finding centers");
        }
    };
    private int mCurrentDelay;

    /*********************************** store data *********************************************/
    private final Runnable runStoreData = () -> {
        try {
            storeData();
        } catch (Exception e) {
            // TODO find out how we gracefully get out in this case
        }
    };

    public DecodeProcessor(StriptestHandler striptestHandler) {
        mDecodeThread = new HandlerThread("DecodeProcessor");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper());
        this.striptestHandler = striptestHandler;
    }

    private static float distance(double x1, double y1, double x2, double y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    //method to calculate the amount of perspective, based on the difference of distances at the top and sides
    // horizontal and vertical are according to calibration card in landscape view
    @Nullable
    private static float[] getTilt(@Nullable FinderPatternInfo info) {
        if (info == null) {
            return null;
        }

        // compute distances
        // in info, we have topLeft, topRight, bottomLeft, bottomRight
        float hDistanceTop = distance(info.getBottomLeft().getX(), info.getBottomLeft().getY(),
                info.getTopLeft().getX(), info.getTopLeft().getY());
        float hDistanceBottom = distance(info.getBottomRight().getX(), info.getBottomRight().getY(),
                info.getTopRight().getX(), info.getTopRight().getY());
        float vDistanceLeft = distance(info.getBottomLeft().getX(), info.getBottomLeft().getY(),
                info.getBottomRight().getX(), info.getBottomRight().getY());
        float vDistanceRight = distance(info.getTopRight().getX(), info.getTopRight().getY(),
                info.getTopLeft().getX(), info.getTopLeft().getY());

        // return ratio of horizontal distances top and bottom and ratio of vertical distances left and right
        return new float[]{hDistanceTop / hDistanceBottom, vDistanceLeft / vDistanceRight};
    }

    @SuppressWarnings("SameParameterValue")
    private static float capValue(float val, float min, float max) {
        if (val > max) {
            return max;
        }
        return val < min ? min : val;
    }

    public void startFindPossibleCenters() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runFindPossibleCenters);
            mDecodeHandler.post(runFindPossibleCenters);
        }
//        else {
        // TODO find out how we gracefully get out in this case
        // throw new RuntimeException("can't find possible centers");
//        }
    }

    private void findPossibleCenters() {
        List<FinderPattern> possibleCenters;
        FinderPatternInfo patternInfo;
        BitMatrix bitMatrix;

        DecodeData decodeData = StriptestHandler.getDecodeData();

        final int decodeHeight = decodeData.getDecodeHeight();
        final int decodeWidth = decodeData.getDecodeWidth();

        if (mBitMatrixCreator == null) {
            mBitMatrixCreator = new BitMatrixCreator(decodeWidth, decodeHeight);
        }

        // create a black and white bit matrix from our data. Cut out the part that interests us
        try {
            bitMatrix = BitMatrixCreator.createBitMatrix(decodeData.getDecodeImageByteArray(),
                    decodeWidth, decodeWidth, decodeHeight, 0, 0,
                    (int) Math.round(decodeHeight * Constants.CROP_FINDER_PATTERN_FACTOR),
                    decodeHeight);
        } catch (Exception e) {
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.DECODE_FAILED_MESSAGE, 0);
            return;
        }

        // if we have valid data, try to find the finder patterns
        if (bitMatrix != null && decodeWidth > 0 && decodeHeight > 0) {
            FinderPatternFinder finderPatternFinder = new FinderPatternFinder(bitMatrix);

            try {
                patternInfo = finderPatternFinder.find(null);
                possibleCenters = finderPatternFinder.getPossibleCenters();

                //We only get four finder patterns back. If one of them is very small, we know we have
                // picked up noise and we should break early.
                for (int i = 0; i < possibleCenters.size(); i++) {
                    if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
                        decodeData.setPatternInfo(null);
                        MessageUtils.sendMessage(striptestHandler, StriptestHandler.DECODE_FAILED_MESSAGE, 0);
                        return;
                    }
                }

            } catch (Exception ignored) {
                // patterns where not detected.
                decodeData.setPatternInfo(null);
                MessageUtils.sendMessage(striptestHandler, StriptestHandler.DECODE_FAILED_MESSAGE, 0);
                return;
            }

            // compute and store tilt and distance check
            decodeData.setTilt(getDegrees(Objects.requireNonNull(getTilt(patternInfo))));
            decodeData.setDistanceOk(distanceOk(patternInfo, decodeHeight));

            // store finder patterns
            decodeData.setPatternInfo(patternInfo);

            decodeData.setFinderPatternsFound(possibleCenters);

            // store decode image size
//            decodeData.setDecodeSize(new Size(decodeWidth, decodeHeight));

            // get the version number from the barcode printed on the card
            if (possibleCenters.size() == 4 && !decodeData.isCardVersionEstablished()) {
                int versionNumber = CalibrationCardUtils.decodeCalibrationCardCode(possibleCenters, bitMatrix);
                decodeData.addVersionNumber(versionNumber);
            }

            // send the message that the decoding was successful
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.DECODE_SUCCEEDED_MESSAGE, 0);
        }
    }

    public void startExposureQualityCheck() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runExposureQualityCheck);
            mDecodeHandler.post(runExposureQualityCheck);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    /*
     * checks exposure of image, by looking at the Y value of the white. It should be as high as
     * possible, without being overexposed.
     */
    private void checkExposureQuality() {
        DecodeData decodeData = StriptestHandler.getDecodeData();
        int maxY;
        int maxMaxY = 0;
        if (decodeData.getFinderPatternsFound() != null) {
            for (FinderPattern fp : decodeData.getFinderPatternsFound()) {
                maxY = ImageUtils.maxY(decodeData, fp);
                if (maxY > maxMaxY) {
                    maxMaxY = maxY;
                }
            }
        }

        if (maxMaxY < Constants.MAX_LUM_LOWER) {
            // send the message that the Exposure should be changed upwards
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.CHANGE_EXPOSURE_MESSAGE, 2);
            return;
        }

        if (maxMaxY > Constants.MAX_LUM_UPPER) {
            // send the message that the Exposure should be changed downwards
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.CHANGE_EXPOSURE_MESSAGE, -2);
            return;
        }

        // send the message that the Exposure is ok
        MessageUtils.sendMessage(striptestHandler, StriptestHandler.EXPOSURE_OK_MESSAGE, 0);
    }

    public void startShadowQualityCheck() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runShadowQualityCheck);
            mDecodeHandler.post(runShadowQualityCheck);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    /*
     * checks exposure of image, by looking at the homogeneity of the white values.
     */
    private void checkShadowQuality() {
        DecodeData decodeData = StriptestHandler.getDecodeData();
        CalibrationCardData calCardData = StriptestHandler.getCalCardData();

        float tlCardX = calCardData.hSize;
        float tlCardY = 0f;
        float trCardX = calCardData.hSize;
        float trCardY = calCardData.vSize;
        float blCardX = 0f;
        float blCardY = 0;
        float brCardX = 0;
        float brCardY = calCardData.vSize;

        FinderPatternInfo info = decodeData.getPatternInfo();
        if (info == null) {
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.DECODE_FAILED_MESSAGE, 0);
            return;
        }

        float tlX = info.getTopLeft().getX();
        float tlY = info.getTopLeft().getY();
        float trX = info.getTopRight().getX();
        float trY = info.getTopRight().getY();
        float blX = info.getBottomLeft().getX();
        float blY = info.getBottomLeft().getY();
        float brX = info.getBottomRight().getX();
        float brY = info.getBottomRight().getY();

        // create transform from picture coordinates to calibration card coordinates
        // the calibration card starts with 0,0 in the top left corner, and is measured in mm
        PerspectiveTransform cardToImageTransform = PerspectiveTransform.quadrilateralToQuadrilateral(
                tlCardX, tlCardY, trCardX, trCardY, blCardX, blCardY, brCardX, brCardY,
                tlX, tlY, trX, trY, blX, blY, brX, brY);

        decodeData.setCardToImageTransform(cardToImageTransform);

        // get white point array
        float[][] points = CalibrationCardUtils.createWhitePointArray(decodeData, calCardData);

        // store in decodeData
        decodeData.setWhitePointArray(points);
        if (points.length > 0) {
            // select those that are not ok, looking at Y only
            float sumY = 0;
            float deviation;
            for (float[] point : points) {
                sumY += point[2];
            }
            // compute average illumination Y value
            float avgY = sumY / points.length;

            // take reciprocal for efficiency reasons
            float avgYReciprocal = 1.0f / avgY;

            int numDev = 0;
            List<float[]> badPoints = new ArrayList<>();
            for (float[] point : points) {
                deviation = Math.abs(point[2] - avgY) * avgYReciprocal;
                // count number of points that differ more than CONTRAST_DEVIATION_FRACTION from the average
                if (deviation > Constants.CONTRAST_DEVIATION_FRACTION) {
                    badPoints.add(point);
                    numDev++;

                    // extra penalty for points that differ more than CONTRAST_MAX_DEVIATION_FRACTION from the average
                    if (deviation > Constants.CONTRAST_MAX_DEVIATION_FRACTION) {
                        numDev += 4;
                    }
                }
            }

            // store in decodeData, and send message
            decodeData.setShadowPoints(badPoints);

            // compute percentage of good points
            float devPercent = 100f - (100.0f * numDev) / points.length;
//            decodeData.setPercentageShadow(Math.min(Math.max(50f, devPercent), 100f));

            // if the percentage of good point is under the limit (something like 90%), we fail the test
            if (devPercent < Constants.SHADOW_PERCENTAGE_LIMIT) {
                MessageUtils.sendMessage(striptestHandler, StriptestHandler.SHADOW_QUALITY_FAILED_MESSAGE, 0);
            } else {
                MessageUtils.sendMessage(striptestHandler, StriptestHandler.SHADOW_QUALITY_OK_MESSAGE, 0);
            }
        } else {
            MessageUtils.sendMessage(striptestHandler, StriptestHandler.SHADOW_QUALITY_OK_MESSAGE, 0);
        }
    }

    public void storeImageData(int currentDelay) {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runStoreData);
            mCurrentDelay = currentDelay;
            mDecodeHandler.post(runStoreData);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    private void storeData() {
        // subtract black part and put it in a rectangle. Do the calibration at the same time.
        // 1) determine size of new image array
        // 2) loop over pixels of new image array
        // 3) compute location of pixel using card-to-image transform
        // 4) apply illumination correction
        // 5) apply calibration
        // 6) store new array

        float[] stripArea = StriptestHandler.getCalCardData().getStripArea();
        DecodeData decodeData = StriptestHandler.getDecodeData();
        PerspectiveTransform cardToImageTransform = decodeData.getCardToImageTransform();
        TestInfo testInfo = decodeData.getTestInfo();

        float[] illumination = decodeData.getIlluminationData();
        RealMatrix calMatrix = decodeData.getCalMatrix();

        // we cut of an edge of 1mm at the edges, to avoid any white space captured at the edge
        float tlx = stripArea[0] + Constants.SKIP_MM_EDGE;
        float tly = stripArea[1] + Constants.SKIP_MM_EDGE;
        float brx = stripArea[2] - Constants.SKIP_MM_EDGE;
        float bry = stripArea[3] - Constants.SKIP_MM_EDGE;

        float widthMm = brx - tlx;
        float heightMm = bry - tly;

        float mmPerPixel = 1.0f / Constants.PIXEL_PER_MM;

        int widthPixels = Math.round(widthMm * Constants.PIXEL_PER_MM);
        int heightPixels = Math.round(heightMm * Constants.PIXEL_PER_MM);

        float[][][] XYZ = new float[widthPixels][heightPixels][3];

        float[] rowX = new float[widthPixels];
        float[] rowY = new float[widthPixels];

        byte[] iDataArray = decodeData.getDecodeImageByteArray();
        int rowStride = decodeData.getDecodeWidth();
        int frameSize = rowStride * decodeData.getDecodeHeight();

        int uvPos;
        int xIm;
        int yIm;
        float xCard;
        float yCard;

        float xCal;
        float yCal;
        float zCal;

        float Y;
        float U;
        float V;

        float[] col;
        float[] xyz;
        float[] coefficient = new float[16];

        float c0c0;
        float c1c1;
        float c2c2;

        float ONE_THIRD = 1.0f / 3.0f;

        // we use a lookup table to speed this thing up.
        Map<String, float[]> colourMap = new HashMap<>();
        String label;

        // transform strip area image to calibrated values
        for (int yi = 0; yi < heightPixels; yi++) {
            // fill a row with card-based coordinates, and transform them to image based coordinates
            for (int xi = 0; xi < widthPixels; xi++) {
                rowX[xi] = xi * mmPerPixel + tlx;
                rowY[xi] = yi * mmPerPixel + tly;
            }

            cardToImageTransform.transformPoints(rowX, rowY);

            // get YUV colours from image
            for (int xi = 0; xi < widthPixels; xi++) {
                xIm = Math.round(rowX[xi]);
                yIm = Math.round(rowY[xi]);

                uvPos = frameSize + (yIm >> 1) * rowStride;
                Y = (0xff & iDataArray[xIm + yIm * rowStride]);
                U = (0xff & ((int) iDataArray[uvPos + (xIm & ~1) + 1])) - 128;
                V = (0xff & ((int) iDataArray[uvPos + (xIm & ~1)])) - 128;

                xCard = xi * mmPerPixel + tlx;
                yCard = yi * mmPerPixel + tly;

                //Apply illumination transform
                Y = capValue(Y - (illumination[0] * xCard + illumination[1] * yCard +
                        +illumination[2] * xCard * xCard + illumination[3] * yCard * yCard
                        + illumination[4] * xCard * yCard + illumination[5])
                        + illumination[6], 0.0f, 255.0f);

                // from here on, it is all just colour transforms fixed values.
                // therefore, we try to shortcut this by using a hashMap.
                label = Math.round(Y) + "|" + Math.round(U) + "|" + Math.round(V);
                if (colourMap.containsKey(label)) {
                    xyz = colourMap.get(label);
                    XYZ[xi][yi][0] = xyz[0];
                    XYZ[xi][yi][1] = xyz[1];
                    XYZ[xi][yi][2] = xyz[2];
                } else {
                    // transform YUV to linear RGB
                    col = ColorUtils.YUVtoLinearRGB(new float[]{Y, U, V});

                    // create transformation coefficients
                    c0c0 = col[0] * col[0];
                    c1c1 = col[1] * col[1];
                    c2c2 = col[2] * col[2];
                    coefficient[0] = col[0];
                    coefficient[1] = col[1];
                    coefficient[2] = col[2];
                    coefficient[3] = (float) Math.sqrt(col[0] * col[1]); // sqrt(R * G)
                    coefficient[4] = (float) Math.sqrt(col[1] * col[2]); // sqrt(G * B)
                    coefficient[5] = (float) Math.sqrt(col[0] * col[2]); // sqrt(R * B)
                    coefficient[6] = (float) Math.pow(col[0] * c1c1, ONE_THIRD); // RGG ^ 1/3
                    coefficient[7] = (float) Math.pow(col[1] * c2c2, ONE_THIRD); // GBB ^ 1/3
                    coefficient[8] = (float) Math.pow(col[0] * c2c2, ONE_THIRD); // RBB ^ 1/3
                    coefficient[9] = (float) Math.pow(col[1] * c0c0, ONE_THIRD); // GRR ^ 1/3
                    coefficient[10] = (float) Math.pow(col[2] * c1c1, ONE_THIRD); // BGG ^ 1/3
                    coefficient[11] = (float) Math.pow(col[2] * c0c0, ONE_THIRD); // BRR ^ 1/3
                    coefficient[12] = (float) Math.pow(col[0] * col[1] * col[2], ONE_THIRD); // RGB ^ 1/3

                    xCal = 0;
                    yCal = 0;
                    zCal = 0;

                    for (int i = 0; i <= 12; i++) {
                        xCal += coefficient[i] * calMatrix.getEntry(i, 0);
                        yCal += coefficient[i] * calMatrix.getEntry(i, 1);
                        zCal += coefficient[i] * calMatrix.getEntry(i, 2);
                    }

                    // store the result in the image
                    // XYZ is scale 0..100
                    XYZ[xi][yi][0] = xCal;
                    XYZ[xi][yi][1] = yCal;
                    XYZ[xi][yi][2] = zCal;

                    // store the colour in the hashMap
                    colourMap.put(label, new float[]{xCal, yCal, zCal});
                }
            }
        }

        // get out strip
        float ratioPixelPerMm = widthPixels / widthMm;
        float[][][] result = ImageUtils.detectStrip(XYZ, widthPixels, heightPixels,
                testInfo.getStripLength(), ratioPixelPerMm);

        // store image in decodeData object
        // NOTE: the image has been transposed in the detectStrip method, so here the rows
        // correspond to the vertical dimension.
        if (result != null) {
            decodeData.addStripImage(result, mCurrentDelay);
            decodeData.setStripPixelWidth(result[0].length);
        } else {
            //todo: check null argument
            decodeData.addStripImage(null, mCurrentDelay);
            decodeData.setStripPixelWidth(0);
        }
        MessageUtils.sendMessage(striptestHandler, StriptestHandler.IMAGE_SAVED_MESSAGE, 0);
    }

    public void startCalibration() {
        if (mDecodeHandler != null) {
            mDecodeHandler.removeCallbacks(runCalibration);
            mDecodeHandler.post(runCalibration);
        }
//        else {
        // TODO find out how we gracefully get out in this case
//        }
    }

    private void calibrateCard() {
        // get calibrationPatches as map
        CalibrationCardData calCardData = StriptestHandler.getCalCardData();
        DecodeData decodeData = StriptestHandler.getDecodeData();
        Map<String, float[]> calXYZMap = CalibrationCardUtils.calCardXYZ(calCardData.getCalValues());

        // measure patches
        // YUV here has full scale: [0..255, -128..128, -128 .. 128]
        Map<String, float[]> patchYUVMap = CalibrationCardUtils.measurePatches(calCardData, StriptestHandler.getDecodeData());

        // correct inhomogeneity of illumination
        Map<String, float[]> patchYUVMapCorrected = CalibrationUtils.correctIllumination(patchYUVMap, decodeData, calCardData);

        // color transform: Android YUV (YCbCr) to sRGB D65
        Map<String, float[]> patchRGBMap = CalibrationCardUtils.YUVtoLinearRGB(patchYUVMapCorrected);

        Map<String, float[]> resultXYZMap;
//        float[] deltaE2000Stats2;

        // perform 3rd order root-polynomial calibration on RGB -> XYZ data
        resultXYZMap = CalibrationUtils.rootPolynomialCalibration(decodeData, calXYZMap, patchRGBMap);

        // measure the distance in terms of deltaE2000
        float[] deltaE2000Stats = CalibrationCardUtils.deltaE2000stats(calXYZMap, resultXYZMap);

        // set deltaE2000 stats
        decodeData.setDeltaEStats(deltaE2000Stats);

        MessageUtils.sendMessage(striptestHandler, StriptestHandler.CALIBRATION_DONE_MESSAGE, 0);
    }

    /********************************* utility methods ******************************************/
    public void stopDecodeThread() {
        mDecodeThread.quitSafely();
        try {
            mDecodeThread.join();
            mDecodeThread = null;
            mDecodeHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean distanceOk(@Nullable FinderPatternInfo info, int decodeHeight) {
        float leftStop = Constants.MAX_CLOSER_DIFF * decodeHeight;
        float rightStop = (1 - Constants.MAX_CLOSER_DIFF) * decodeHeight;
        return info != null &&
                (info.getBottomLeft().getY() > rightStop &&
                        info.getTopLeft().getY() < leftStop &&
                        info.getBottomRight().getY() > rightStop &&
                        info.getTopRight().getY() < leftStop);
    }

    private int getDegrees(float[] tiltValues) {
        int degrees;

        // if the horizontal tilt is too large, indicate it
        if (Math.abs(tiltValues[0] - 1) > Constants.MAX_TILT_DIFF) {
            degrees = tiltValues[0] - 1 < 0 ? -DEGREES_90 : DEGREES_90;
            return degrees;
        }

        // if the vertical tilt is too large, indicate it
        if (Math.abs(tiltValues[1] - 1) > Constants.MAX_TILT_DIFF) {
            degrees = tiltValues[1] - 1 < 0 ? DEGREES_180 : DEGREES_0;
            return degrees;
        }
        // we don't have a tilt problem
        return NO_TILT;
    }
}
