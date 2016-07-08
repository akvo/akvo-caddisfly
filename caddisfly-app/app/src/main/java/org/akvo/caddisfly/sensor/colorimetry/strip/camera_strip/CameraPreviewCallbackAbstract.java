package org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtils;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.PreviewUtils;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationData;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.BinaryBitmap;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.BitMatrix;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPattern;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPatternFinder;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.FinderPatternInfo;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.HybridBinarizer;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.NotFoundException;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.detector.PlanarYUVLuminanceSource;
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
abstract class CameraPreviewCallbackAbstract implements Camera.PreviewCallback {
    private final LinkedList<Double> lumTrack = new LinkedList<>();
    private final LinkedList<Double> shadowTrack = new LinkedList<>();
    private final int[] qualityChecksArray = new int[]{0, 0, 0};//array containing brightness, shadow, level check values
    private final List<double[]> lumList = new ArrayList<>();
    private final Mat src_gray = new Mat();
    CameraViewListener listener;
    Camera.Size previewSize;
    boolean stop;
    //private int count;
    private List<FinderPattern> possibleCenters;
    private int finderPatternColor;
    private CalibrationData calibrationData;
    private Context context;
    private float EV;
    private Mat bgr = null;
    private Mat convert_mYuv = null;
    private FinderPatternInfo info = null;
    private BitMatrix bitMatrix = null;

    CameraPreviewCallbackAbstract(Context context, Camera.Parameters parameters) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement camera view Listener");
        }

        this.context = context;

        finderPatternColor = Color.parseColor("#f02cb673"); //same as res/values/colors/spring_green

        possibleCenters = new ArrayList<>();

        previewSize = parameters.getPreviewSize();
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (camera == null)
            return;

        //get EV to use in order to avoid over exposure while trying to optimise brightness
        float step = camera.getParameters().getExposureCompensationStep();
        EV = step * camera.getParameters().getExposureCompensation();
    }

    protected abstract void sendData(byte[] data);

    int[] qualityChecks(byte[] data, FinderPatternInfo info) {
        lumList.clear();
        float[] tilts = null;
        int lumVal = 0;
        int shadVal = 0;
        int levVal = 0;

        try {
            if (possibleCenters != null) {
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

                    //brightness: add lum. values to list
                    addLumToList(src_gray, lumList);
                }
            } else {
                //if no finder patterns are found, remove one from track
                //when device e.g. is put down, slowly the value becomes zero
                //'slowly' being about a second
                if (possibleCenters.size() == 0) {
                    if (lumTrack.size() > 0) {
                        lumTrack.removeFirst();
                    }
                    if (shadowTrack.size() > 0) {
                        shadowTrack.removeFirst();
                    }
                }
            }

            // number of finder patterns can be anything here.
            if (info != null) {
                //DETECT BRIGHTNESS
                double maxLuminance = luminosityCheck(lumList);
                lumVal = maxLuminance > Constant.MAX_LUM_LOWER && maxLuminance <= Constant.MAX_LUM_UPPER ? 1 : 0;

                // DETECT SHADOWS
                if (bgr != null && possibleCenters.size() == 4) {
                    double shadowPercentage = detectShadows(info, bgr);
                    shadVal = shadowPercentage < Constant.MAX_SHADOW_PERCENTAGE ? 1 : 0;
                }

                // Get Tilt
                if (possibleCenters.size() == 4) {
                    tilts = PreviewUtils.getTilt(info);
                    // The tilt in both directions should not exceed Constant.MAX_TILT_DIFF
                    levVal = Math.abs(tilts[0] - 1) < Constant.MAX_TILT_DIFF && Math.abs(tilts[1] - 1) < Constant.MAX_TILT_DIFF ? 1 : 0;
                }
            }

            //UPDATE VALUES IN ARRAY
            qualityChecksArray[0] = lumVal;
            qualityChecksArray[1] = shadVal;
            qualityChecksArray[2] = levVal;

            //SHOW VALUES ON SCREEN
            if (listener != null) {
                //brightness: show the values on device
                if (lumTrack.size() < 1) {
                    //-1 means 'no data'
                    listener.showBrightness(-1);
                } else {
                    listener.showBrightness(lumTrack.getLast());
                }

                //shadows: show the values on device
                if (shadowTrack.size() < 1) {
                    //101 means 'no data'
                    listener.showShadow(101);
                } else {
                    listener.showShadow(shadowTrack.getLast());
                }

                //level: show on device
                listener.showLevel(tilts);
            }
        } catch (Exception e) {
            // throw new RuntimeException(e);
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
            bgr = OpenCVUtils.perspectiveTransform(tl, tr, bl, br, bgr).clone();

            try {
                if (calibrationData != null) {
                    shadowPercentage = PreviewUtils.getShadowPercentage(bgr, calibrationData);
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

    private void addLumToList(Mat src_gray, List<double[]> lumList) {
        double[] lumMinMax;

        lumMinMax = PreviewUtils.getDiffLuminosity(src_gray);
        if (lumMinMax.length == 2) {
            lumList.add(lumMinMax);
        }
    }

    private double luminosityCheck(List<double[]> lumList) {
        double maxLuminance = -1; //highest value of 'white'

        for (int i = 0; i < lumList.size(); i++) {
            //store lum max value that corresponds with highest: we use it to check over- and under exposure
            if (lumList.get(i)[1] > maxLuminance) {
                maxLuminance = lumList.get(i)[1];
            }
        }
        //fill the linked list up to 25 items; meant to stabilise the view, keep it from flickering.
        if (lumTrack.size() > 25) {
            lumTrack.removeFirst();
        }

        if (lumList.size() > 0) {
            //add highest value of 'white' to track list
            lumTrack.addLast(100 * maxLuminance / 255);

            //compensate for under-exposure
            //if max values lower than 150
            if (maxLuminance < Constant.MAX_LUM_LOWER) {
                //The maximum is below the minimum value. Increase the exposure value
                listener.adjustExposureCompensation(1);
                return maxLuminance;
            }

            //compensate for over-exposure
            //if max values larger than 254
            if (maxLuminance > Constant.MAX_LUM_UPPER) {
                // the maximum is larger than the maximum value. We are likely overexposed
                // adjust exposure downwards.
                listener.adjustExposureCompensation(-1);
            } else {
                //we want to get it as bright as possible but without risking overexposure
                if (maxLuminance * Constant.PERCENT_ILLUMINATION < Constant.MAX_LUM_UPPER) {
                    // try to increase the exposure one more time
                    listener.adjustExposureCompensation(1);
                } else {
                    //optimum situation reached
                    System.out.println("*** optimum exposure reached.  EV = " +
                            EV);
                }
            }
        }
        return maxLuminance;
    }

    FinderPatternInfo findPossibleCenters(byte[] data, final Camera.Size size) {
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

        if (bitMatrix != null) {
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

                if (possibleCenters != null && previewSize != null) {
                    if (listener != null) {
                        listener.showFinderPatterns(possibleCenters, previewSize, finderPatternColor);
                    }
                    //get the version number from the barcode printed on the card
                    try {
                        if (possibleCenters.size() == 4) {
                            int versionNumber = CalibrationCard.decodeCalibrationCardCode(possibleCenters, bitMatrix);
                            if (versionNumber != CalibrationCard.CODE_NOT_FOUND) {
                                CalibrationCard.addVersionNumber(versionNumber);
                                calibrationData = CalibrationCard.readCalibrationFile(context);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ignored) {
                // this only means not all patterns (=4) are detected.
            }
        }
        return info;
    }
}
