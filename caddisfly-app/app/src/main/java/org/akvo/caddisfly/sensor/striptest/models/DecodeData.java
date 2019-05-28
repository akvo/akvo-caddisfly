package org.akvo.caddisfly.sensor.striptest.models;

import android.media.Image;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.qrdetector.PerspectiveTransform;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by markwestra on 19/05/2017.
 */

public class DecodeData {

//    private static final String TAG = "Caddisfly-DecodeData";

    private final Map<Integer, Integer> versionNumberMap;
    private final Map<Integer, float[][][]> stripImageMap;
    private Image decodeImage;
    private byte[] decodeImageByteArray;
    private int decodeWidth;
    private int decodeHeight;
    private FinderPatternInfo patternInfo;
    private List<FinderPattern> finderPatternsFound;
    private int tilt;
    private boolean distanceOk;
    private PerspectiveTransform cardToImageTransform;
    private List<float[]> shadowPoints;
    private float[][] whitePointArray;
    private float[] deltaEStats;
    private float[] illumData;
    private RealMatrix calMatrix;
    private int stripPixelWidth;
    private TestInfo testInfo;

    public DecodeData() {
        this.versionNumberMap = new HashMap<>();
        this.stripImageMap = new HashMap<>();
    }

    public void addStripImage(float[][][] image, int delay) {
        this.stripImageMap.put(delay, image);
    }

    public Map<Integer, float[][][]> getStripImageMap() {
        return this.stripImageMap;
    }

    //put version number in array: number, frequency
    public void addVersionNumber(Integer number) {
        Integer existingFrequency = versionNumberMap.get(number);
        if (existingFrequency != null) {
            versionNumberMap.put(number, versionNumberMap.get(number) + 1);
        } else {
            versionNumberMap.put(number, 1);
        }
    }

    public int getMostFrequentVersionNumber() {
        int mostFrequent = 0;
        int largestValue = -1;

        //look for the most frequent value
        for (Integer key : versionNumberMap.keySet()) {
            int freq = versionNumberMap.get(key);
            if (freq > mostFrequent) {
                mostFrequent = freq;
                largestValue = key;
            }
        }
        return largestValue;
    }

    public boolean isCardVersionEstablished() {
        int mostFrequent = 0;
        int prevMostFrequent = 0;

        //look for the most frequent value
        for (Integer key : versionNumberMap.keySet()) {
            int freq = versionNumberMap.get(key);
            if (freq > mostFrequent) {
                prevMostFrequent = mostFrequent;
                mostFrequent = freq;
            }
        }
        // this means we have seen the most frequent version number at least 5 times more often
        // second most frequent.
        return mostFrequent - prevMostFrequent > 5;
    }


    public int getDecodeWidth() {
        return decodeWidth;
    }

    public void setDecodeWidth(int decodeWidth) {
        this.decodeWidth = decodeWidth;
    }

    public int getDecodeHeight() {
        return decodeHeight;
    }

    public void setDecodeHeight(int decodeHeight) {
        this.decodeHeight = decodeHeight;
    }

    public List<FinderPattern> getFinderPatternsFound() {
        return finderPatternsFound;
    }

    public void setFinderPatternsFound(List<FinderPattern> finderPatternsFound) {
        this.finderPatternsFound = finderPatternsFound;
    }

    public FinderPatternInfo getPatternInfo() {
        return patternInfo;
    }

    public void setPatternInfo(FinderPatternInfo patternInfo) {
        this.patternInfo = patternInfo;
    }

    public int getTilt() {
        return tilt;
    }

    public void setTilt(int tilt) {
        this.tilt = tilt;
    }

    public PerspectiveTransform getCardToImageTransform() {
        return cardToImageTransform;
    }

    public void setCardToImageTransform(PerspectiveTransform cardToImageTransform) {
        this.cardToImageTransform = cardToImageTransform;
    }

    public List<float[]> getShadowPoints() {
        return shadowPoints;
    }

    public void setShadowPoints(List<float[]> shadowPoints) {
        this.shadowPoints = shadowPoints;
    }

    public float[][] getWhitePointArray() {
        return whitePointArray;
    }

    public void setWhitePointArray(float[][] whitePointArray) {
        this.whitePointArray = whitePointArray;
    }

//    public float getPercentageShadow() {
//        return percentageShadow;
//    }

//    public void setPercentageShadow(float percentageShadow) {
//        this.percentageShadow = percentageShadow;
//    }

    public float[] getDeltaEStats() {
        return deltaEStats;
    }

    public void setDeltaEStats(float[] deltaE2000Stats) {
        this.deltaEStats = deltaE2000Stats;
    }

//    public void setMeasuredPatchRGB(Map<String, int[]> measuredPatchRGB) {
//        this.measuredPatchRGB = measuredPatchRGB;
//    }

//    public void setCalibrationPatchRGB(Map<String, int[]> calibrationPatchRGB) {
//        this.calibrationPatchRGB = calibrationPatchRGB;
//    }

    public byte[] getDecodeImageByteArray() {
        return decodeImageByteArray;
    }

    public void setDecodeImageByteArray(byte[] decodeImageByteArray) {
        this.decodeImageByteArray = decodeImageByteArray;
    }

//    public void setPreviewWidth(int previewWidth) {
//        this.previewWidth = previewWidth;
//    }

//    public void setPreviewHeight(int previewHeight) {
//        this.previewHeight = previewHeight;
//    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getDistanceOk() {
        return distanceOk;
    }

    public void setDistanceOk(boolean distanceOk) {
        this.distanceOk = distanceOk;
    }

    public float[] getIllumData() {
        return illumData;
    }

    public void setIllumData(float[] illumData) {
        this.illumData = illumData;
    }


    public RealMatrix getCalMatrix() {
        return calMatrix;
    }

    public void setCalMatrix(RealMatrix calMatrix) {
        this.calMatrix = calMatrix;
    }

    public TestInfo getTestInfo() {
        return testInfo;
    }

    public void setTestInfo(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    public int getStripPixelWidth() {
        return stripPixelWidth;
    }

    public void setStripPixelWidth(int stripPixelWidth) {
        this.stripPixelWidth = stripPixelWidth;
    }

    public void clearData() {
        if (this.decodeImage != null) {
            this.decodeImage.close();
        }
        this.decodeImage = null;
        this.patternInfo = null;
        this.decodeImageByteArray = null;
        this.shadowPoints = null;
//        this.measuredPatchRGB = null;
//        this.calibrationPatchRGB = null;
        this.tilt = DecodeProcessor.NO_TILT;
        this.distanceOk = true;
        calMatrix = null;
        illumData = null;
    }

    // debug code
//    public void saveImage() {
//        ImageUtil.saveImage(decodeImageByteArray, FileHelper.FileType.TEST_IMAGE,
//                String.valueOf(Calendar.getInstance().getTimeInMillis()));
//    }
}
