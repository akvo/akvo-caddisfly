package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.calibration.CalibrationData;
import org.akvo.akvoqr.calibration.CalibrationResultData;
import org.akvo.akvoqr.choose_striptest.ChooseStriptestListActivity;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;

public class DetectStripActivity extends AppCompatActivity {

    private Mat labImg;
    private LinearLayout linearLayout;
    private Handler handler;
    private Button toResultsButton;
    private Button redoTestButton;
    private ScrollView scrollView;

    private void showImage(final Bitmap bitmap) {

        Runnable showImageRunnable = new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(DetectStripActivity.this);
                imageView.setImageBitmap(bitmap);
                linearLayout.addView(imageView);
            }

        };
        handler.post(showImageRunnable);
    }
    private void showMessage(final String message) {

        Runnable showMessageRunnable = new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(DetectStripActivity.this);
                textView.setText(message);
                linearLayout.addView(textView);

                View lastView = scrollView.getChildAt(scrollView.getChildCount()-1);
                scrollView.smoothScrollTo(0, lastView.getBottom());

                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

        };
        handler.postDelayed(showMessageRunnable, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_strip_timelapse);

        linearLayout = (LinearLayout) findViewById(R.id.activity_detect_strip_timelapseLinearLayout);
        toResultsButton = (Button) findViewById(R.id.activity_detect_strip_timelapseButtonToResults);
        redoTestButton = (Button) findViewById(R.id.activity_detect_strip_timelapseButtonRedo);
        scrollView = (ScrollView) findViewById(R.id.activity_detect_strip_timelapseScrollView);
        handler = new Handler();

        new DetectStripTask().execute(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detect_strip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public CalibrationResultData getCalibratedImage(Mat mat) throws Exception
    {
        //System.out.println("***version number detect: " + CalibrationCard.getMostFrequentVersionNumber());

        CalibrationCard calibrationCard = CalibrationCard.getInstance();
        if(CalibrationCard.getMostFrequentVersionNumber() == CalibrationCard.CODE_NOT_FOUND)
        {
            throw new Exception("no version number set.");
        }
        return calibrationCard.calibrateImage(mat);

    }

    private class DetectStripTask extends AsyncTask<Intent,Void,Void> {
        Intent intent;
        Intent resultIntent;
        ArrayList<Mat> resultList = new ArrayList<>();
        byte[] data;
        int format;
        int width;
        int height;
        double ratioW = 1;
        double ratioH = 1;
        org.opencv.core.Rect roiStriparea = null;
        org.opencv.core.Rect roiCalarea = null;
        Mat warp_dst;
        Mat cal_dest;
        Mat striparea = null;
        Mat calarea = null;
        private boolean develop = true;

        protected void onPreExecute() {
            resultIntent = new Intent(DetectStripActivity.this, ResultActivity.class);
        }

        @Override
        protected Void doInBackground(Intent... params) {

            intent = params[0];

            String brandname = intent.getStringExtra(Constant.BRAND);
            resultIntent.putExtra(Constant.BRAND, brandname);

            int numPatches = StripTest.getInstance().getBrand(brandname).getPatches().size();

            format = intent.getIntExtra(Constant.FORMAT, ImageFormat.NV21);
            width = intent.getIntExtra(Constant.WIDTH, 0);
            height = intent.getIntExtra(Constant.HEIGHT, 0);

            if (width == 0 || height == 0) {

                return null;
            }

            JSONArray imagePatchArray = null;
            int imageCount = -1;
            // Mat for detected strip
            Mat labStrip = new Mat();

            try {
                String json = FileStorage.readFromInternalStorage(Constant.IMAGE_PATCH+".txt");
                imagePatchArray = new JSONArray(json);
                System.out.println("***imagePatchArray: " + imagePatchArray.toString(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < numPatches; i++) {
                try {
                    if (imagePatchArray != null) {
                        // sub-array for each patch
                        JSONArray array = imagePatchArray.getJSONArray(i);

                        // get the image number from the json array
                        int imageNo = array.getInt(0);

                        if (imageNo > imageCount) {

                            // Set imageCount to current number
                            imageCount = imageNo;

                            showMessage(getString(R.string.reading_data));
                            data = FileStorage.readByteArray(imageNo);
                            if (data == null)
                                throw new IOException();
                            //make a L,A,B Mat object from data
                            try {
                                makeLab();
                            } catch (Exception e) {
                                showMessage(getString(R.string.error_conversion));
                                continue;
                            }

                            //perspectiveTransform
                            try {
                                warp(imageNo);
                            } catch (Exception e) {
                                showMessage(getString(R.string.error_warp));
                                continue;
                            }

                            //divide into calibration and stripareas
                            try {
                                divideIntoCalibrationAndStripArea();
                            } catch (Exception e) {
                                showMessage(getString(R.string.error_detection));
                                continue;
                            }

                            // save warped image to external storage
                            if (develop) {
                                Mat rgb = new Mat();
                                Imgproc.cvtColor(warp_dst, rgb, Imgproc.COLOR_Lab2RGB);
                                Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(rgb, bitmap);

                                if (FileStorage.checkExternalMedia()) {
                                    FileStorage.writeToSDFile(bitmap);
                                }
                                Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                                showImage(bitmap);
                            }

                            //calibrate
                            try {
                                showMessage(getString(R.string.calibrating));
                                CalibrationResultData calResult = getCalibratedImage(warp_dst);
                                cal_dest = calResult.calibratedImage;
                                showMessage("E94 mean: " + String.format("%.2f", calResult.meanE94) + ", max: " + String.format("%.2f", calResult.maxE94));
                            } catch (Exception e) {
                                System.out.println("cal. failed: " + e.getMessage());
                                e.printStackTrace();
                                showMessage(getString(R.string.error_calibrating));
                                cal_dest = warp_dst.clone();
                            }

                            //show calibrated image
                            if (develop) {
                                Mat rgb = new Mat();
                                Imgproc.cvtColor(cal_dest, rgb, Imgproc.COLOR_Lab2RGB);
                                Bitmap bitmap = Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(rgb, bitmap);
                                Bitmap.createScaledBitmap(bitmap, 800, 480, false);
                                showImage(bitmap);
                            }

                            if (roiStriparea != null)
                                striparea = cal_dest.submat(roiStriparea);

                            if (striparea != null) {
                                showMessage(getString(R.string.cut_out_strip));

                                StripTest stripTestBrand = StripTest.getInstance();
                                StripTest.Brand brand = stripTestBrand.getBrand(brandname);

                                Mat strip = OpenCVUtils.detectStrip(striparea, brand, ratioW, ratioH);

                                if (strip != null) {
                                    labStrip = strip.clone();
                                    //Imgproc.cvtColor(strip, labStrip, Imgproc.COLOR_Lab2RGB);
                                } else {
                                    showMessage(getString(R.string.error_cut_out_strip));
                                    labStrip = striparea.clone();
                                    //Imgproc.cvtColor(striparea, labStrip, Imgproc.COLOR_Lab2RGB);

                                    //draw a red cross over the image
                                    Imgproc.line(labStrip, new Point(0, 0), new Point(labStrip.cols(),
                                            labStrip.rows()), new Scalar(255, 0, 0, 255), 2);
                                    Imgproc.line(labStrip, new Point(0, labStrip.rows()), new Point(labStrip.cols(),
                                            0), new Scalar(255, 0, 0, 255), 2);
                                }
                            }
                        }
                        resultList.add(labStrip);
                    }
                } catch (Exception e) {
                    showMessage(getString(R.string.error_unknown));
                    //place a Mat object in result list. This is necessary for ResultActivity to work
                    //because we are counting patches, not mats
                    Mat mat = Mat.zeros(1, 1, CvType.CV_8UC4);
                    resultList.add(mat);
                    continue;
                }
            }
            showMessage("\n\n" + getString(R.string.finished));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            //hack to make the scrollview scroll
            showMessage("\n");

            resultIntent.putExtra(Constant.MAT, resultList);

            redoTestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DetectStripActivity.this, ChooseStriptestListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });

            toResultsButton.setBackgroundColor(getResources().getColor(R.color.skyblue));
            toResultsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(resultIntent);
                    //DetectStripActivity.this.finish();
                }
            });
        }

        private void makeLab() throws Exception
        {
            if (format == ImageFormat.NV21) {
                //convert preview data to Mat object in CIELAB format
                Mat bgr = new Mat(height, width, CvType.CV_8UC3);
                labImg = new Mat(height, width, CvType.CV_8UC3);
                Mat convert_mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
                convert_mYuv.put(0, 0, data);
                Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2RGB_NV21, bgr.channels());
                Imgproc.cvtColor(bgr, labImg, Imgproc.COLOR_RGB2Lab, bgr.channels());
            }
//            else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565) {
//
//                Mat bgra = new Mat(height, width, CvType.CV_8UC4);
//                //System.out.println("***bgra type I: " + CvType.typeToString(bgra.type()) + ", channels: " + bgra.channels());
//
//                bgr = new Mat();
//                Bitmap bitmap;
//
//                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                Utils.bitmapToMat(bitmap, bgra);
//
//                Imgproc.cvtColor(bgra, bgr, Imgproc.COLOR_BGRA2BGR);
//                //System.out.println("***bgr type II: " + CvType.typeToString(bgr.type()) + ", channels: " + bgr.channels());
//
//                if(develop) {
//                    Bitmap scaledbitmap = Bitmap.createScaledBitmap(bitmap, 800, 480, false);
//                    showImage(scaledbitmap);
//                }
//            }
        }

        private void warp(int i) throws Exception
        {
            if(labImg == null)
            {
                throw new Exception("no image");
            }

            String jsonInfo = FileStorage.readFromInternalStorage(Constant.INFO + i + ".txt");
            if (jsonInfo == null) {
                showMessage(getString(R.string.error_no_finder_pattern_info));
                throw new Exception("no finder pattern info");
            }

            JSONObject jsonObject = new JSONObject(jsonInfo);
            JSONArray tl = jsonObject.getJSONArray(Constant.TOPLEFT);
            JSONArray tr = jsonObject.getJSONArray(Constant.TOPRIGHT);
            JSONArray bl = jsonObject.getJSONArray(Constant.BOTTOMLEFT);
            JSONArray br = jsonObject.getJSONArray(Constant.BOTTOMRIGHT);
            double[] topleft = new double[]{tl.getDouble(0), tl.getDouble(1)};
            double[] topright = new double[]{tr.getDouble(0), tr.getDouble(1)};
            double[] bottomleft = new double[]{bl.getDouble(0), bl.getDouble(1)};
            double[] bottomright = new double[]{br.getDouble(0), br.getDouble(1)};

            showMessage(getString(R.string.warp));
            warp_dst = OpenCVUtils.perspectiveTransform(topleft, topright, bottomleft, bottomright, labImg);
        }

        private void divideIntoCalibrationAndStripArea() throws Exception{

            CalibrationData data = CalibrationCard.getCalData();

            if (warp_dst!=null && data != null) {

                double hsize = data.hsize;
                double vsize = data.vsize;
                double[] area = data.stripArea;

                if (area.length == 4) {

                    ratioW = warp_dst.width() / hsize;
                    ratioH = warp_dst.height() / vsize;
                    Point stripTopLeft = new Point(area[0] * ratioW + 2,
                            area[1] * ratioH + 2);
                    Point stripBottomRight = new Point(area[2] * ratioW - 2,
                            area[3] * ratioH - 2);

                    //striparea rect
                    roiStriparea = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);

                    //calarea rect
                    roiCalarea = new org.opencv.core.Rect(new Point(0, 0),
                            new Point(warp_dst.width(), area[1] * ratioH));

                }
            }
        }
    }
}
