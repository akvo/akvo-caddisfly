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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.CalibrationException;
import org.akvo.caddisfly.sensor.colorimetry.strip.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtil;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.PreviewUtil;
import org.akvo.caddisfly.util.detector.BinaryBitmap;
import org.akvo.caddisfly.util.detector.BitMatrix;
import org.akvo.caddisfly.util.detector.FinderPattern;
import org.akvo.caddisfly.util.detector.FinderPatternFinder;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.akvo.caddisfly.util.detector.HybridBinarizer;
import org.akvo.caddisfly.util.detector.NotFoundException;
import org.akvo.caddisfly.util.detector.PlanarYUVLuminanceSource;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * The base class for camera callback.
 */
@SuppressWarnings("deprecation")
abstract class CameraCallbackBase implements Camera.PreviewCallback {

    private static final int NO_SHADOW_DATA = 101;
    private static final int MAX_LIST_COUNT = 25;
    private static final int MAX_RGB_INT_VALUE = 255;
    @SuppressWarnings("PMD.LooseCoupling")
    private final LinkedList<Double> luminanceTrack = new LinkedList<>();
    @SuppressWarnings("PMD.LooseCoupling")
    private final LinkedList<Double> shadowTrack = new LinkedList<>();
    private final int[] qualityChecksArray = new int[]{0, 0, 0}; //array containing brightness, shadow, level check values
    private final List<double[]> luminanceList = new ArrayList<>();
    private final Mat grayMat = new Mat();
    private boolean stopped;
    private CameraViewListener listener;
    private Camera.Size previewSize;
    //private int count;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private CalibrationData calibrationData;
    private Mat bgr = null;
    private Mat previewMat = null;
    private FinderPatternInfo patternInfo = null;

    CameraCallbackBase(Context context, Camera.Parameters parameters) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Must implement camera view Listener", e);
        }

        finderPatternColor = ContextCompat.getColor(context, R.color.spring_green);

        possibleCenters = new ArrayList<>();

        previewSize = parameters.getPreviewSize();

        CalibrationCard.initialize();
    }

    void stop() {
        stopped = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    int[] qualityChecks(byte[] data, FinderPatternInfo info) {
        luminanceList.clear();
        float[] tilts;
        int luminance;
        int shadow = 0;
        int titleLevel = 0;

        try {
            if (possibleCenters != null) {
                bgr = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC3);

                //convert preview data to Mat object
                previewMat = new Mat(previewSize.height + previewSize.height / 2, previewSize.width, CvType.CV_8UC1);
                previewMat.put(0, 0, data);
                Imgproc.cvtColor(previewMat, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

                for (int i = 0; i < possibleCenters.size(); i++) {
                    double esModSize = possibleCenters.get(i).getEstimatedModuleSize();

                    // find top left and bottom right coordinates of finder pattern
                    double minX = Math.max(possibleCenters.get(i).getX() - 4 * esModSize, 0);
                    double minY = Math.max(possibleCenters.get(i).getY() - 4 * esModSize, 0);
                    double maxX = Math.min(possibleCenters.get(i).getX() + 4 * esModSize, bgr.width());
                    double maxY = Math.min(possibleCenters.get(i).getY() + 4 * esModSize, bgr.height());
                    Point topLeft = new Point(minX, minY);
                    Point bottomRight = new Point(maxX, maxY);

                    // make grayscale subMat of finder pattern
                    org.opencv.core.Rect roi = new org.opencv.core.Rect(topLeft, bottomRight);

                    Imgproc.cvtColor(bgr.submat(roi), grayMat, Imgproc.COLOR_BGR2GRAY);

                    //brightness: add luminance values to list
                    addLuminanceToList(grayMat, luminanceList);
                }
            } else {
                //if no finder patterns are found, remove one from track
                //when device e.g. is put down, slowly the value becomes zero
                //'slowly' being about a second
                if (luminanceTrack.size() > 0) {
                    luminanceTrack.removeFirst();
                }
                if (shadowTrack.size() > 0) {
                    shadowTrack.removeFirst();
                }
            }

            // number of finder patterns can be anything here.
            if (info != null) {
                // Detect brightness
                double maxLuminance = luminosityCheck(luminanceList);
                luminance = maxLuminance > Constant.MAX_LUM_LOWER && maxLuminance <= Constant.MAX_LUM_UPPER ? 1 : 0;

                // Detect shadows
                if (bgr != null && possibleCenters.size() == 4) {
                    double shadowPercentage = detectShadows(info, bgr);
                    shadow = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE ? 1 : 0;
                }

                // Get Tilt
                if (possibleCenters.size() == 4) {
                    tilts = PreviewUtil.getTilt(info);
                    // The tilt in both directions should not exceed Constant.MAX_TILT_DIFF
                    if (tilts != null) {
                        titleLevel = Math.abs(tilts[0] - 1) < Constant.MAX_TILT_DIFF
                                && Math.abs(tilts[1] - 1) < Constant.MAX_TILT_DIFF ? 1 : 0;
                    }
                } else {
                    tilts = null;
                }

                qualityChecksArray[0] = luminance;
                qualityChecksArray[1] = shadow;
                qualityChecksArray[2] = titleLevel;

                //Display the values
                if (listener != null) {
                    // Show brightness values
                    if (luminanceTrack.size() < 1) {
                        //-1 means 'no data'
                        listener.showBrightness(-1);
                    } else {
                        listener.showBrightness(luminanceTrack.getLast());
                    }

                    // Show shadow values
                    if (shadowTrack.size() < 1) {
                        listener.showShadow(NO_SHADOW_DATA);
                    } else {
                        listener.showShadow(shadowTrack.getLast());
                    }

                    // Show tilt
                    listener.showLevel(tilts);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (bgr != null) {
                bgr.release();
                bgr = null;
            }

            if (grayMat != null) {
                grayMat.release();
            }

            if (previewMat != null) {
                previewMat.release();
            }
        }
        return qualityChecksArray;
    }

    private double detectShadows(FinderPatternInfo info, Mat mat) {
        double shadowPercentage = NO_SHADOW_DATA;

        if (mat == null) {
            return shadowPercentage;
        }

        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if (shadowTrack.size() > MAX_LIST_COUNT) {
            shadowTrack.removeFirst();
        }

        if (info != null) {
            double[] tl = new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()};
            double[] tr = new double[]{info.getTopRight().getX(), info.getTopRight().getY()};
            double[] bl = new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()};
            double[] br = new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()};
            mat = OpenCVUtil.perspectiveTransform(tl, tr, bl, br, mat).clone();

            try {
                if (calibrationData != null) {
                    shadowPercentage = PreviewUtil.getShadowPercentage(mat, calibrationData);
                    shadowTrack.add(shadowPercentage);
                }
            } catch (Exception e) {
                Timber.e(e);
            } finally {
                if (mat != null) {
                    mat.release();
                }
            }
        }

        return shadowPercentage;
    }

    private void addLuminanceToList(Mat mat, List<double[]> list) {
        double[] lumMinMax;

        lumMinMax = PreviewUtil.getDiffLuminosity(mat);
        if (lumMinMax.length == 2) {
            list.add(lumMinMax);
        }
    }

    private double luminosityCheck(List<double[]> lumList) {
        double maxLuminance = -1; // highest value of 'white'

        for (int i = 0; i < lumList.size(); i++) {
            // Store lum max value that corresponds with highest: we use it to check over and under exposure
            if (lumList.get(i)[1] > maxLuminance) {
                maxLuminance = lumList.get(i)[1];
            }
        }

        // Fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if (luminanceTrack.size() > MAX_LIST_COUNT) {
            luminanceTrack.removeFirst();
        }

        if (lumList.size() > 0) {
            // Add highest value of 'white' to track list
            luminanceTrack.addLast(100 * maxLuminance / MAX_RGB_INT_VALUE);

            // Compensate for underexposure
            if (maxLuminance < Constant.MAX_LUM_LOWER) {
                // Increase the exposure value
                listener.adjustExposureCompensation(1);
                return maxLuminance;
            }

            // Compensate for overexposure
            if (maxLuminance > Constant.MAX_LUM_UPPER) {
                // We are likely overexposed, so adjust exposure downwards.
                listener.adjustExposureCompensation(-1);
            } else {
                // We want to get it as bright as possible but without risking overexposure
                if (maxLuminance * Constant.PERCENT_ILLUMINATION < Constant.MAX_LUM_UPPER) {
                    // try to increase the exposure one more time
                    listener.adjustExposureCompensation(1);
                }
            }
        }
        return maxLuminance;
    }

    FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) throws CalibrationException {

        BitMatrix bitMatrix = null;

        // crop preview image to only contain the known region for the finder pattern
        // this leads to an image in portrait view
        PlanarYUVLuminanceSource myYUV = new PlanarYUVLuminanceSource(data, size.width,
                size.height, 0, 0,
                (int) Math.round(size.height * Constant.CROP_FINDER_PATTERN_FACTOR),
                size.height,
                false);

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(myYUV));

        try {
            bitMatrix = binaryBitmap.getBlackMatrix();
        } catch (NotFoundException e) {
            Timber.e(e);
        }

        if (bitMatrix != null && previewSize != null) {
            FinderPatternFinder finderPatternFinder = new FinderPatternFinder(bitMatrix);

            try {
                patternInfo = finderPatternFinder.find(null);
                possibleCenters = finderPatternFinder.getPossibleCenters();

                //detect centers that are too small in order to get rid of noise
                for (int i = 0; i < possibleCenters.size(); i++) {
                    if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
                        return null;
                    }
                }

            } catch (Exception ignored) {
                // patterns where not detected.
                possibleCenters = null;
                patternInfo = null;
            }

            //get the version number from the barcode printed on the card
            if (possibleCenters != null && possibleCenters.size() == 4) {
                if (listener != null) {
                    listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);
                }

                // if card version has been read and established then no need to decode again
                if (!CalibrationCard.isCardVersionEstablished()) {
                    int versionNumber = CalibrationCard.decodeCalibrationCardCode(possibleCenters, bitMatrix);
                    CalibrationCard.addVersionNumber(versionNumber);
                }
                calibrationData = CalibrationCard.readCalibrationFile();
            } else {
                listener.showFinderPatterns(null, null, 1);
            }
        }

        return patternInfo;
    }

    boolean isRunning() {
        return !stopped;
    }

    Camera.Size getPreviewSize() {
        return previewSize;
    }

    CameraViewListener getListener() {
        return listener;
    }

}
