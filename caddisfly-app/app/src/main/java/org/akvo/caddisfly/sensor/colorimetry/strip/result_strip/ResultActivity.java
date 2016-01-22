package org.akvo.caddisfly.sensor.colorimetry.strip.result_strip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.sensor.colorimetry.strip.BaseActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ColorimetryStripActivity;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.CircleView;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.OpenCVUtils;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration.CalibrationCard;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.color.ColorDetected;
import org.akvo.caddisfly.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.Arrays;
import java.util.List;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class ResultActivity extends BaseActivity{

    private JSONObject resultJsonObj = new JSONObject();
    private JSONArray resultJsonArr = new JSONArray();
    private FileStorage fileStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            fileStorage = new FileStorage(this);
            final String brandName = intent.getStringExtra(Constant.BRAND);

            Mat strip;
            StripTest stripTest = new StripTest();
            StripTest.Brand brand = stripTest.getBrand(brandName);

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
                        //System.out.println("***imagePatchArray: " + imagePatchArray.toString(1));

                        JSONArray array = imagePatchArray.getJSONArray(i);
                        // get the image number from the json array
                        int imageNo = array.getInt(0);

                        //if in DetectStripTask, no strip was found, an image was saved with the String Constant.ERROR
                        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

                        String error = isInvalidStrip? Constant.ERROR: "";

                        // read the Mat object from internal storage
                        byte[] data = fileStorage.readByteArray(Constant.STRIP + imageNo + error);
                        if (data != null) {
                            // determine cols and rows dimensions
                            byte[] rows = new byte[4];
                            byte[] cols = new byte[4];

                            int length = data.length;
                            System.arraycopy(data,length - 8, rows, 0, 4);
                            System.arraycopy(data,length - 4, cols, 0, 4);

                            int rowsNum = fileStorage.byteArrayToLeInt(rows);
                            int colsNum = fileStorage.byteArrayToLeInt(cols);

                            // remove last part
                            byte[] imgData = Arrays.copyOfRange(data, 0, data.length - 8);

                            // reserve Mat of proper size:
                            strip = new Mat(rowsNum, colsNum, CvType.CV_8UC3);

                            // put image data back in Mat:
                            strip.put(0,0,imgData);

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
                        //new BitmapTask(true, null, new Point(1,1),null, null).execute(new Mat());

                        continue;
                    }
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("no data");
                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

                layout.addView(textView);

                //TESTING
                //new BitmapTask(true, null, new Point(1,1),null, null).execute(new Mat());
            }

            Button save = (Button) findViewById(R.id.activity_resultButtonSave);
            Button redo = (Button) findViewById(R.id.activity_resultButtonRedo);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    listener.onResult(resultJsonArr.toString());

                    Intent i = new Intent(v.getContext(), ColorimetryStripActivity.class);
                    i.putExtra("finish", true);
                    i.putExtra("response", resultJsonArr.toString());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    finish();
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
        private Bitmap combinedBitmap = null;
        private Mat combined;
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

                Core.copyMakeBorder(mat, mat, borderSize, borderSize, borderSize, borderSize, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

                //Draw a green circle around each patch
                Imgproc.circle(mat, new Point(centerPatch.x + borderSize, mat.height() / 2), (int) Math.ceil(mat.height() * 0.4),
                        new Scalar(0, 255, 0, 255), 2);

                /*
                 * Start making mats to put into image to be send back as an String to server
                */
                double xMargin = 10d;
                int circleRadius = 10;
                double xtrans = (double) mat.cols() / (double) colours.length(); //calculate size of each color range block
                double yColorRect = 20d; //distance from top Mat to top color rectangles
                boolean ppmIsDrawn = false;
                Scalar labWhite = new Scalar(255, 128, 128);
                Scalar labGrey = new Scalar(128, 128, 128);
                Scalar labBlack = new Scalar(0, 128, 128);
                JSONObject colourObj;
                JSONObject nextcolourObj;

                /*
                * Create Mat to hold description of patch
                 */
                int[] baseline = new int[1];
                Size textSizeDesc = Imgproc.getTextSize(desc, Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, baseline);
                Mat descMat = new Mat((int) Math.ceil(textSizeDesc.height) * 3, mat.cols(), CvType.CV_8UC3, labWhite);
                Imgproc.putText(descMat, desc, new Point(2, descMat.height() - textSizeDesc.height), Core.FONT_HERSHEY_SIMPLEX, 0.35d, labBlack, 1, Core.LINE_8, false);

                 /*
                * COLOR RANGE AS IN JSON FILE (FROM MANUFACTURER)
                * Create Mat to hold a rectangle for each color
                * the corresponding value written as text above that rectangle
                 */
                Mat colorRangeMat = new Mat((int) Math.ceil(xtrans - xMargin + yColorRect), mat.cols(), CvType.CV_8UC3, labWhite);
                for (int d = 0; d < colours.length(); d++) {
                    try {

                        colourObj = colours.getJSONObject(d);

                        double value = colourObj.getDouble("value");
                        JSONArray lab = colourObj.getJSONArray("lab");
                        Scalar scalarLab = new Scalar(lab.getDouble(0) / 100 * 255, lab.getDouble(1) + 128, lab.getDouble(2) + 128);
                        Size textSizeValue = Imgproc.getTextSize(round(value), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                        //draw a rectangle filled with color for ppm value
                        Point topleft = new Point(xtrans * d, yColorRect);
                        Point bottomright =  new Point(topleft.x + xtrans - xMargin, yColorRect + xtrans);
                        Imgproc.rectangle(colorRangeMat, topleft, bottomright, scalarLab, -1);

                        //draw color value above rectangle
                        Point centerText = new Point(topleft.x + (bottomright.x - topleft.x) / 2 - textSizeValue.width / 2, yColorRect - textSizeValue.height);
                        Imgproc.putText(colorRangeMat, round(value), centerText, Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                /*
                * END COLOR RANGE
                 */

                 /*
                * VALUE MEASURED
                * Create Mat to hold a line between min and max values, on it a circle filled with
                * the color detected below which the ppm value measured
                 */
                Mat valueMeasuredMat = new Mat(50, mat.cols(), CvType.CV_8UC3, labWhite);

                //grey line with ppm values at left and right
                Imgproc.line(valueMeasuredMat, new Point(xMargin, 25), new Point(valueMeasuredMat.cols() - 2 * xMargin, 25), labGrey, 2, Core.LINE_AA, 0);

                try {
                    //get values for lowest and highest ppm values from striptest range
                    double leftValue = colours.getJSONObject(0).getDouble("value");
                    double rightValue = colours.getJSONObject(colours.length() - 1).getDouble("value");
                    Size textSizeLeftValue = Imgproc.getTextSize(String.format("%.0f", leftValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);
                    Size textSizeRightValue = Imgproc.getTextSize(String.format("%.0f", rightValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                    Imgproc.putText(valueMeasuredMat, String.format("%.0f", leftValue), new Point((xtrans - xMargin) / 2 - textSizeLeftValue.width / 2, 15), Core.FONT_HERSHEY_SIMPLEX,
                            0.3d, labGrey, 1, Core.LINE_AA, false);
                    Imgproc.putText(valueMeasuredMat, String.format("%.0f", rightValue),
                            new Point(valueMeasuredMat.cols() - xMargin - (xtrans - xMargin) / 2 - textSizeRightValue.width / 2, 15),
                            Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


                    for (int d = 0; d < colours.length(); d++) {
                        try {

                            colourObj = colours.getJSONObject(d);
                            if (d < colours.length() - 1) {
                                nextcolourObj = colours.getJSONObject(d + 1);
                            } else nextcolourObj = colourObj;

                            double value = colourObj.getDouble("value");
                            double nextvalue = nextcolourObj.getDouble("value");

                            if (ppm < nextvalue && !ppmIsDrawn) {

                                double restPPM = ppm - (value); //calculate the amount above the lowest value
                                double transX = xtrans * (restPPM / (nextvalue - value)); //calculate number of pixs needed to translate in x direction

                                Scalar ppmColor = colorDetected.getLab();
                                //calculate where the center of the circle should be
                                double left = xtrans * d;
                                double right =  left + xtrans - xMargin;
                                Point centerCircle = (transX) + xtrans * d < xMargin ? new Point(xMargin, 25d) :
                                        new Point(left + (right - left)/2 + transX, 25d);

                                //get text size of value test
                                Size textSizePPM = Imgproc.getTextSize(round(ppm), Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, null);

                                Imgproc.circle(valueMeasuredMat, centerCircle, circleRadius, ppmColor, -1, Imgproc.LINE_AA, 0);
                                Imgproc.putText(valueMeasuredMat, round(ppm), new Point(centerCircle.x - textSizePPM.width / 2, 47d), Core.FONT_HERSHEY_SIMPLEX, 0.35d,
                                        labGrey, 1, Core.LINE_AA, false);

                                ppmIsDrawn = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    /*
                     * END VALUE MEASURED
                    */


                    /*
                    * PUTTING ALL TOGETHER
                    */
                    Imgproc.cvtColor(descMat, descMat, Imgproc.COLOR_Lab2RGB);
                    Imgproc.cvtColor(colorRangeMat, colorRangeMat, Imgproc.COLOR_Lab2RGB);
                    Imgproc.cvtColor(valueMeasuredMat, valueMeasuredMat, Imgproc.COLOR_Lab2RGB);

                    combined = new Mat(descMat.rows() + mat.rows() + colorRangeMat.rows() + valueMeasuredMat.rows(), mat.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255));

                    Rect roi = new Rect(0, descMat.height(), mat.width(), mat.height());
                    Mat roiMat = combined.submat(roi);
                    mat.copyTo(roiMat);

                    roi = new Rect(0, descMat.height() + mat.height(), colorRangeMat.width(), colorRangeMat.height());
                    roiMat = combined.submat(roi);
                    colorRangeMat.copyTo(roiMat);

                    roi = new Rect(0, descMat.height() + mat.height() + colorRangeMat.height(), valueMeasuredMat.width(), valueMeasuredMat.height());
                    roiMat = combined.submat(roi);
                    valueMeasuredMat.copyTo(roiMat);

                    //make bitmap to be rendered on screen
                    if (!combined.empty()) {
                        stripBitmap = makeBitmap(combined);
                    }

                    //add name of patch to combined mat
                    roi = new Rect(0, 0, descMat.width(), descMat.height());
                    roiMat = combined.submat(roi);
                    descMat.copyTo(roiMat);

                    //make bitmap to be send to server
                    if (!combined.empty()) {

                        combinedBitmap = makeBitmap(combined);
                        FileStorage.writeToSDFile(combinedBitmap);
                    }
                 /*
                 * End making mats to put into image to be send back as an String to server
                 */
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                //System.out.println("***invalid mat object***");

                if(!mat.empty())
                {
                    //done with lab shema, make rgb to show in imageview
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
                    stripBitmap = makeBitmap(mat);
                }
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            LayoutInflater inflater = (LayoutInflater) ResultActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            LinearLayout result_ppm_layout = (LinearLayout) inflater.inflate(R.layout.result_ppm_layout, null, false);

            TextView descView = (TextView) result_ppm_layout.findViewById(R.id.result_ppm_layoutDescView);
            descView.setText(desc);

            ImageView imageView = (ImageView) result_ppm_layout.findViewById(R.id.result_ppm_layoutImageView);
            CircleView circleView = (CircleView) result_ppm_layout.findViewById(R.id.result_ppm_layoutCircleView);

            if (stripBitmap != null) {
                imageView.setImageBitmap(stripBitmap);

                if (!invalid) {
                    if (colorDetected != null) {
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
            } else {
                descView.append("\n\n" + getResources().getString(R.string.no_data));

                circleView.circleView(Color.RED);
            }

            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);
            layout.addView(result_ppm_layout);

            //put ppm and image in resultJsonArr
            if (combinedBitmap != null) {

                //TESTING COMBINED BITMAP
                //imageView.setImageBitmap(combinedBitmap);

                try {
                    JSONObject object = new JSONObject();
                    object.put("name", desc);
                    object.put("value", ppm);
                    object.put("unit", unit);
                    String img = fileStorage.bitmapToBase64String(combinedBitmap);
                    object.put("img", img);

                    resultJsonArr.put(object);

                    //TESTING write image string to external storage
                    //FileStorage.writeLogToSDFile("base64.txt", img, false);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private double calculatePPM(double[] colorValues, JSONArray colours) throws Exception {

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

    private String round(double value)
    {
        String valueString = String.format(Locale.US, "%.2f", value);
        int indexDecimal = valueString.indexOf(".");
        double decimal = Double.valueOf(valueString.substring(indexDecimal+1));
        if(decimal > 0)
            return String.format("%.1f", value);
        else
            return String.format("%.0f", value);
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
