package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.akvoqr.choose_striptest.ChooseStriptestListActivity;
import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.color.ColorDetected;
import org.akvo.akvoqr.opencv.OpenCVUtils;
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
import java.util.List;


public class ResultActivity extends AppCompatActivity {

    private String brandName;
    private StripTest.Brand brand;
    private ArrayList<Mat> mats;
    private Mat strip;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        mats = (ArrayList<Mat>) intent.getSerializableExtra(Constant.MAT);
        brandName = intent.getStringExtra(Constant.BRAND);

        if(mats!=null && mats.size()>0) {

            layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

            StripTest stripTestBrand = StripTest.getInstance();
            brand = stripTestBrand.getBrand(brandName);

            List<StripTest.Brand.Patch> patches = brand.getPatches();

            for(int i=0;i<patches.size();i++) {

                //the name of the patch
                String desc = patches.get(i).getDesc();

                if (brand.hasTimeLapse()) {
                    if (i < mats.size()) {
                        strip = mats.get(i).clone();
                    } else {
                        continue;
                    }
                } else strip = mats.get(mats.size() - 1).clone();

                int matH = strip.height();

                if(matH > 1) {
                    double ratioW = strip.width() / brand.getStripLenght();

                    Mat mat = new Mat();
                    Core.copyMakeBorder(strip, mat, 20, 20, 0, 0, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

                    //calculate center of patch in pixels
                    double x = patches.get(i).getPosition() * ratioW;
                    double y = mat.height() / 2;
                    Point centerPatch = new Point(x, y);

                    //Draw a green circle around each patch and make a bitmap of the whole
                    Imgproc.circle(mat, centerPatch, (int) Math.ceil(matH * 0.8),
                            new Scalar(0, 255, 0, 255), 2);
                    new BitmapTask(desc).execute(mat);

                    //make a submat around center of the patch and get mean color
                    int minRow = (int) Math.round(Math.max(centerPatch.y - 7, 0));
                    int maxRow = (int) Math.round(Math.min(centerPatch.y + 7, mat.height()));
                    int minCol = (int) Math.round(Math.max(centerPatch.x - 7, 0));
                    int maxCol = (int) Math.round(Math.min(centerPatch.x + 7, mat.width()));

                    Mat submat = mat.submat(minRow, maxRow,
                            minCol, maxCol);

                    //set the colours needed to calculate ppm
                    JSONArray colours = patches.get(i).getColours();
                    String unit = patches.get(i).getUnit();

                    new ColorDetectedTask(unit, colours).execute(submat);
                }
                else
                {
                    new BitmapTask(desc).execute(strip);
                }


                Button save = (Button) findViewById(R.id.activity_resultButtonSave);
                Button redo = (Button) findViewById(R.id.activity_resultButtonRedo);

                //TODO onclicklistener for save button

                redo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentRedo = new Intent(ResultActivity.this, ChooseStriptestListActivity.class);
                        intentRedo.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intentRedo);
                        ResultActivity.this.finish();
                    }
                });

            }
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
        private Bitmap stripBitmap = null;
        private String desc;

        public BitmapTask(String desc)
        {
            this.desc = desc;
        }
        @Override
        protected Void doInBackground(Mat... params) {

            Mat strip = params[0];
            if(strip.rows()>1 && strip.cols()>1)
                stripBitmap = makeBitmap(strip);

            return null;
        }

        protected void onPostExecute(Void result)
        {
            TextView descView = new TextView(ResultActivity.this);
            descView.setText(desc);
            layout.addView(descView);

            if(stripBitmap!=null) {
                ImageView imageView = new ImageView(ResultActivity.this);
                imageView.setImageBitmap(stripBitmap);
                layout.addView(imageView);
            }
            else
            {
                TextView nodataView = new TextView(ResultActivity.this);
                nodataView.setText("no data");
                nodataView.setBackgroundResource(R.drawable.background_white_black_line_bottom);
                nodataView.setPadding(0,0,0,12);
                layout.addView(nodataView);
            }
        }
    }

    private class ColorDetectedTask extends AsyncTask<Mat, Void, Void> {
        private ColorDetected colorDetected;
        private Mat patch;
        private double ppm;
        private String unit;
        private JSONArray colours;

        public ColorDetectedTask(String unit, JSONArray colours) {
            this.unit = unit;
            this.colours = colours;
        }


        @Override
        protected Void doInBackground(Mat... params) {

            patch = params[0];

            colorDetected = OpenCVUtils.detectStripColorBrandKnown(patch);

            ppm = calculatePPMrgb(colorDetected);

            return null;
        }

        protected void onPostExecute(Void result) {

            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            LinearLayout result_ppm_layout = (LinearLayout) inflater.inflate(R.layout.result_ppm_layout, null, false);
            CircleView circleView = (CircleView) result_ppm_layout.findViewById(R.id.result_ppm_layoutCircleView);
            circleView.circleView(colorDetected.getColor());

            TextView textView = (TextView) result_ppm_layout.findViewById(R.id.result_ppm_layoutPPMtextView);
            textView.setText(String.format("%.2f", ppm) + " " + unit);
            layout.addView(result_ppm_layout);

        }

        private double calculatePPMrgb(ColorDetected colorDetected) {

            List<Pair<Integer,Double>> labdaList = new ArrayList<>();
            double ppm = 0;
            double[] pointA = null;
            double[] pointB = null;
            double[] pointC = null;
            double labda;

            // make pointC array
            if (colorDetected.getRgb() != null) {

                System.out.println("***stripColors has RGB");
                pointC = colorDetected.getRgb().val;
                //System.out.println("***RGB C : " + pointC[0] + ", " + pointC[1] + ", " + pointC[2]);

            }

            JSONArray rgb;
            for (int j = 0; j < colours.length() - 1; j++) {

                // make points A and B
                try
                {
                    rgb = colours.getJSONObject(j).getJSONArray("rgb");
                    pointA = new double[]{rgb.getDouble(0), rgb.getDouble(1), rgb.getDouble(2)};

                    rgb = colours.getJSONObject(j + 1).getJSONArray("rgb");
                    pointB = new double[]{rgb.getDouble(0), rgb.getDouble(1), rgb.getDouble(2)};

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (pointA != null && pointB != null & pointC != null)
                    {
                        labda = getClosestPointOnLine(pointA, pointB, pointC);
                        labdaList.add(new Pair(j, labda));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //get labda closest to 0.5
            double smallestL = Double.MAX_VALUE;
            int firstColorPos = 0;
            for(Pair p: labdaList)
            {
                double l = Math.abs((double)p.second);

                if( Math.abs(l - 0.5) < smallestL)
                {
                    smallestL = Math.abs(l - 0.5) ;
                    firstColorPos = (int) p.first;
                }

                //System.out.println("***labda from list: " + p.second + " smallestL: " + smallestL);
            }

            labda = labdaList.get(firstColorPos).second;

            try
            {
                ppm = (1 - labda) * colours.getJSONObject(firstColorPos).getDouble("value") + labda * colours.getJSONObject(firstColorPos + 1).getDouble("value");
                //System.out.println("***RGB*** color patch no: " + firstColorPos + "  labda: " + labda + " ppm: " + ppm);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            // if labda is between 0 and 1, it is valid.
            if (0 < labda && labda < 1)
            {
                return ppm;
            }

            if (labda > 1) //we have a very dark color. extrapolate.
            {
                try
                {
                    ppm = (1 - labda) * colours.getJSONObject(colours.length() - 2).getDouble("value") +
                            labda * colours.getJSONObject(colours.length() - 1).getDouble("value");

//                System.out.println("***NO MATCH FOUND*** strip patch no: "  + "  labda: " + labda + " ppm: " + ppm);
//                System.out.println("***SETTING VALUE FOR PPM: " + ppm);

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            return ppm;
        }
    }



    /*
    //   labda = 	 (A - L) . (B - A)
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

}
