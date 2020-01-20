package org.akvo.caddisfly.sensor.striptest.models;

import android.media.Image;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.akvo.caddisfly.helper.FileType;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.decode.DecodeProcessor;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPattern;
import org.akvo.caddisfly.sensor.striptest.qrdetector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.striptest.qrdetector.PerspectiveTransform;
import org.akvo.caddisfly.util.ImageUtil;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;

public class DecodeData {

    private final SparseIntArray versionNumberMap;
    private final SparseArray<float[][][]> stripImageMap;
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
    private float[] illuminationData;
    private RealMatrix calMatrix;
    private int stripPixelWidth;
    private TestInfo testInfo;

    public DecodeData() {
        this.versionNumberMap = new SparseIntArray();
        this.stripImageMap = new SparseArray<>();
    }

    public void addStripImage(float[][][] image, int delay) {
        this.stripImageMap.put(delay, image);
    }

    public SparseArray<float[][][]> getStripImageMap() {
        return this.stripImageMap;
    }

    //put version number in array: number, frequency
    public void addVersionNumber(Integer number) {
        int versionNumber = versionNumberMap.get(number, -1);
        if (versionNumber == -1) {
            versionNumberMap.put(number, 1);
        } else {
            versionNumberMap.put(number, versionNumber + 1);
        }
    }

    public int getMostFrequentVersionNumber() {
        int mostFrequent = 0;
        int largestValue = -1;

        //look for the most frequent value
        for (int i = 0; i < versionNumberMap.size(); i++) {
            int freq = versionNumberMap.valueAt(i);
            if (freq > mostFrequent) {
                mostFrequent = freq;
                largestValue = versionNumberMap.keyAt(i);
            }
        }
        return largestValue;
    }

    public boolean isCardVersionEstablished() {
        int mostFrequent = 0;
        int prevMostFrequent = 0;

        //look for the most frequent value
        for (int i = 0; i < versionNumberMap.size(); i++) {
            int freq = versionNumberMap.valueAt(i);
            if (freq > mostFrequent) {
                prevMostFrequent = mostFrequent;
                mostFrequent = freq;
            }
        }
        // this means we have seen the most frequent version number at least
        // 5 times more often than second most frequent.
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

    public float[] getDeltaEStats() {
        return deltaEStats;
    }

    public void setDeltaEStats(float[] deltaE2000Stats) {
        this.deltaEStats = deltaE2000Stats;
    }

    public byte[] getDecodeImageByteArray() {
        return decodeImageByteArray;
    }

    public void setDecodeImageByteArray(byte[] decodeImageByteArray) {
        this.decodeImageByteArray = decodeImageByteArray;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getDistanceOk() {
        return distanceOk;
    }

    public void setDistanceOk(boolean distanceOk) {
        this.distanceOk = distanceOk;
    }

    public float[] getIlluminationData() {
        return illuminationData;
    }

    public void setIlluminationData(float[] illuminationData) {
        this.illuminationData = illuminationData;
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
        if (decodeImage != null) {
            decodeImage.close();
        }
        decodeImage = null;
        patternInfo = null;
        decodeImageByteArray = null;
        shadowPoints = null;
        tilt = DecodeProcessor.NO_TILT;
        distanceOk = true;
        calMatrix = null;
        illuminationData = null;
    }

    public void clearImageMap() {
        stripImageMap.clear();
    }

    public void saveCapturedImage() {
        ImageUtil.saveYuvImage(decodeImageByteArray, FileType.TEST_IMAGE, testInfo.getName());
    }
}
