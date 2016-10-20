/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;

import org.akvo.caddisfly.R;
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

/**
 * Created by linda on 12/17/15
 */
@SuppressWarnings("deprecation")
abstract class CameraCallbackBase implements Camera.PreviewCallback {
    private final LinkedList<Double> luminanceTrack = new LinkedList<>();
    private final LinkedList<Double> shadowTrack = new LinkedList<>();
    private final int[] qualityChecksArray = new int[]{0, 0, 0};//array containing brightness, shadow, level check values
    private final List<double[]> luminanceList = new ArrayList<>();
    private final Mat src_gray = new Mat();
    boolean stopped;
    CameraViewListener listener;
    Camera.Size previewSize;
    //private int count;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private CalibrationData calibrationData;
    private Mat bgr = null;
    private Mat convert_mYuv = null;
    private FinderPatternInfo info = null;
    private BitMatrix bitMatrix = null;

    CameraCallbackBase(Context context, Camera.Parameters parameters) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement camera view Listener");
        }

        finderPatternColor = ContextCompat.getColor(context, R.color.jungle_green);

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
        float[] tilts = null;
        int luminance = 0;
        int shadow = 0;
        int titleLevel = 0;

        try {
            if (possibleCenters.size() > 0) {
                bgr = new Mat(previewSize.height, previewSize.width, CvType.CV_8UC3);

                //convert preview data to Mat object
                convert_mYuv = new Mat(previewSize.height + previewSize.height / 2, previewSize.width, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

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

                    Imgproc.cvtColor(bgr.submat(roi), src_gray, Imgproc.COLOR_BGR2GRAY);

                    //brightness: add luminance values to list
                    addLuminanceToList(src_gray, luminanceList);
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
                    titleLevel = Math.abs(tilts[0] - 1) < Constant.MAX_TILT_DIFF && Math.abs(tilts[1] - 1) < Constant.MAX_TILT_DIFF ? 1 : 0;
                }
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
                    //101 means 'no data'
                    listener.showShadow(101);
                } else {
                    listener.showShadow(shadowTrack.getLast());
                }

                // Show tilt
                listener.showLevel(tilts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bgr != null) {
                bgr.release();
                bgr = null;
            }

            if (src_gray != null)
                src_gray.release();

            if (convert_mYuv != null)
                convert_mYuv.release();
        }
        return qualityChecksArray;
    }

    private double detectShadows(FinderPatternInfo info, Mat bgr) {
        double shadowPercentage = 101;

        if (bgr == null) {
            return shadowPercentage;
        }

        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if (shadowTrack.size() > 25) {
            shadowTrack.removeFirst();
        }

        if (info != null) {
            double[] tl = new double[]{info.getTopLeft().getX(), info.getTopLeft().getY()};
            double[] tr = new double[]{info.getTopRight().getX(), info.getTopRight().getY()};
            double[] bl = new double[]{info.getBottomLeft().getX(), info.getBottomLeft().getY()};
            double[] br = new double[]{info.getBottomRight().getX(), info.getBottomRight().getY()};
            bgr = OpenCVUtil.perspectiveTransform(tl, tr, bl, br, bgr).clone();

            try {
                if (calibrationData != null) {
                    shadowPercentage = PreviewUtil.getShadowPercentage(bgr, calibrationData);
                    shadowTrack.add(shadowPercentage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bgr != null)
                    bgr.release();
            }
        }

        return shadowPercentage;
    }

    private void addLuminanceToList(Mat src_gray, List<double[]> luminanceList) {
        double[] lumMinMax;

        lumMinMax = PreviewUtil.getDiffLuminosity(src_gray);
        if (lumMinMax.length == 2) {
            luminanceList.add(lumMinMax);
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
        if (luminanceTrack.size() > 25) {
            luminanceTrack.removeFirst();
        }

        if (lumList.size() > 0) {
            // Add highest value of 'white' to track list
            luminanceTrack.addLast(100 * maxLuminance / 255);

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

    FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) throws Exception {
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
        } catch (NotFoundException | NullPointerException e) {
            e.printStackTrace();
        }

        if (bitMatrix != null && previewSize != null) {
            FinderPatternFinder finderPatternFinder = new FinderPatternFinder(bitMatrix);

            try {
                info = finderPatternFinder.find(null);
                possibleCenters = finderPatternFinder.getPossibleCenters();

                //detect centers that are to small in order to get rid of noise
                for (int i = 0; i < possibleCenters.size(); i++) {
                    if (possibleCenters.get(i).getEstimatedModuleSize() < 2) {
                        return null;
                    }
                }

            } catch (Exception ignored) {
                // patterns where not detected.
            }

            //get the version number from the barcode printed on the card
            if (possibleCenters != null && possibleCenters.size() == 4) {
                if (listener != null) {
                    listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);
                }
                int versionNumber = CalibrationCard.decodeCalibrationCardCode(possibleCenters, bitMatrix);
                CalibrationCard.addVersionNumber(versionNumber);
                calibrationData = CalibrationCard.readCalibrationFile();
            }
        }

        return info;
    }
}
