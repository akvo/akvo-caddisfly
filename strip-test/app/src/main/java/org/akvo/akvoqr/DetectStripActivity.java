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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.akvoqr.calibration.CalibrationCard;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.opencv.ShadowDetector;
import org.akvo.akvoqr.util.AssetsManager;
import org.akvo.akvoqr.util.Constant;
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

import static org.akvo.akvoqr.opencv.OpenCVUtils.getOrderedPoints;

public class DetectStripActivity extends AppCompatActivity {

    private Mat bgr;
    private Bitmap bitmap;
    private TextView textView0;
    private TextView textView1;
    private TextView textView2;
    private ImageView imageView0;
    private ImageView imageView1;
    private ImageView imageView2;
    private Handler handler;
    private List<ProgressStep> steps = new ArrayList<>();
    private ArrayList<Mat> mats = new ArrayList<>();

    private void setText(final int step) {
        Runnable setProgRunnable = new Runnable() {
            @Override
            public void run() {

                String message = steps.get(step).success? "OK": "failure";
                switch (step){
                    case 0:
                        textView0.append(message);
                        break;
                    case 1:
                        textView1.append(message);
                        break;
                    case 2:
                        textView2.append(message);
                        break;
                }
            }
        };
        handler.post(setProgRunnable);
    }
    private void showImage(final int view, final Bitmap bitmap) {

        Runnable showImageRunnable = new Runnable() {
            @Override
            public void run() {
                switch (view) {
                    case 0:
                        imageView0.setImageBitmap(bitmap);
                        break;
                    case 1:
                        imageView1.setImageBitmap(bitmap);
                        break;
                    case 2:
                        imageView2.setImageBitmap(bitmap);
                        break;
                }
            }
        };
        handler.post(showImageRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_strip);

        steps.add(new ProgressStep(0, "Shadow detection"));
        steps.add(new ProgressStep(1, "Calibrating"));
        steps.add(new ProgressStep(2, "Strip area"));

        textView0 = (TextView) findViewById(R.id.activity_detect_stripTextView0);
        textView1 = (TextView) findViewById(R.id.activity_detect_stripTextView1);
        textView2 = (TextView) findViewById(R.id.activity_detect_stripTextView2);
        imageView0 = (ImageView) findViewById(R.id.activity_detect_stripImageView0);
        imageView1 = (ImageView) findViewById(R.id.activity_detect_stripImageView1);
        imageView2 = (ImageView) findViewById(R.id.activity_detect_stripImageView2);
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

    // the points are in the order top left, bottom left, top right, bottom right
    private int getCode(Mat warp_dst, double mSize, List<Point> points){
        double pixelWidthBetweenPatterns = Math.sqrt(Math.pow(points.get(1).x - points.get(3).x, 2) + Math.pow(points.get(1).y - points.get(3).y, 2));
        int iWidth = warp_dst.cols();
        int iHeight = warp_dst.rows();
        double newMSize = mSize * iWidth / pixelWidthBetweenPatterns;
        System.out.println("***new module size: " + newMSize);

        return 1;
    }

    private class DetectStripTask extends AsyncTask<Intent,Void,Void>
    {
        Intent intent;
        Intent resultIntent;

        protected void  onPreExecute()
        {
            resultIntent = new Intent(DetectStripActivity.this, ResultActivity.class);
        }
        @Override
        protected Void doInBackground(Intent... params) {

            System.out.println("***start detect strip task");

            intent = params[0];
            try {
                String brandname = intent.getStringExtra(Constant.BRAND);
                byte[] data = intent.getByteArrayExtra(Constant.DATA);
                int format = intent.getIntExtra(Constant.FORMAT, ImageFormat.NV21);
                int width = intent.getIntExtra(Constant.WIDTH, 0);
                int height = intent.getIntExtra(Constant.HEIGHT, 0);
                double mSize = intent.getDoubleExtra(Constant.MODULE_SIZE,0);
                Bundle finderPatternBundle = intent.getBundleExtra(Constant.FINDERPATTERNBUNDLE);
                double[] topleft = finderPatternBundle.getDoubleArray(Constant.TOPLEFT);
                double[] topright = finderPatternBundle.getDoubleArray(Constant.TOPRIGHT);
                double[] bottomleft = finderPatternBundle.getDoubleArray(Constant.BOTTOMLEFT);
                double[] bottomright = finderPatternBundle.getDoubleArray(Constant.BOTTOMRIGHT);

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

                int code = getCode(warp_dst, mSize, getOrderedPoints(topleft, topright, bottomleft, bottomright));

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
                                    roiCalarea = new org.opencv.core.Rect(new Point(0,0), new Point(warp_dst.width(), area.getDouble(1)*ratioH));

                                }
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

                //detect shadows
                if(warp_dst != null)
                {

                    if(roiCalarea!=null)
                        calarea = warp_dst.submat(roiCalarea);

                    ShadowDetector.detectShadows(calarea);

                    //show bitmap with shadow contour in image view
                    //Imgproc.cvtColor(calarea, calarea, Imgproc.COLOR_BGR2RGBA);
                    Bitmap bitmap = Bitmap.createBitmap(calarea.width(), calarea.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(calarea, bitmap);
                    showImage(0, bitmap);

                    publishProgress(0, true);

                }
                else
                {
                    publishProgress(0, false);
                }

                //find calibration patches
                Mat dest;
                try {
                    //Calibration code works with 8UC3 images only.
                    System.out.println("***warp_dst type: " + CvType.typeToString(warp_dst.type()) + ", channels: " + warp_dst.channels());
                    dest = getCalibratedImage(warp_dst);
                    publishProgress(1, true);
                }
                catch (Exception e)
                {
                    System.out.println("*** calibration failed");
                    dest = warp_dst.clone();
                    publishProgress(1, false);
                }

                Mat rgba = new Mat();
                Imgproc.cvtColor(dest, rgba, Imgproc.COLOR_BGR2RGBA);
                Bitmap bitmap1 = Bitmap.createBitmap(dest.width(), dest.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(rgba, bitmap1);
                showImage(1, bitmap1);

                if(roiStriparea!=null)
                    striparea = dest.submat(roiStriparea);

                resultIntent.putExtra(Constant.BRAND, brandname);

                if (striparea != null) {

                    //show bitmap in image view
                    Imgproc.cvtColor(striparea, rgba, Imgproc.COLOR_BGR2RGBA);
                    Bitmap bitmap2 = Bitmap.createBitmap(striparea.width(), striparea.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(rgba, bitmap2);
                    showImage(2, bitmap2);

                    publishProgress(2, true);

                    StripTest stripTestBrand = StripTest.getInstance();
                    StripTest.Brand brand = stripTestBrand.getBrand(brandname);

                    Mat strip = OpenCVUtils.detectStrip(striparea, brand, ratioW, ratioH);

                    if (strip != null) {

//                    Imgproc.cvtColor(strip, strip, Imgproc.COLOR_BGR2RGBA);
                        mats.add(strip);

                    } else {

                        Imgproc.cvtColor(striparea, striparea, Imgproc.COLOR_BGR2RGBA);
                        //draw a red cross over the image
                        Imgproc.line(striparea, new Point(0, 0), new Point(striparea.cols(),
                                striparea.rows()), new Scalar(255, 0, 0, 255), 2);
                        Imgproc.line(striparea, new Point(0, striparea.rows()), new Point(striparea.cols(),
                                0), new Scalar(255, 0, 0, 255), 2);

                        mats.add(striparea);

                    }
                    bitmap2.recycle();

                }
                else{
                    publishProgress(2, false);
                }

                bitmap1.recycle();

                resultIntent.putExtra(Constant.BRAND, brandname);

                resultIntent.putExtra(Constant.MAT, mats);

            } catch (Exception e) {
                e.printStackTrace();

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {

            Button toResultButton = (Button) findViewById(R.id.activity_detect_stripButtonResult);
            toResultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(resultIntent);
                }
            });
        }

        private void publishProgress(int id, boolean success)
        {
            steps.get(id).setSuccess(success);
            setText(id);
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
