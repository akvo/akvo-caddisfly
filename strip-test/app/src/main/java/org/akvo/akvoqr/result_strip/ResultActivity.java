package org.akvo.akvoqr.result_strip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.akvoqr.ColorimetryStripActivity;
import org.akvo.akvoqr.R;
import org.akvo.akvoqr.colorimetry_strip.StripTest;
import org.akvo.akvoqr.ui.CircleView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.akvo.akvoqr.util.OpenCVUtils;
import org.akvo.akvoqr.util.calibration.CalibrationCard;
import org.akvo.akvoqr.util.color.ColorDetected;
import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;


public class ResultActivity extends AppCompatActivity {


//    private static int countInstance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

//        countInstance++;
//        System.out.println("***ResultActivity countInstance: " + countInstance);

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            final FileStorage fileStorage = new FileStorage(this);
            String brandName = intent.getStringExtra(Constant.BRAND);

            Mat strip;
            StripTest stripTest = new StripTest();
            StripTest.Brand brand = stripTest.getBrand(this, brandName);

            List<StripTest.Brand.Patch> patches = brand.getPatches();

            JSONArray imagePatchArray = null;
            try {
                String json = fileStorage.readFromInternalStorage(Constant.IMAGE_PATCH + ".txt");
                if (json != null) {
                    imagePatchArray = new JSONArray(json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (imagePatchArray != null) {
                for (int i = 0; i < patches.size(); i++) {

                    //the name of the patch
                    String desc = patches.get(i).getDesc();
                    try {
                        System.out.println("***imagePatchArray: " + imagePatchArray.toString(1));
                        JSONArray array = imagePatchArray.getJSONArray(i);
                        // get the image number from the json array
                        int imageNo = array.getInt(0);

                        //if in DetectStripTask, no strip was found, an image was saved with the String Constant.ERROR
                        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

                        String error = isInvalidStrip? Constant.ERROR: "";

                        byte[] data = fileStorage.readByteArray(Constant.STRIP + imageNo + error);
                        if (data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                            strip = new Mat();
                            Utils.bitmapToMat(bitmap, strip);

                            double ratioW = strip.width() / brand.getStripLenght();

                            //calculate center of patch in pixels
                            double x = patches.get(i).getPosition() * ratioW;
                            double y = strip.height() / 2;
                            Point centerPatch = new Point(x, y);

                            //set the colours needed to calculate ppm
                            JSONArray colours = patches.get(i).getColours();
                            String unit = patches.get(i).getUnit();

                            new BitmapTask(isInvalidStrip, desc, centerPatch, colours, unit).execute(strip);

                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                        //TESTING
                        new BitmapTask(true, null, new Point(1,1),null, null).execute(new Mat());

                        continue;
                    }
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("no data");
                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

                layout.addView(textView);

                //TESTING
                new BitmapTask(true, null, new Point(1,1),null, null).execute(new Mat());

            }


            Button save = (Button) findViewById(R.id.activity_resultButtonSave);
            Button redo = (Button) findViewById(R.id.activity_resultButtonRedo);

            //TODO onclicklistener for save button
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), R.string.thank_using_caddisfly, Toast.LENGTH_SHORT).show();
                }
            });

            redo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (fileStorage != null) {
                        fileStorage.deleteFromInternalStorage(Constant.INFO);
                        fileStorage.deleteFromInternalStorage(Constant.DATA);
                        fileStorage.deleteFromInternalStorage(Constant.STRIP);
                    }

                    Intent intentRedo = new Intent(ResultActivity.this, ColorimetryStripActivity.class);

                    startActivity(intentRedo);
                    ResultActivity.this.finish();
                }
            });


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
        private boolean invalid;
        private Bitmap stripBitmap = null;
        private String desc;
        private Point centerPatch;
        private JSONArray colours;
        private String unit;
        private ColorDetected colorDetected;
        private double ppm = -1;

        public BitmapTask(boolean invalid, String desc, Point centerPatch, JSONArray colours, String unit)
        {
            this.invalid = invalid;
            this.desc = desc;
            this.centerPatch = centerPatch;
            this.colours = colours;
            this.unit = unit;
        }

        @Override
        protected Void doInBackground(Mat... params) {

            Mat mat = params[0];

            if(mat.empty()) {
                return null;
            }

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2Lab);
            int submatSize = 7;
            if(mat.height()<submatSize)
                return null;

            if (!invalid) {
                //make a submat around center of the patch and get mean color
                int minRow = (int) Math.round(Math.max(centerPatch.y - submatSize, 0));
                int maxRow = (int) Math.round(Math.min(centerPatch.y + submatSize, mat.height()));
                int minCol = (int) Math.round(Math.max(centerPatch.x - submatSize, 0));
                int maxCol = (int) Math.round(Math.min(centerPatch.x + submatSize, mat.width()));

                Mat patch = mat.submat(minRow, maxRow,
                        minCol, maxCol);

                colorDetected = OpenCVUtils.detectStripColorBrandKnown(patch);

                double[] colorValueLab = colorDetected.getLab().val;
                //String colorSchema = "lab"; //must correspond with name of property in strips.json
                try {
                    ppm = calculatePPM(colorValueLab, colours);
                } catch (Exception e) {
                    e.printStackTrace();
                    ppm = Double.NaN;
                }

                //done with lab shema, make rgb to show in imageview
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);

                //extend the strip with a border, so we can draw a circle around each patch that is
                //wider than the strip itself. That is just because it looks nice.
                int borderSize = (int) Math.ceil(mat.height() * 0.5);

                Core.copyMakeBorder(mat, mat, borderSize, borderSize, 0, 0, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

                //Draw a green circle around each patch
                Imgproc.circle(mat, new Point(centerPatch.x, mat.height() / 2), (int) Math.ceil(mat.height() * 0.4),
                        new Scalar(0, 255, 0, 255), 2);
            }
            else
            {
                //done with lab shema, make rgb to show in imageview
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
            }

            if(!mat.empty()) {
                stripBitmap = makeBitmap(mat);
            }

            return null;
        }

        protected void onPostExecute(Void result)
        {
            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            LinearLayout result_ppm_layout = (LinearLayout) inflater.inflate(R.layout.result_ppm_layout, null, false);

            TextView descView = (TextView) result_ppm_layout.findViewById(R.id.result_ppm_layoutDescView);
            descView.setText(desc);

            if(stripBitmap!=null) {
                ImageView imageView = (ImageView) result_ppm_layout.findViewById(R.id.result_ppm_layoutImageView);
                imageView.setImageBitmap(stripBitmap);

                if(!invalid) {
                    if(colorDetected!=null) {
                        CircleView circleView = (CircleView) result_ppm_layout.findViewById(R.id.result_ppm_layoutCircleView);
                        circleView.circleView(colorDetected.getColor());
                    }

                    TextView textView = (TextView) result_ppm_layout.findViewById(R.id.result_ppm_layoutPPMtextView);
                    if (ppm > -1) {
                        if (ppm < 1.0) {
                            textView.setText(String.format("%.2f", ppm) + " " + unit);
                        } else {
                            textView.setText(String.format("%.1f", ppm) + " " + unit);
                        }
                    }
                }
            }
            else
            {
                descView.append("\n\n" + getResources().getString(R.string.no_data));
                //TESTING
                CircleView circleView = (CircleView) result_ppm_layout.findViewById(R.id.result_ppm_layoutCircleView);
                circleView.circleView(Color.RED);

            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

            layout.addView(result_ppm_layout);

        }
    }


    private double calculatePPM(double[] colorValues, JSONArray colours) throws Exception{

        JSONArray patchColorValues;
        double ppmPatchValueStart,ppmPatchValueEnd;
        double[] pointStart;
        double[] pointEnd;
        double LInter, aInter, bInter, vInter;
        int INTERPOLNUM = 10;
        double result;

        // compute total number of interpolated values the table will hold
        // this includes INTERPOLNUM values in between each two patches
        // The table holds [L, a, b, ppm value]
        double[][] interpolTable = new double[(colours.length() - 1) * INTERPOLNUM + 1][4];
        int count = 0;
        for (int i = 0; i < colours.length() - 1; i++) {
            try {
                patchColorValues = colours.getJSONObject(i).getJSONArray("lab");
                ppmPatchValueStart = colours.getJSONObject(i).getDouble("value");
                pointStart = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                patchColorValues = colours.getJSONObject(i + 1).getJSONArray("lab");
                ppmPatchValueEnd = colours.getJSONObject(i + 1).getDouble("value");
                pointEnd = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                double LStart = pointStart[0];
                double aStart = pointStart[1];
                double bStart = pointStart[2];

                double dL = (pointEnd[0] - pointStart[0]) / INTERPOLNUM;
                double da = (pointEnd[1] - pointStart[1]) / INTERPOLNUM;
                double db = (pointEnd[2] - pointStart[2]) / INTERPOLNUM;
                double dV = (ppmPatchValueEnd - ppmPatchValueStart) / INTERPOLNUM;

                // create 10 interpolation points, including the start point,
                // but excluding the end point

                for (int ii = 0; ii < INTERPOLNUM; ii++) {
                    LInter = LStart + ii * dL;
                    aInter = aStart + ii * da;
                    bInter = bStart + ii * db;
                    vInter = ppmPatchValueStart + ii * dV;

                    interpolTable[count][0] = LInter;
                    interpolTable[count][1] = aInter;
                    interpolTable[count][2] = bInter;
                    interpolTable[count][3] = vInter;
                    count++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // add final point
            patchColorValues = colours.getJSONObject(colours.length() - 1).getJSONArray("lab");
            interpolTable[count][0] = patchColorValues.getDouble(0);
            interpolTable[count][1] = patchColorValues.getDouble(1);
            interpolTable[count][2] = patchColorValues.getDouble(2);
            interpolTable[count][3] = colours.getJSONObject(colours.length() - 1).getDouble("value");
        }

        // determine closest value
        // create interpolation and extrapolation tables using linear approximation
        if (colorValues == null || colorValues.length < 3){
            throw new Exception("no valid lab colour data.");
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[] labPoint = new double[]{colorValues[0] / 2.55, colorValues[1] - 128, colorValues[2] - 128};

        System.out.println("*** lab value:" + labPoint[0] + "," + labPoint[1] + "," + labPoint[2]);
        double dist;
        int minPos = 0;
        double smallestE94Dist = Double.MAX_VALUE;


        for (int j = 0; j < interpolTable.length; j++) {
            // Find the closest point using the E94 distance
            // the values are already in the right range, so we don't need to normalize
            dist = CalibrationCard.E94(labPoint[0],labPoint[1],labPoint[2],interpolTable[j][0], interpolTable[j][1], interpolTable[j][2], false);
            if (dist < smallestE94Dist){
                smallestE94Dist = dist;
                minPos = j;
            }
        }
        result = interpolTable[minPos][3];
        System.out.println("*** result ppm:" + result);
        return result;
    }

    // old way of computing ppm value, using geometric method to determine closest point
//    private double calculatePPM_old(double[] colorValues, JSONArray colours) throws Exception{
//
//        List<Pair<Integer,Double>> labdaList = new ArrayList<>();
//        double ppm = Double.MAX_VALUE;
//        double[] pointA = null;
//        double[] pointB = null;
//        double[] pointC = null;
//        double labda;
//        double minLabdaAbs = Double.MAX_VALUE;
//        JSONArray patchColorValues;
//        double distance;
//        double minDistance = Double.MAX_VALUE;
//
//        // make pointC array
//        if (colorValues != null) {
//
//            pointC = colorValues;
//
//            // in strips.json, lab values for a en b are between -128 and 128 and l between 0 - 100
//            //  but in OpenCV the range is: 0 - 255.
//            // we calculate pointC values back to CIE-Lab values
//            if(colorSchema.equals("lab"))
//            {
//                if(pointC.length < 3)
//                {
//                    throw new Exception("no valid lab data.");
//                }
//                pointC[0] = (pointC[0] / 255) * 100;
//                pointC[1] = pointC[1] - 128;
//                pointC[2] = pointC[2] - 128;
//            }
//
//            //qualityChecksOK test
//            testLab(pointC);
//            testCount ++;
//            //end test
//        }
//
//        for (int j = 0; j < colours.length() - 1; j++) {
//
//            // make points A and B
//            try
//            {
//                patchColorValues = colours.getJSONObject(j).getJSONArray(colorSchema);
//                pointA = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};
//
//                patchColorValues = colours.getJSONObject(j + 1).getJSONArray(colorSchema);
//                pointB = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};
//
//
//            }
//            catch (JSONException e)
//            {
//                e.printStackTrace();
//            }
//
//            try
//            {
//                if (pointA != null && pointB != null && pointC != null)
//                {
//                    labda = getClosestPointOnLine(pointA, pointB, pointC);
//
//                    distance = getDistanceCtoAB(pointA, pointB, pointC, labda);
//
//                    // if labda is between 0 and 1, it is valid.
//                    if (0 < labda && labda < 1) {
//
//                        //choose shortest distance if more than one patch is valid
//                        if( distance < minDistance) {
//
//                            minDistance = distance;
//
//                            ppm = (1 - labda) * colours.getJSONObject(j).getDouble("value") + labda * colours.getJSONObject(j + 1).getDouble("value");
//                        }
//                    }
//
//                    //add value for labda to list for later use: extrapolate ppm
//                    labdaList.add(new Pair(j, labda));
//
////                    System.out.println("***RGB*** color patch no: " + j + " distance: " + distance +
////                            "  labda: " + labda + " ppm: " + ppm);
//
//                }
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        if(ppm == Double.MAX_VALUE)
//        {
//            //no labda between 0 and 1 is found: extrapolate ppm value
//
//            try
//            {
//                //find the lowest value for labda and calculate ppm with that
//                for (int i = 0; i < labdaList.size(); i++) {
//                    labda = Math.abs(labdaList.get(i).second);
//                    if (labda < minLabdaAbs) {
//
//                        minLabdaAbs = labda;
//                        if(labdaList.get(i).first < colours.length()) {
//                            ppm = (1 - labda) * colours.getJSONObject(labdaList.get(i).first).getDouble("value") +
//                                    labda * colours.getJSONObject(labdaList.get(i).first + 1).getDouble("value");
//                        }
//
//                        // System.out.println("***SETTING VALUE FOR PPM: " + ppm);
//
//                    }
//                }
//                //System.out.println("***NO MATCH FOUND*** strip patch no: "  + "  labda: " + labda + " ppm: " + ppm);
//
//            }
//            catch (JSONException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        return ppm;
//    }

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
//    private double getClosestPointOnLine(double[] pointA, double[] pointB, double[] pointC) throws Exception {
//
//
//        if(pointA.length<3 || pointB.length<3 || pointC.length<3)
//            throw new Exception("array lengths should all be 3");
//
//        double pxA = pointA[0];
//        double pyA = pointA[1];
//        double pzA = pointA[2];
//        double pxB = pointB[0];
//        double pyB = pointB[1];
//        double pzB = pointB[2];
//        double pxC = pointC[0];
//        double pyC = pointC[1];
//        double pzC = pointC[2];
//
//        double numerator = (pxA - pxC) * (pxB - pxA) + (pyA - pyC) * (pyB - pyA) + (pzA - pzC) * (pzB - pzA);
//        double denominator = Math.pow((pxB - pxA), 2) + Math.pow(pyB - pyA, 2) + Math.pow(pzB - pzA, 2);
//
//        double t = - numerator / denominator;
//
//        return t;
//
//    }
//
//    private double getDistanceCtoAB(double[] pointA, double[] pointB, double[] pointC, double labda) throws Exception {
//
//
//        if(pointA.length<3 || pointB.length<3 || pointC.length<3)
//            throw new Exception("array lengths should all be 3");
//
//        double pxA = pointA[0];
//        double pyA = pointA[1];
//        double pzA = pointA[2];
//        double pxB = pointB[0];
//        double pyB = pointB[1];
//        double pzB = pointB[2];
//        double pxC = pointC[0];
//        double pyC = pointC[1];
//        double pzC = pointC[2];
//
//        double pxC1 = pxA * (1-labda) + labda*pxB;
//        double pyC1 = pyA * (1-labda) + labda*pyB;
//        double pzC1 = pzA * (1-labda) + labda*pzB;
//
//        return Math.sqrt(Math.pow(pxC1 - pxC, 2) + Math.pow(pyC1 - pyC, 2) + Math.pow(pzC1 - pzC, 2));
//
//    }
//
//    private double getDistanceBetween2Points3D(double[] pointA, double[] pointB)
//    {
//        double pxA = pointA[0];
//        double pyA = pointA[1];
//        double pzA = pointA[2];
//        double pxB = pointB[0];
//        double pyB = pointB[1];
//        double pzB = pointB[2];
//
//        return Math.sqrt(Math.pow(pxB - pxA, 2) + Math.pow(pyB - pyA, 2) + Math.pow(pzB - pzA, 2));
//
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_result, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    private void testRGB(double[] pointC)
//    {
//        Scalar[] testColorsListRGB = new Scalar[]
//                {
//                        new Scalar(255, 232.21, 168.64), //light yellow
//                        new Scalar(208.83, 218.83, 150.51), //light green
//                        new Scalar(255, 168.51, 161.92), //medium pink
//                        new Scalar(200.30, 169.03, 181.46), //lilac
//                        new Scalar(239.90, 117.48, 142.37) //dark pink
//
//                };
//        Locale l = Locale.US;
//
//        System.out.print("***test color ,");
//        System.out.print(testCount + "," + String.format(l, "%.2f", testColorsListRGB[testCount].val[0]) +
//                ", " + String.format(l, "%.2f", testColorsListRGB[testCount].val[1]) + ", " +
//                String.format(l, "%.2f", testColorsListRGB[testCount].val[2]) + ",");
//
//        System.out.print(String.format(l, "%.2f", pointC[0]) + ", "
//                + String.format(l, "%.2f", pointC[1]) + ", " + String.format(l, "%.2f", pointC[2]));
//
//
//        try {
//
//            double[] pointA = testColorsListRGB[testCount].val;
//
//            double distance = getDistanceBetween2Points3D(pointA, pointC);
//            System.out.print("," + distance);
//            System.out.println(",***");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

//    private void testLab(double[] pointC)
//    {
//        Scalar[] testColorsListLab = new Scalar[]
//                {
////                        new Scalar(64.27, 48.17, - 3.48),
////                        new Scalar(73.02,10.895, - 17.26),
////                        new Scalar(77.915, 30.98, 3.01),
////                        new Scalar(85.93, - 16.24, 20),
////                        new Scalar(93.58, 0.6, 20.37)
//                        new Scalar(93.58, 0.6, 20.37),
//                        new Scalar(85.93, - 16.24, 20),
//                        new Scalar(77.915, 30.98, 3.01),
//                        new Scalar(73.02,10.895, - 17.26),
//                        new Scalar(64.27, 48.17, - 3.48),
//                };
//        Locale l = Locale.US;
//
//        String data = "***test color Lab,";
//
//        data += testCount + "," + String.format(l, "%.2f", testColorsListLab[testCount].val[0]) +
//                ", " + String.format(l, "%.2f", testColorsListLab[testCount].val[1]) + ", " +
//                String.format(l, "%.2f", testColorsListLab[testCount].val[2]) + ",";
//
//        data += String.format(l, "%.2f", pointC[0]) + ", "
//                + String.format(l, "%.2f", pointC[1]) + ", " + String.format(l, "%.2f", pointC[2]);
//
//
//        try {
//
//            double[] pointA = testColorsListLab[testCount].val;
//
//            double distance = getDistanceBetween2Points3D(pointA, pointC);
//            data += "," + distance;
//
//            double E94 =  CalibrationCard.getInstance().E94(pointA[0], pointA[1], pointA[2],
//                    pointC[0], pointC[1], pointC[2]);
//            data += "," + E94;
//            data += "\n";
//
//            FileStorage.writeLogToSDFile(data);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}
