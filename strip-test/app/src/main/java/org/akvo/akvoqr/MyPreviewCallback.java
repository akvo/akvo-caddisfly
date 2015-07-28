package org.akvo.akvoqr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.calibration.Patch;
import org.akvo.akvoqr.detector.BinaryBitmap;
import org.akvo.akvoqr.detector.BitMatrix;
import org.akvo.akvoqr.detector.FinderPattern;
import org.akvo.akvoqr.detector.FinderPatternFinder;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.HybridBinarizer;
import org.akvo.akvoqr.detector.NotFoundException;
import org.akvo.akvoqr.detector.PlanarYUVLuminanceSource;
import org.akvo.akvoqr.detector.ResultPoint;
import org.akvo.akvoqr.detector.ResultPointCallback;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 6/26/15.
 */
public class MyPreviewCallback implements Camera.PreviewCallback {

    static boolean firstTime = true;
    FinderPatternFinder finderPatternFinder;
    List<ResultPoint> resultPoints = new ArrayList<>();
    FinderPatternInfo info;
    List<FinderPattern> possibleCenters;
    CameraViewListener listener;
    Camera camera;
    private static boolean isRunning = false;
    private boolean focused = false;
    private boolean allOK = false;
    private byte[] previewData;
    Bitmap bitmap;
    private Handler handler;
    private Runnable runAtListener = new Runnable() {
        @Override
        public void run() {
            if (listener != null) {

                if(allOK) {

                    listener.setBitmap(bitmap);
                }
                else
                    listener.getMessage(0);
            }
        }
    };

    public static MyPreviewCallback getInstance(Context context) {

        return new MyPreviewCallback(context);

    }

    private MyPreviewCallback(Context context) {
        try {
            listener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement cameraviewhandler");
        }

        handler = new Handler();
    }

    final ResultPointCallback resultPointCallback = new ResultPointCallback() {
        @Override
        public void foundPossibleResultPoint(ResultPoint point) {
            resultPoints.add(point);

        }
    };

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {

        this.camera = camera;

        //System.out.println("***is Running: " + isRunning);
        if (!isRunning) {
            isRunning = true;
            allOK = false;
            focused = false;
            possibleCenters = null;

            new BitmapTask().execute(data);
        }

    }

    public FinderPatternInfo getInfo() {
        return info;
    }

    public List<FinderPattern> getPossibleCenters() {
        return possibleCenters;
    }

    private class BitmapTask extends AsyncTask<byte[], Void, Void> {

        @Override
        public void onCancelled()
        {
            //if(!isRunning)
            cancel(false);
            super.onCancelled();
        }
        @Override
        protected Void doInBackground(byte[]... params) {


            byte[] data = params[0];

            makeBitmap(data);

            return null;
        }

        protected void onPostExecute(Void result) {
            isRunning = false;
            handler.post(runAtListener);
        }

    }

    private void makeBitmap(byte[] data) {

        focused = false;
        try {

            //TODO CHECK EXPOSURE

            //TODO CHECK SHADOWS



            findPossibleCenters(data);


            if (possibleCenters != null && possibleCenters.size() > 3) {

                //if patterns are found, focus camera and return
                //this is a workaround to give camera time to adjust exposure
                //we assume that second time is immediately after first time, so patterns are found while the camera is
                //focused correctly

                if (firstTime){
                    System.out.println("*** focussing!!!!!!!");
                    while (!focused){
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) focused = true;
                         }
                        });
                    }
                    firstTime = false;
                    return;
                }

                for (FinderPattern pattern : possibleCenters) {
                    System.out.println("***pattern estimated module size: " + pattern.getEstimatedModuleSize());
                }

                int pheight =  camera.getParameters().getPreviewSize().height;
                int pwidth = camera.getParameters().getPreviewSize().width;

                //convert preview data to Mat object with highest possible quality
                Mat mbgra = new Mat(pheight, pwidth, CvType.CV_64FC4);
                Mat convert_mYuv = new Mat(pheight + pheight / 2, pwidth, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, mbgra, Imgproc.COLOR_YUV2RGBA_NV21, mbgra.channels());

                System.out.println("***bgra mbgra w, h: " + mbgra.width() + " , " + mbgra.height() + CvType.typeToString(mbgra.type()) + " mbgra :" + mbgra.toString());


                //noOfModules: constant that holds the time estimated module size is to be multiplied by, used to 'cut out' image not showing finder patterns
                //typical value is 3.5 as pattern is 1:1:3:1:1 which amounts to seven.
                final float noOfModules = 0f;
                final float adjustTL = noOfModules*possibleCenters.get(0).getEstimatedModuleSize();
                final float adjustTR = noOfModules*possibleCenters.get(1).getEstimatedModuleSize();
                final float adjustBL = noOfModules*possibleCenters.get(2).getEstimatedModuleSize();
                final float adjustBR = noOfModules*possibleCenters.get(3).getEstimatedModuleSize();

                List<Point> srcList = new ArrayList<>();

                //coordinates for the rect (the finder pattern centers)
                srcList.add(new Point(getInfo().getTopLeft().getX(),
                        getInfo().getTopLeft().getY()));
                srcList.add(new Point(getInfo().getTopRight().getX(),
                        getInfo().getTopRight().getY()));
                srcList.add(new Point(getInfo().getBottomLeft().getX(),
                        getInfo().getBottomLeft().getY()));
                srcList.add(new Point(getInfo().getBottomRight().getX(),
                        getInfo().getBottomRight().getY()));

                System.out.println("***before sort:");
                System.out.println("***topleft: " + srcList.get(0).x + " ," + srcList.get(0).y);
                System.out.println("***topright: " + srcList.get(1).x + " ," + srcList.get(1).y);
                System.out.println("***bottomleft: " + srcList.get(2).x + " ," + srcList.get(2).y);
                System.out.println("***bottomright: " + srcList.get(3).x + ", " + srcList.get(3).y);

                //Sort the arraylist of finder patterns based on a comparison of the sum of x and y values. Lowest values come first,
                // so the result will be: top-left, bottom-left, top-right, bottom-right. Because top-left always has the lowest sum of x and y
                // and bottom-right always the highest
                Collections.sort(srcList, new PointComparator());

                System.out.println("***after sort:");
                System.out.println("***topleft: " + srcList.get(0).x +" ,"+ srcList.get(0).y);
                System.out.println("***bottomleft: " + srcList.get(1).x +" ,"+ srcList.get(1).y);
                System.out.println("***topright: " + srcList.get(2).x +" ,"+ srcList.get(2).y);
                System.out.println("***bottomright: "+ srcList.get(3).x + ", "+ srcList.get(3).y);

                //source quad
                //here we maintain the order: top-left, top-right, bottom-left, bottom-right
                Point[] srcQuad = new Point[4];
                srcQuad[0]=srcList.get(0);
                srcQuad[1]=srcList.get(2);
                srcQuad[2]=srcList.get(1);
                srcQuad[3]=srcList.get(3);
                //destination quad corresponding with srcQuad
                Point[] dstQuad = new Point[4];
                dstQuad[0] = new Point( 0,0 );
                dstQuad[1] = new Point( mbgra.cols() - 1, 0 );
                dstQuad[2] = new Point( 0, mbgra.rows() - 1 );
                dstQuad[3] = new Point(mbgra.cols()-1, mbgra.rows()-1);

                //srcQuad and destQuad to MatOfPoint2f objects, needed in perspective transform
                MatOfPoint2f srcMat2f = new MatOfPoint2f(srcQuad);
                MatOfPoint2f dstMat2f = new MatOfPoint2f(dstQuad);

                //make a destination mat for a warp
                Mat warp_dst = Mat.zeros(mbgra.rows(), mbgra.cols(), mbgra.type());

                //get a perspective transform matrix
                Mat warp_mat = Imgproc.getPerspectiveTransform(srcMat2f, dstMat2f);

                //do the warp
                Imgproc.warpPerspective(mbgra, warp_dst,warp_mat, warp_dst.size());

                //enhance contrast
//                Mat equalsrc = new Mat();
//                List<Mat> channels = new ArrayList<>();
//                Imgproc.cvtColor(warp_dst, equalsrc, Imgproc.COLOR_RGB2YCrCb);
//                Core.split(equalsrc, channels);
//                Imgproc.equalizeHist(channels.get(0), channels.get(0));
//                Core.merge(channels, equalsrc);
//                Imgproc.cvtColor(equalsrc, dest, Imgproc.COLOR_YCrCb2RGB);
//
//                //sharpen image
//                Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3,3));
//                Imgproc.filter2D(warp_dst, dest, -1, kernel);
//                Imgproc.GaussianBlur(warp_dst, dest, new Size(3, 3), 0, 0);
//                Core.addWeighted(warp_dst, 1.5, dest, -0.5, 0, dest);


                CalibrationCard calibrationCard = new CalibrationCard();
                Patch[] patches = calibrationCard.measurePatches(warp_dst);

                ResultActivity.colors.clear();

                for(Patch patch: patches) {
                    Point point1 = new Point(patch.x - patch.d/2, patch.y - patch.d/2);
                    Point point2 = new Point(patch.x + patch.d/2, patch.y + patch.d/2);
                    Core.rectangle(warp_dst, point1, point2, new Scalar(255, 0, 0, 255));

                    int color = Color.rgb((int)patch.red,(int) patch.green,(int) patch.blue);
                    ResultActivity.colors.add(new ResultActivity.ColorDetected(color, patch.x));
                }
                //detect strip colors
                Mat dest = warp_dst.clone();
                Rect rect = new Rect(0, (int)Math.round(dest.height()*0.66), dest.width(), (int)Math.round(dest.height() * 0.33));
                Mat striparea = dest.submat(rect);


                //OpenCVUtils.detectColor(striparea, ResultActivity.colors);

                //convert to 8bits 4 channels necessary to make bitmap
                dest.convertTo(dest, CvType.CV_8UC4);

                //create a bitmap with the same size and color config as the dest
                bitmap = Bitmap.createBitmap(warp_dst.width(), warp_dst.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dest, bitmap);


                allOK = true;

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            allOK = false;
        }
    }

    public void findPossibleCenters(byte[] data) {
        if (camera != null) {
            final Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            PlanarYUVLuminanceSource myYUV = new PlanarYUVLuminanceSource(data, size.width,
                    size.height, 0, 0,
                    size.width,
                    size.height, false);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(myYUV));

            BitMatrix bitMatrix = null;
            try {
                bitMatrix = binaryBitmap.getBlackMatrix();
            } catch (NotFoundException e) {
                e.printStackTrace();

            } catch (NullPointerException e) {
                e.printStackTrace();

            }

            if (bitMatrix != null) {
                finderPatternFinder = new FinderPatternFinder(bitMatrix, resultPointCallback);

                try {

                    if (possibleCenters != null)
                        possibleCenters = null;

                    info = finderPatternFinder.find(null);
                    possibleCenters = finderPatternFinder.getPossibleCenters();

                } catch (Exception e) {
                    // ignore. this only means no patterns are detected.
                }
            }
        }
    }

    public static class PointComparator implements Comparator<Point>
    {


        @Override
        public int compare(Point lhs, Point rhs) {

          if(lhs.x + lhs.y < rhs.x + rhs.y)
          {
             return -1;
          }

          else
          {
              return 1;
          }

        }
    }


}




