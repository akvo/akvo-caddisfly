package org.akvo.caddisfly.sensor.striptest.utils;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardData;
import org.akvo.caddisfly.sensor.striptest.models.CalibrationCardException;
import org.akvo.caddisfly.sensor.striptest.models.DecodeData;
import org.akvo.caddisfly.sensor.striptest.qrdetector.BitMatrix;
import org.akvo.caddisfly.sensor.striptest.qrdetector.Detector;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.striptest.qrdetector.PerspectiveTransform;
import org.akvo.caddisfly.sensor.striptest.qrdetector.ResultPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.akvo.caddisfly.sensor.striptest.qrdetector.MathUtils.distance;
import static org.akvo.caddisfly.sensor.striptest.utils.MathUtils.meanMedianMax;

public class CalibrationCardUtils {
    private static final int VERSION_NUMBER_NOT_FOUND_CODE = 0;
    /*
     * Samples known "white" points on the color card, and creates an array
     */
    private static final int NUM_SAMPLES_PER_LINE = 10;
    private final static float PATCH_SAMPLE_FRACTION = 0.5f;

    /**
     * find and decode the code of the calibration card
     * The code is stored as a simple barcode. It starts 4.5 modules from the center of the bottom left finder pattern
     * and extends to module 29.5.
     * It has 12 bits, of 2 modules wide each.
     * It starts and ends with a 1 bit.
     * The remaining 10 bits are interpreted as a 9 bit number with the last bit as parity bit.
     * Position barcode:
     * _________________________________________________________
     * |                                                        |
     * |________________                                        |
     * ||0             1|                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||               |                                       |
     * ||              b|                                       |
     * ||              b|                                       |
     * ||2_____________3|                                       |
     * |________________________________________________________|
     */
    public static int decodeCalibrationCardCode(@NonNull List<FinderPattern> patternInfo, @NonNull BitMatrix image) {
        // patterns are ordered top left, top right, bottom left, bottom right (in portrait mode, with black area to the right)
        if (patternInfo.size() == 4) {
            ResultPoint bottomLeft = new ResultPoint(patternInfo.get(3).getX(), patternInfo.get(3).getY());
            ResultPoint bottomRight = new ResultPoint(patternInfo.get(1).getX(), patternInfo.get(1).getY());

            // get estimated module size
            Detector detector = new Detector(image);
            float modSize = detector.calculateModuleSize(bottomLeft, bottomRight, bottomRight);

            // go from one finder pattern to the other,
            //because camera is in portrait mode, we need to shift x and y
            double lrx = bottomRight.getX() - bottomLeft.getX();
            double lry = bottomRight.getY() - bottomLeft.getY();
            double hNorm = distance(bottomLeft.getX(), bottomLeft.getY(),
                    bottomRight.getX(), bottomRight.getY());

            // check if left and right are ok
            if (lry > 0) {
                return VERSION_NUMBER_NOT_FOUND_CODE;
            }

            // create vector of length 1 pixel, in the direction of the bottomRight finder pattern
            lrx /= hNorm;
            lry /= hNorm;

            // sample line into new row
            boolean[] bits = new boolean[image.getHeight()];
            int index = 0;
            double px = bottomLeft.getX();
            double py = bottomLeft.getY();
            try {
                while (px > 0 && py > 0 && px < image.getWidth() && py < image.getHeight()) {
                    bits[index] = image.get((int) Math.round(px), (int) Math.round(py));
                    px += lrx;
                    py += lry;
                    index++;
                }
            } catch (Exception e) {
                Timber.d("Error sample line into new row");
                return VERSION_NUMBER_NOT_FOUND_CODE;
            }

            // starting index: 4.5 modules in the direction of the bottom right finder pattern
            // end index: our pattern ends at module 17, so we take 25 to be sure.
            int startIndex = (int) Math.abs(Math.round(4.5 * modSize / lry));
            int endIndex = (int) Math.abs(Math.round(25 * modSize / lry));

            // determine qualityChecksOK of pattern: first black bit. Approach from the left
            try {
                int startI = startIndex;
                while (startI < endIndex && !bits[startI]) {
                    startI++;
                }

                // determine end of pattern: last black bit. Approach from the right
                int endI = endIndex;
                while (endI > startI && !bits[endI]) {
                    endI--;
                }

                int lengthPattern = endI - startI + 1;

                // sanity check on length of pattern.
                // We put the minimum size at 20 pixels, which would correspond to a module size of less than 2 pixels,
                // which is too small.
                if (lengthPattern < 20) {
                    Timber.d("Length of pattern too small");
                    return VERSION_NUMBER_NOT_FOUND_CODE;
                }

                double pWidth = lengthPattern / 12.0;

                // determine bits by majority voting
                int[] bitVote = new int[12];
                for (int i = 0; i < 12; i++) {
                    bitVote[i] = 0;
                }

                int bucket;
                for (int i = startI; i <= endI; i++) {
                    bucket = (int) Math.round(Math.floor((i - startI) / pWidth));
                    bitVote[bucket] += bits[i] ? 1 : -1;
                }

                // translate into information bits. Skip first and last, which are always 1
                boolean[] bitResult = new boolean[10]; // will contain the information bits
                for (int i = 1; i < 11; i++) {
                    bitResult[i - 1] = bitVote[i] > 0;
                }

                // check parity bit
                if (parity(bitResult) != bitResult[9]) {
                    return VERSION_NUMBER_NOT_FOUND_CODE;
                }

                // compute result
                int code = 0;
                int count = 0;
                for (int i = 8; i >= 0; i--) {
                    if (bitResult[i]) {
                        code += (int) Math.pow(2, count);
                    }
                    count++;
                }

                return code;
            } catch (Exception e) {
                return VERSION_NUMBER_NOT_FOUND_CODE;
            }
        } else {
            return VERSION_NUMBER_NOT_FOUND_CODE;
        }
    }

    /**
     * Compute even parity, where last bit is the even parity bit
     */
    private static boolean parity(@NonNull boolean[] bits) {
        int oneCount = 0;
        for (int i = 0; i < bits.length - 1; i++) {  // skip parity bit in calculation of parity
            if (bits[i]) {
                oneCount++;
            }
        }
        return oneCount % 2 != 0; // returns true if parity is odd
    }

    public static void readCalibrationFile(CalibrationCardData calCardData, int version) throws CalibrationCardException {
//        Log.d(TAG, "reading calibration file");
        String calFileName = "calibration-v2-" + version + ".json";
        String json = AssetsManager.getInstance().loadJSONFromAsset(calFileName);

//        boolean success = false;
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);

                // general data
                //calData.date = obj.getString("date");
                // calData.cardVersion = obj.getString("cardVersion");
                // calData.unit = obj.getString("unit");

                calCardData.version = obj.getInt("cardCode");
                // sizes
                JSONObject calDataJSON = obj.getJSONObject("calData");
                calCardData.setPatchSize((float) calDataJSON.getDouble("patchSize"));
                calCardData.hSize = (float) calDataJSON.getDouble("hSize");
                calCardData.vSize = (float) calDataJSON.getDouble("vSize");

                // locations
                JSONArray locJSON = calDataJSON.getJSONArray("locations");
                for (int i = 0; i < locJSON.length(); i++) {
                    JSONObject loc = locJSON.getJSONObject(i);
                    calCardData.addLocation(loc.getString("l"), (float) loc.getDouble("x"), (float) loc.getDouble("y"), loc.getBoolean("gray"));
                }

                // colors
                JSONArray colJSON = calDataJSON.getJSONArray("calValues");
                for (int i = 0; i < colJSON.length(); i++) {
                    JSONObject cal = colJSON.getJSONObject(i);
                    // We don't scale the incoming XYZ data, so it has a range [0..100]
                    calCardData.addCal(cal.getString("l"), (float) cal.getDouble("X"), (float) cal.getDouble("Y"), (float) cal.getDouble("Z"));
                }

                // white lines
                JSONArray linesJSON = obj.getJSONObject("whiteData").getJSONArray("lines");
                for (int i = 0; i < linesJSON.length(); i++) {
                    JSONObject line = linesJSON.getJSONObject(i);
                    JSONArray p = line.getJSONArray("p");
                    calCardData.addWhiteLine((float) p.getDouble(0), (float) p.getDouble(1), (float) p.getDouble(2), (float) p.getDouble(3), (float) line.getDouble("width"));
                }

                // strip area
                JSONArray stripArea = obj.getJSONObject("stripAreaData").getJSONArray("area");
                calCardData.setStripArea((float) stripArea.getDouble(0), (float) stripArea.getDouble(1), (float) stripArea.getDouble(2), (float) stripArea.getDouble(3));

            } catch (JSONException e) {
                throw new CalibrationCardException("Error reading calibration card");
            }
        } else {
            throw new CalibrationCardException("Unknown version of calibration card");
        }
    }

    public static float[][] createWhitePointArray(DecodeData decodeData, CalibrationCardData calCardData) {
        PerspectiveTransform cardToImageTransform = decodeData.getCardToImageTransform();

        byte[] yDataArray = decodeData.getDecodeImageByteArray();
        int rowStride = decodeData.getDecodeWidth();
        List<CalibrationCardData.WhiteLine> lines = calCardData.getWhiteLines();
        int numLines = lines.size() * NUM_SAMPLES_PER_LINE; // on each line, we sample NUM_LINES points
        float[][] points = new float[numLines][3];
        int index = 0;
        float fraction = 1.0f / (NUM_SAMPLES_PER_LINE - 1);
        for (CalibrationCardData.WhiteLine line : lines) {
            // these coordinates are in the card-space
            float xStart = line.getPosition()[0];
            float yStart = line.getPosition()[1];
            float xEnd = line.getPosition()[2];
            float yEnd = line.getPosition()[3];
            float xStep = (xEnd - xStart) * fraction;
            float yStep = (yEnd - yStart) * fraction;

            // sample line
            for (int i = 0; i <= NUM_SAMPLES_PER_LINE - 1; i++) {
                float xp = xStart + i * xStep;
                float yp = yStart + i * yStep;

                points[index * NUM_SAMPLES_PER_LINE + i][0] = xp;
                points[index * NUM_SAMPLES_PER_LINE + i][1] = yp;
                float yVal = getYVal(yDataArray, rowStride, xp, yp, cardToImageTransform);
                points[index * NUM_SAMPLES_PER_LINE + i][2] = yVal;
            }
            index++;
        }
        return points;
    }

    // Get the average Y value in a 3x3 area around the central point.
    private static float getYVal(byte[] yData, int rowStride, float xp, float yp, PerspectiveTransform cardToImageTransform) {
        float totY = 0;
        int totNum = 0;

        // transform from card-space to image-space
        float[] points = new float[]{xp, yp};
        cardToImageTransform.transformPoints(points);
        for (int i = Math.round(points[0]) - 1; i <= Math.round(points[0]) + 1; i++) {
            for (int j = Math.round(points[1]) - 1; j <= Math.round(points[1]) + 1; j++) {
                totY += yData[j * rowStride + i] & 0xff;
                totNum++;
            }
        }
        return totY / totNum;
    }

    /*
     * measure colour patches on colour card and create an array
     */
    public static Map<String, float[]> measurePatches(CalibrationCardData calCardData, DecodeData decodeData) {
        PerspectiveTransform cardToImageTransform = decodeData.getCardToImageTransform();

        byte[] iDataArray = decodeData.getDecodeImageByteArray();
        int rowStride = decodeData.getDecodeWidth();
        int frameSize = rowStride * decodeData.getDecodeHeight();

        float totY;
        float totU;
        float totV;
        int Y, U, V, uvPos;
        int num;

        float patchSize = calCardData.getPatchSize();
        float halfSampleSize = 0.5f * patchSize * PATCH_SAMPLE_FRACTION;

        Map<String, float[]> patchYUVMap = new HashMap<>();

        for (String label : calCardData.getCalValues().keySet()) {
            //CalibrationCardData.CalValue cal = calCardData.getCalValues().get(label);
            CalibrationCardData.Location loc = calCardData.getLocations().get(label);

            // upper and lower corners of the sample area, in card coordinates
            float[] points = new float[]{loc.x - halfSampleSize, loc.y - halfSampleSize, loc.x + halfSampleSize, loc.y + halfSampleSize};
            cardToImageTransform.transformPoints(points);
            totY = 0.0f;
            totU = 0.0f;
            totV = 0.0f;
            num = 0;

            // follows https://stackoverflow.com/questions/12469730/confusion-on-yuv-nv21-conversion-to-rgb
            for (int x = Math.round(points[0]); x <= Math.round(points[2]); x++) {
                for (int y = Math.round(points[3]); y <= Math.round(points[1]); y++) {
                    uvPos = frameSize + (y >> 1) * rowStride;
                    Y = (0xff & iDataArray[x + y * rowStride]);
                    V = (0xff & ((int) iDataArray[uvPos + (x & ~1)])) - 128;
                    U = (0xff & ((int) iDataArray[uvPos + (x & ~1) + 1])) - 128;

                    totY += Y;
                    totU += U;
                    totV += V;
                    num++;
                }
            }
            float[] patchColYUV = new float[]{totY / num, totU / num, totV / num};
            patchYUVMap.put(label, patchColYUV);
        }
        return patchYUVMap;
    }

    // Android YUV to sRGB
    // RGB has scale [0..255]
    public static Map<String, float[]> YUVtoLinearRGB(Map<String, float[]> patchYUVMap) {
        Map<String, float[]> patchRGBMap = new HashMap<>();
        for (String label : patchYUVMap.keySet()) {
            float[] col = patchYUVMap.get(label);
            float[] rgb = ColorUtils.YUVtoLinearRGB(col);
            patchRGBMap.put(label, rgb);
        }
        return patchRGBMap;
    }

    public static Map<String, float[]> calCardXYZ(Map<String, CalibrationCardData.CalValue> calValues) {
        Map<String, float[]> patchXYZMap = new HashMap<>();
        for (String label : calValues.keySet()) {
            CalibrationCardData.CalValue xyzCol = calValues.get(label);
            float[] XYZ = new float[3];
            XYZ[0] = xyzCol.getX();
            XYZ[1] = xyzCol.getY();
            XYZ[2] = xyzCol.getZ();
            patchXYZMap.put(label, XYZ);
        }
        return patchXYZMap;
    }

    public static float[] deltaE2000stats(Map<String, float[]> calibrationXYZMap, Map<String, float[]> patchXYZMap) {
        float[] deltaEArray = new float[patchXYZMap.keySet().size()];
        int i = 0;
        float deltaE2000;
        float[] calibrationColXYZ, cardColXYZ, calibrationColLab, cardColLab;
        for (String label : patchXYZMap.keySet()) {
            calibrationColXYZ = calibrationXYZMap.get(label);
            cardColXYZ = patchXYZMap.get(label);

            calibrationColLab = ColorUtils.XYZtoLAB(calibrationColXYZ);
            cardColLab = ColorUtils.XYZtoLAB(cardColXYZ);

            deltaE2000 = ColorUtils.deltaE2000(calibrationColLab, cardColLab);
            deltaEArray[i] = deltaE2000;
            i++;
        }
        return meanMedianMax(deltaEArray);
    }
}
