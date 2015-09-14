package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.opencv.ShadowDetector;
import org.akvo.akvoqr.util.AssetsManager;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DetectStripTimeLapseActivity extends AppCompatActivity {

    private Mat bgr;
    private Bitmap bitmap;
    private LinearLayout linearLayout;
    private Handler handler;
    private List<ProgressStep> steps = new ArrayList<>();

    private void showImage(final Bitmap bitmap) {

        Runnable showImageRunnable = new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(DetectStripTimeLapseActivity.this);
                imageView.setImageBitmap(bitmap);
                linearLayout.addView(imageView);
            }

        };
        handler.post(showImageRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_strip_timelapse);

        linearLayout = (LinearLayout) findViewById(R.id.activity_detect_strip_timelapseLinearLayout);
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

    public Mat getCalibratedImage(Mat mat)
    {
        CalibrationCard calibrationCard = new CalibrationCard();
        return calibrationCard.calibrateImage(this, mat);

    }

    private class DetectStripTask extends AsyncTask<Intent,Void,Void>
    {
        Intent intent;
        Intent resultIntent;

        protected void  onPreExecute()
        {
            resultIntent = new Intent(DetectStripTimeLapseActivity.this, ResultActivity.class);
        }
        @Override
        protected Void doInBackground(Intent... params) {

            System.out.println("***start detect strip task");

            intent = params[0];

            String brandname = intent.getStringExtra(Constant.BRAND);
            int numPatches = StripTest.getInstance().getBrand(brandname).getPatches().size();
            for (int i = 0; i < numPatches; i++) {

                try {

                    byte[] data = FileStorage.readByteArray(i);

                    int format = intent.getIntExtra(Constant.FORMAT, ImageFormat.NV21);
                    int width = intent.getIntExtra(Constant.WIDTH, 0);
                    int height = intent.getIntExtra(Constant.HEIGHT, 0);

                    String jsonInfo = FileStorage.readFinderPatternInfoJson(i);
                    if(jsonInfo==null) {
                        continue;
                    }

                    JSONObject jsonObject = new JSONObject(jsonInfo);
                    JSONArray tl = jsonObject.getJSONArray("topleft");
                    JSONArray tr = jsonObject.getJSONArray("topright");
                    JSONArray bl = jsonObject.getJSONArray("bottomleft");
                    JSONArray br = jsonObject.getJSONArray("bottomright");
                    double[] topleft = new double[]{tl.getDouble(0), tl.getDouble(1)};
                    double[] topright = new double[]{tr.getDouble(0), tr.getDouble(1)};
                    double[] bottomleft = new double[]{bl.getDouble(0), bl.getDouble(1)};
                    double[] bottomright = new double[]{br.getDouble(0), br.getDouble(1)};

                    System.out.println("***data DetectStrip: " + data.length);

                    if (format == ImageFormat.NV21) {

                        //convert preview data to Mat object with highest possible quality
                        bgr = new Mat(height, width, CvType.CV_8UC3);
                        Mat convert_mYuv = new Mat(height + height / 2, width, CvType.CV_8UC1);
                        convert_mYuv.put(0, 0, data);
                        Imgproc.cvtColor(convert_mYuv, bgr, Imgproc.COLOR_YUV2BGR_NV21, bgr.channels());

                        YuvImage yuvImage = new YuvImage(data, format, width, height, null);
                        Rect rect = new Rect(0, 0, width, height);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(rect, 100, baos);
                        byte[] jData = baos.toByteArray();

                        // bitmap = BitmapFactory.decodeByteArray(jData, 0, jData.length);

                    } else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565) {

                        Mat bgra = new Mat(height, width, CvType.CV_8UC4);
                        bgr = new Mat();
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        Utils.bitmapToMat(bitmap, bgra);
                        System.out.println("***bgra_dst type I: " + CvType.typeToString(bgra.type()) + ", channels: " + bgra.channels());

                        Imgproc.cvtColor(bgra, bgr, Imgproc.COLOR_BGRA2BGR);

                        System.out.println("***bgr_dst type II: " + CvType.typeToString(bgr.type()) + ", channels: " + bgr.channels());

                    }

                    //perspectiveTransform
                    Mat warp_dst = OpenCVUtils.perspectiveTransform(topleft, topright, bottomleft, bottomright, bgr);

                    Mat striparea = null;
                    Mat calarea = null;

                    //detect strip
                    double ratioW = 1;
                    double ratioH = 1;
                    org.opencv.core.Rect roiStriparea = null;
                    org.opencv.core.Rect roiCalarea = null;
                    String json = AssetsManager.getInstance().loadJSONFromAsset("calibration.json");
                    if (json != null) {

                        try {
                            double hsize = 1;
                            double vsize = 1;
                            JSONObject object = new JSONObject(json);
                            if (!object.isNull("calData")) {
                                JSONObject calData = object.getJSONObject("calData");
                                hsize = calData.getDouble("hsize");
                                vsize = calData.getDouble("vsize");
                            }
                            if (!object.isNull("stripAreaData")) {
                                JSONObject stripAreaData = object.getJSONObject("stripAreaData");
                                if (!stripAreaData.isNull("area")) {
                                    JSONArray area = stripAreaData.getJSONArray("area");
                                    if (area.length() == 4) {

                                        ratioW = warp_dst.width() / hsize;
                                        ratioH = warp_dst.height() / vsize;
                                        Point stripTopLeft = new Point(area.getDouble(0) * ratioW + 2,
                                                area.getDouble(1) * ratioH + 2);
                                        Point stripBottomRight = new Point(area.getDouble(2) * ratioW - 2,
                                                area.getDouble(3) * ratioH - 2);

                                        //striparea rect
                                        roiStriparea = new org.opencv.core.Rect(stripTopLeft, stripBottomRight);

                                        //calarea rect
                                        roiCalarea = new org.opencv.core.Rect(new Point(0, 0), new Point(warp_dst.width(), area.getDouble(1) * ratioH));

                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //detect shadows
                    if (warp_dst != null) {

                        if (roiCalarea != null)
                            calarea = warp_dst.submat(roiCalarea);

                        ShadowDetector.detectShadows(calarea);
                    }

                    //find calibration patches
                    Mat dest;
                    try {
                        //Calibration code works with 8UC3 images only.
                        // System.out.println("***warp_dst type: " + CvType.typeToString(warp_dst.type()) + ", channels: " + warp_dst.channels());
                        dest = getCalibratedImage(warp_dst);

                    } catch (Exception e) {
                        // System.out.println("*** calibration failed");
                        dest = warp_dst.clone();

                    }

                    if (roiStriparea != null)
                        striparea = dest.submat(roiStriparea);

                    resultIntent.putExtra(Constant.BRAND, brandname);

                    if (striparea != null) {

                        Mat rgba = new Mat();
                        Imgproc.cvtColor(striparea, rgba, Imgproc.COLOR_BGR2RGBA);
                        Bitmap bitmap = Bitmap.createBitmap(striparea.width(), striparea.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(rgba, bitmap);
                        showImage(bitmap);

                        StripTest stripTestBrand = StripTest.getInstance();
                        StripTest.Brand brand = stripTestBrand.getBrand(brandname);

                        Mat strip = OpenCVUtils.detectStrip(striparea, brand, ratioW, ratioH);

                        if (strip != null) {

                            resultIntent.putExtra(Constant.BRAND, brandname);
                            resultIntent.putExtra(Constant.MAT, strip);

                        } else {

                            //draw a red cross over the image
                            Imgproc.line(rgba, new Point(0, 0), new Point(rgba.cols(),
                                    rgba.rows()), new Scalar(255, 0, 0, 255), 2);
                            Imgproc.line(rgba, new Point(0, rgba.rows()), new Point(rgba.cols(),
                                    0), new Scalar(255, 0, 0, 255), 2);

                            resultIntent.putExtra(Constant.MAT, rgba);

                        }

                        // startActivity(resultIntent);
                    } else {

                    }





                }catch(Exception e){
                    e.printStackTrace();

                }
            }
            return null;
        }


    }



    private class ProgressStep
    {
        int id;
        boolean success;
        String text;

        public ProgressStep(int id, String text){
            this.id = id;
            this.text = text;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }


}
