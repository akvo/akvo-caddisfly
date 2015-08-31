package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import org.akvo.akvoqr.opencv.StripTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ResultActivity extends AppCompatActivity {

    public static List<ColorDetected> colors = new ArrayList<>();
    public static List<ColorDetected> stripColors = new ArrayList<>();
    private Map<String, JSONArray> ppmValues;
    private JSONArray ppms;
    private boolean useLab = true;
    private boolean useRGB = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        ArrayList<Mat> mats = (ArrayList<Mat>) intent.getSerializableExtra("mats");

        if(mats!=null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);
            for (int i=0;i<mats.size();i++)
            {
                try {
                    Mat mat = mats.get(i);
                    Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat, bitmap);

                    double max = bitmap.getHeight()>bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
                    double min = bitmap.getHeight()<bitmap.getWidth()? bitmap.getHeight(): bitmap.getWidth();
                    double ratio = (double) min / (double) max;
                    int width = (int) Math.max(800, max);
                    int height = (int) Math.round(ratio * width);

                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(bitmap);

                    layout.addView(imageView);

                } catch (Exception e) {
                    e.printStackTrace();
                }
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

            //The colors of calibration card
//            if(colors.size()>0)
//            {
//                GridView colorLayout = (GridView) findViewById(R.id.colors);
//
//                Collections.sort(colors, new mColorComparator());
//                MyAdapter adapter = new MyAdapter(this, 0, colors);
//                colorLayout.setAdapter(adapter);
//
//            }

            /* end obsolete code */

            //The colors of the strip test
            // stripColors arraylist is filled in class OpenCVUtils, when detecting the color of the patches
            // detectStripColorBrandKnown(Mat src, StripTest.Brand brand)
            if(stripColors.size()>0)
            {
                GridView colorLayout1 = (GridView) findViewById(R.id.stripcolors);

                //sort the arraylist on x-position
                Collections.sort(stripColors, new mColorComparator());

                /**
                 * TODO put values in strips.json in assets
                 * After we decide if quality of image used to gather colors is sufficient
                 */
                StripTest stripTestBrand = StripTest.getInstance();
                StripTest.Brand brand = stripTestBrand.getBrand(StripTest.brand.HACH883738);
                ArrayList<ColorDetected> colors = stripTestBrand.getPPMColorsFromImage();

                ppmValues = brand.getPpmValues();

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

                //for Lab
                double[] pointA = null;
                double[] pointB = null;
                double[] pointC = null;
                //for rgb
                double[] pointAr = null;
                double[] pointBr = null;
                double[] pointCr = null;

                double labda = 0;
                double labdar = 0;
                boolean matchFound = false;

                for(int j=0;j<stripColors.size();j++) {

                    // make pointC array
                    if(useLab && stripColors.get(j).getLab()!=null) {

                        System.out.println("***stripColors has Lab");
                        pointC = stripColors.get(j).getLab().val;
                        System.out.println("***Lab: " + pointC[0] + ", " + pointC[1] + ", "+ pointC[2]);

                    }
                    // make pointCr arry
                   /* else */ if(useRGB && stripColors.get(j).getRgb()!=null)
                    {
                        System.out.println("***stripColors has RGB");
                        pointCr = stripColors.get(j).getRgb().val;

                        System.out.println("***RGB: " + pointCr[0] + ", " + pointCr[1] + ", "+ pointCr[2]);
                    }
                    else if(stripColors.get(j).getColor()!=0)
                    {
                        // if for some reason there is no lab or rgb info, but ColorDetected has an integer (Android Color)
                        // we use that to get rgb values
                        int CL = stripColors.get(j).getColor();
                        int CLred = Color.red(CL);
                        int CLgreen = Color.green(CL);
                        int CLblue = Color.blue(CL);
                        pointC = new double[]{CLred, CLgreen, CLblue};

                    }

                    // make points A and B
                    for (int i = 0; i < colors.size() - 1; i++) {

                        if(useLab && colors.get(i).getLab()!=null)
                        {
                            pointA = colors.get(i).getLab().val;
                            pointB = colors.get(i+1).getLab().val;

                        }
                        /*else */ if(useRGB )
                        {
                            if(colors.get(i).getRgb()!=null)
                                pointAr = colors.get(i).getRgb().val;
                            if(colors.get(i+1).getRgb()!=null)
                                pointBr = colors.get(i+1).getRgb().val;
                        }
                        else if(colors.get(i).getColor()!=0) {


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

                            double ppm = 0;
                            //Lab
                            if(pointA!=null && pointB!=null & pointC!=null) {

                                labda = getClosestPointOnLine(pointA, pointB, pointC);

                                String ppmDesc = brand.getPatchDescList().get(j);
                                ppms = ppmValues.get(ppmDesc);
                                ppm = (1 - labda) * ppms.getDouble(i) + labda * ppms.getDouble(i+1);

                                // if labda is between 0 and 1, it is valid. Maybe we should break the loop here.
                                // we set matchFound to true, so if the loop ends with matchFound = false,
                                // we can do something with that info (extrapolate)
                                if(0 < labda && labda < 1) {

                                    stripColors.get(j).setPpm(ppm);
                                    matchFound = true;
                                }
                                System.out.println("***LAB*** color patch no: " + i + "  labda: " + labda + " ppm: " + ppm);

                            }

                            //RGB
                            if(pointAr!=null && pointBr!=null & pointCr!=null) {

                                labdar = getClosestPointOnLine(pointAr, pointBr, pointCr);

                                String ppmDesc = brand.getPatchDescList().get(j);
                                ppms = ppmValues.get(ppmDesc);
                                ppm = (1 - labdar) * ppms.getDouble(i) + labdar * ppms.getDouble(i+1);

                                if(0 < labda && labda < 1) {

                                    /* We chose te display the ppm for Lab in the above, ignore for rgb */
                                    //stripColors.get(j).setPpm(ppm);
                                    // matchFound = true;
                                }
                                System.out.println("***RGB*** color patch no: " + i + "  labdarr: " + labdar + " ppm: " + ppm);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(!matchFound && labda>1) //we have a very dark color. extrapolate.
                    {

                        try
                        {
                            double ppm = (1 - labda) * ppms.getDouble(ppms.length()-2) + labda * ppms.getDouble(ppms.length() -1);
                            stripColors.get(j).setPpm(ppm);

                            System.out.println("***NO MATCH FOUND*** strip patch no: " + j + "  labda: " + labda + " ppm: " + ppm);
                            System.out.println("***SETTING LAB VALUE FOR PPM: " + ppm);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }

                MyAdapter adapter1 = new MyAdapter(this, 0, stripColors);
                colorLayout1.setAdapter(adapter1);

            }
        }
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
                    textView.setText("< " + ppms.getDouble(0));
                else if (objects.get(position).getPpm() == -Double.MAX_VALUE)
                    textView.setText("> " + ppms.getDouble(ppms.length() - 1));
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
