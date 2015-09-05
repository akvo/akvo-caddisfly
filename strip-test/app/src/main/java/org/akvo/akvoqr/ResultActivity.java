package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.akvoqr.color.ColorDetected;
import org.akvo.akvoqr.color.mColorComparator;
import org.akvo.akvoqr.opencv.OpenCVUtils;
import org.akvo.akvoqr.opencv.StripTest;
import org.akvo.akvoqr.ui.CircleView;
import org.akvo.akvoqr.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ResultActivity extends AppCompatActivity {

    private JSONArray ppmValues;
    private boolean useLab = true;
    private boolean useRGB = true;
    private String brandName;
    private StripTest.Brand brand;
    private Mat strip;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        ArrayList<Mat> mats = (ArrayList<Mat>) intent.getSerializableExtra(Constant.MAT);
        brandName = intent.getStringExtra(Constant.BRAND);

        if(mats!=null) {
            Mat mat = mats.get(0);
            layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

            /**
             * TODO put values in strips.json in assets
             * After we decide if quality of image used to gather colors is sufficient
             */
            StripTest stripTestBrand = StripTest.getInstance();
            brand = stripTestBrand.getBrand(brandName);

            List<StripTest.Brand.Patch> patches = brand.getPatches();
            int matH = mat.height();
            double ratioW = mat.width() / brand.getStripLenght();
            Core.copyMakeBorder(mat, mat, 20, 20, 0, 0, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

            for(int i=0;i<patches.size();i++) {

                //show the name of the patch
                String desc = patches.get(i).getDesc();

                //calculate center of patch in pixels
                double x = patches.get(i).getPosition() * ratioW;
                double y = mat.height() / 2;
                Point centerPatch = new Point(x,y);

                //Draw a green circle around each patch and make a bitmap of the whole
                strip = mat.clone();
                Imgproc.circle(strip, centerPatch, (int) Math.ceil(matH * 0.8),
                        new Scalar(0, 255, 0, 255), 2);

                new BitmapTask(desc).execute(strip);

                //make a submat around each center of the patch and get mean color
                int minRow =(int)Math.round(Math.max(centerPatch.y - 7, 0));
                int maxRow = (int)Math.round(Math.min(centerPatch.y + 7, mat.height()));
                int minCol = (int)Math.round(Math.max(centerPatch.x - 7, 0));
                int maxCol = (int)Math.round(Math.min(centerPatch.x + 7, mat.width()));

                Mat submat = mat.submat(minRow, maxRow,
                        minCol, maxCol);

                //set the ppmValues needed to calculate ppm
                ppmValues = patches.get(i).getPpmValues();
                
                new ColorDetectedTask().execute(submat);

            }

            /*start obsolete code */
            /* code that was used when Intent to start this Activity had a byte[] in Extra's
             * to pass the image.
             * now we put a Serializable Mat object in Extra's.
             * I keep it here in case that does not work well and we need to fall back on Android standards.
             */
//            try {
//                if (format == ImageFormat.NV21) {
//
//                    YuvImage yuvImage = new YuvImage(data, format, width, height, null);
//                    Rect rect = new Rect(0, 0, width, height);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    yuvImage.compressToJpeg(rect, 100, baos);
//                    byte[] jData = baos.toByteArray();
//
//                    bitmap = BitmapFactory.decodeByteArray(jData, 0, jData.length);
//                } else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565) {
//
//                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            }

            /* end obsolete code */

        }
    }

    private Bitmap makeBitmap(Mat mat)
    {
        try {

            Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bitmap);

            double max = bitmap.getHeight()>bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
            double min = bitmap.getHeight()<bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
            double ratio = (double) min / (double) max;
            int width = (int) Math.max(400, max);
            int height = (int) Math.round(ratio * width);

            return Bitmap.createScaledBitmap(bitmap, width, height, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private class BitmapTask extends AsyncTask<Mat, Void, Void>
    {
        private Bitmap stripBitmap;
        private String desc;

        public BitmapTask(String desc)
        {
            this.desc = desc;
        }
        @Override
        protected Void doInBackground(Mat... params) {

            Mat strip = params[0];
            stripBitmap = makeBitmap(strip);

            return null;
        }

        protected void onPostExecute(Void result)
        {
            TextView descView = new TextView(ResultActivity.this);
            descView.setText(desc);
            layout.addView(descView);

            ImageView imageView = new ImageView(ResultActivity.this);
            imageView.setImageBitmap(stripBitmap);
            layout.addView(imageView);
        }
    }

    private class ColorDetectedTask extends AsyncTask<Mat, Void, Void>
    {
        private ColorDetected colorDetected;
        private double ppm;

        @Override
        protected Void doInBackground(Mat... params) {

            Mat patch = params[0];

            colorDetected = OpenCVUtils.detectStripColorBrandKnown(patch);
            ArrayList<ColorDetected> colors = OpenCVUtils.getPPMColorsFromImage();

            ppm = calculatePPMLab(colorDetected, colors);

            return null;
        }

        protected void onPostExecute(Void result)
        {
            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            LinearLayout result_ppm_layout = (LinearLayout) inflater.inflate(R.layout.result_ppm_layout, null, false);
            CircleView circleView = (CircleView) result_ppm_layout.findViewById(R.id.result_ppm_layoutCircleView);
            circleView.circleView(colorDetected.getColor());

            TextView textView = (TextView) result_ppm_layout.findViewById(R.id.result_ppm_layoutPPMtextView);
            textView.setText(String.format("%.2f", ppm) + " ppm.");
            layout.addView(result_ppm_layout);
        }
    }
    private double calculatePPMLab(ColorDetected colorDetected, List<ColorDetected> colors)
    {

        double ppm;
        // The colors from the strip test brand, sort on x position (left to right)
        // this assumes that the darkest color is the one on the right;
        // that is important to do something with labda and matchFound after setting them
        // in the for-loop that follows
        Collections.sort(colors, new mColorComparator());

        //logging
        for(int h=0;h<colors.size();h++)
        {
            double[] val =colors.get(h).getRgb().val;
            System.out.println("***color hach rgb: " +  val[0] + ", " + val[1] + ", " + val[2]);
        }

        double[] pointA = null;
        double[] pointB = null;
        double[] pointC = null;
        double labda = 0;

        boolean matchFound = false;

        // make pointC array
        if(colorDetected.getLab()!=null) {

            System.out.println("***stripColors has Lab");
            pointC = colorDetected.getLab().val;
            System.out.println("***Lab: " + pointC[0] + ", " + pointC[1] + ", "+ pointC[2]);

        }
        else if(colorDetected.getColor()!=0)
        {
            // if for some reason there is no lab info, but ColorDetected has an integer (Android Color)
            // we use that to get rgb values
            int CL = colorDetected.getColor();
            int CLred = Color.red(CL);
            int CLgreen = Color.green(CL);
            int CLblue = Color.blue(CL);
            pointC = new double[]{CLred, CLgreen, CLblue};
        }

        // make points A and B
        for (int j = 0; j < colors.size() - 1; j++) {

            if(useLab && colors.get(j).getLab()!=null)
            {
                pointA = colors.get(j).getLab().val;
                pointB = colors.get(j+1).getLab().val;

            }
            else if(colors.get(j).getColor()!=0) {

                int CA = colors.get(0).getColor();
                int CB = colors.get(colors.size() - 1).getColor();

                int CAred = Color.red(CA);
                int CAgreen = Color.green(CA);
                int CAblue = Color.blue(CA);
                int CBred = Color.red(CB);
                int CBgreen = Color.green(CB);
                int CBblue = Color.blue(CB);

                pointA = new double[]{CAred, CAgreen, CAblue};
                pointB = new double[]{CBred, CBgreen, CBblue};

            }

            try {

                //Lab
                if(pointA!=null && pointB!=null & pointC!=null) {

                    labda = getClosestPointOnLine(pointA, pointB, pointC);

                    ppm = (1 - labda) * ppmValues.getDouble(j) + labda * ppmValues.getDouble(j+1);

                    // if labda is between 0 and 1, it is valid. Maybe we should not break the loop here.
                    // we set matchFound to true, so if the loop ends with matchFound = false,
                    // we can do something with that info (extrapolate)
                    if(0 < labda && labda < 1) {

                        matchFound = true;
                        return ppm;
                    }
                    System.out.println("***LAB*** color patch no: " + j + "  labda: " + labda + " ppm: " + ppm);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(!matchFound && labda>1) //we have a very dark color. extrapolate.
        {

            try
            {
                ppm = (1 - labda) * ppmValues.getDouble(ppmValues.length()-2) + labda * ppmValues.getDouble(ppmValues.length() -1);
                return ppm;

//                System.out.println("***NO MATCH FOUND*** strip patch no: "  + "  labda: " + labda + " ppm: " + ppm);
//                System.out.println("***SETTING LAB VALUE FOR PPM: " + ppm);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }

        return 0;
    }



    /*
    //    dus: labda = 	 (A - L) . (B - A)
    //                  -------------------
    //                      | B - A |^2
    //
    //    En dit wordt: labda = ((Ar - Lr)(Br - Ar) + (Ag - Lg)(Bg - Ag) + (Ab - Lb)(Bb - Ab))   /  ((Br - Ar)^2 + (Bg - Ag)^2 + (Bb - Ab)^2)
    //        Als ik twee vectoren heb: (x1,y1,z1)en (x2,y2,z2), dan is het dot product:
    //
    // x1*x2 + y1*y2 + z1*z2
    */
    private double getClosestPointOnLine(double[] pointA, double[] pointB, double[] pointC) throws Exception {


        if(pointA.length<3 || pointB.length<3 || pointC.length<3)
            throw new Exception("array lengths should all be 3");

        double pxA = pointA[0];
        double pyA = pointA[1];
        double pzA = pointA[2];
        double pxB = pointB[0];
        double pyB = pointB[1];
        double pzB = pointB[2];
        double pxC = pointC[0];
        double pyC = pointC[1];
        double pzC = pointC[2];

        double numerator = (pxA - pxC) * (pxB - pxA) + (pyA - pyC) * (pyB - pyA) + (pzA - pzC) * (pzB - pzA);
        double denominator = Math.pow((pxB - pxA), 2) + Math.pow(pyB - pyA, 2) + Math.pow(pzB - pzA, 2);

        double t = - numerator / denominator;

        return t;

    }

    private double getLabda(int CA, int CB, int CC)
    {

        // relation between three colors and labda. Labda is a percentage of the interval between color CA and CB.
        // e.g. if CA is at 40% and CB is at 100% of a range from 0 to 100 and we want to interpolate the color at 70%,
        // we get labda as: (70 - 40)/(100 - 40) = 0.5
        //C(λ) = (1 - λ)CA + λCB, where 0 ≤ λ ≤ 1.​
        // http://howaboutanorange.com/blog/2011/08/10/color_interpolation/


        //We know CA, CB and CL. We want to find labda.
        //If we know labda, we can calculate a ppm value given the array of values corresponding to the
        //CA and CB colors
        //CL = (1 - x) * CA + x*CB
        //(1 - x) * CA + x*CB = CL
        //CA - x*CA + x*CB = CL
        //   - x*CA + x*CB = CL - CA
        //   -CA + CB = (CL - CA) / x
        // x = (CL – CA) / ( - CA + CB)

        //labda = (CL - CA) / (CB - CA )

        System.out.println("***CA: " + CA + "  CB: " + CB + "  CL: " + CC);

        int CAred = Color.red(CA);
        int CAgreen = Color.green(CA);
        int CAblue = Color.blue(CA);
        int CBred = Color.red(CB);
        int CBgreen = Color.green(CB);
        int CBblue = Color.blue(CB);
        int CLred = Color.red(CC);
        int CLgreen = Color.green(CC);
        int CLblue = Color.blue(CC);

        double labdaRed = ((double) CLred - (double) CAred) / ((double) CBred - (double) CAred);
        System.out.println("***  labdared: " + labdaRed );
        double labdaGreen = ((double) CLgreen - (double) CAgreen) / ((double) CBgreen - (double) CAgreen);
        System.out.println("*** labdagreen: " + labdaGreen );
        double labdaBlue = ((double) CLblue - (double) CAblue) / ((double) CBblue - (double) CAblue);
        System.out.println("***  labdablue: " + labdaBlue );

        System.out.println("*** labdasum: " + (labdaRed + labdaGreen + labdaBlue) );
        System.out.println("*** labdaavg: " + ((labdaRed + labdaGreen + labdaBlue)/2) );

        double labda = ((double) CC - (double) CA) / ((double) CB - (double) CA);
        System.out.println("***  labdaTest: " + labda );
//        double ppm = ppmValues[i] + (ppmValues[i + 1] - ppmValues[i]) * labda;
//         double ppm = ppmValues[ppmValues.length-1] * labda;

        return labda;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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

    private class MyAdapter extends ArrayAdapter<ColorDetected> {

        List<ColorDetected> objects;
        Context mContext;

        public MyAdapter(Context context, int resource, List<ColorDetected> objects) {
            super(context, resource, objects);

            this.objects = objects;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                textView = new TextView(mContext);
                textView.setLayoutParams(new GridView.LayoutParams(55, 55));
                textView.setPadding(1, 1, 1, 1);
                textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            } else {
                textView = (TextView) convertView;
            }

            textView.setBackgroundColor(objects.get(position).getColor());

            try {
                if (objects.get(position).getPpm() == Double.MAX_VALUE)
                    textView.setText("< " + ppmValues.getDouble(0));
                else if (objects.get(position).getPpm() == -Double.MAX_VALUE)
                    textView.setText("> " + ppmValues.getDouble(ppmValues.length() - 1));
                else
                    textView.setText(String.format("%.2f", objects.get(position).getPpm()));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return textView;
        }
    }
}
