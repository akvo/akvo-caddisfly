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

import java.io.IOException;
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

            // get information on the strip test from JSON
            StripTest.Brand brand = stripTest.getBrand(brandName);
            List<StripTest.Brand.Patch> patches = brand.getPatches();

            // get the JSON describing the images of the patches that were stored before
            JSONArray imagePatchArray = null;
            try {
                String json = fileStorage.readFromInternalStorage(Constant.IMAGE_PATCH + ".txt");
                if (json != null) {
                    imagePatchArray = new JSONArray(json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // cycle over the patches and interpret them
            if (imagePatchArray != null) {
                // if this strip is of type 'GROUP', take the first image and use that for all the patches
                System.out.println("*** grouping:" + brand.getGroupingType());
                if (brand.getGroupingType() == StripTest.groupType.GROUP) {
                    // handle grouping case

                    // get the first patch image
                    String desc = patches.get(0).getDesc();
                    JSONArray array = null;
                    try {
                        // get strip image into Mat object
                        array = imagePatchArray.getJSONArray(0);
                        int imageNo = array.getInt(0);
                        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);
                        strip = getMatFromFile(imageNo);
                        if (strip != null) {
                            new BitmapTask(isInvalidStrip, strip, true, brand, patches, 0).execute(strip);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // if this strip is of type 'INDIVIDUAL' handle patch by patch
                    for (int i = 0; i < patches.size(); i++) { // handle patch
                        JSONArray array = null;

                        try {
                            array = imagePatchArray.getJSONArray(i);

                            // get the image number from the json array
                            int imageNo = array.getInt(0);
                            boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

                            // read strip from file
                            strip = getMatFromFile(imageNo);

                            if (strip != null) {
                                new BitmapTask(isInvalidStrip, strip, false, brand, patches, i).execute(strip);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                TextView textView = new TextView(this);
                textView.setText("no data");
                LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);

                layout.addView(textView);
            }
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

    private Mat getMatFromFile(int imageNo) {
        //if in DetectStripTask, no strip was found, an image was saved with the String Constant.ERROR
        boolean isInvalidStrip = fileStorage.checkIfFilenameContainsString(Constant.STRIP + imageNo + Constant.ERROR);

        String error = isInvalidStrip ? Constant.ERROR : "";

        // read the Mat object from internal storage
        byte[] data = new byte[0];
        try {
            data = fileStorage.readByteArray(Constant.STRIP + imageNo + error);

            if (data != null) {
                // determine cols and rows dimensions
                byte[] rows = new byte[4];
                byte[] cols = new byte[4];

                int length = data.length;
                System.arraycopy(data, length - 8, rows, 0, 4);
                System.arraycopy(data, length - 4, cols, 0, 4);

                int rowsNum = fileStorage.byteArrayToLeInt(rows);
                int colsNum = fileStorage.byteArrayToLeInt(cols);

                // remove last part
                byte[] imgData = Arrays.copyOfRange(data, 0, data.length - 8);

                // reserve Mat of proper size:
                Mat result = new Mat(rowsNum, colsNum, CvType.CV_8UC3);

                // put image data back in Mat:
                result.put(0, 0, imgData);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    Scalar labWhite = new Scalar(255, 128, 128);
    Scalar labGrey = new Scalar(128, 128, 128);
    Scalar labBlack = new Scalar(0, 128, 128);
    double yColorRect = 20d; //distance from top Mat to top color rectangles
    int circleRadius = 10;
    double xMargin = 10d;

    private Mat createStripMat(Mat mat, int borderSize, Point centerPatch, boolean grouped){
        //done with lab shema, make rgb to show in imageview
        // mat holds the strip image
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);

        //extend the strip with a border, so we can draw a circle around each patch that is
        //wider than the strip itself. That is just because it looks nice.


        Core.copyMakeBorder(mat, mat, borderSize, borderSize, borderSize, borderSize, Core.BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

        // Draw a green circle at a particular location patch
        // only draw if this is not a 'grouped' strip
        if (!grouped) {
            Imgproc.circle(mat, new Point(centerPatch.x + borderSize, mat.height() / 2), (int) Math.ceil(mat.height() * 0.4),
                new Scalar(0, 255, 0, 255), 2);
        }
        return mat;
    }

    private Mat createDescriptionMat(String desc,int width){
        int[] baseline = new int[1];
        Size textSizeDesc = Imgproc.getTextSize(desc, Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, baseline);
        Mat descMat = new Mat((int) Math.ceil(textSizeDesc.height) * 3, width, CvType.CV_8UC3, labWhite);
        Imgproc.putText(descMat, desc, new Point(2, descMat.height() - textSizeDesc.height), Core.FONT_HERSHEY_SIMPLEX, 0.35d, labBlack, 1, Core.LINE_8, false);

        return descMat;
    }

    /*
      * COLOR RANGE AS IN JSON FILE (FROM MANUFACTURER)
      * Create Mat to hold a rectangle for each color
      * the corresponding value written as text above that rectangle
      */
    private Mat createColourRangeMatSingle(List<StripTest.Brand.Patch> patches, int patchNum, int width, double xtrans){
        // horizontal size of mat: width
        // vertical size of mat: size of colour block - xmargin + top distance
        Mat colorRangeMat = new Mat((int) Math.ceil(xtrans - xMargin + yColorRect), width, CvType.CV_8UC3, labWhite);
        JSONArray colours = null;
        colours = patches.get(patchNum).getColours();

        for (int d = 0; d < colours.length(); d++) {
            try {

                JSONObject colourObj = colours.getJSONObject(d);

                double value = colourObj.getDouble("value");
                JSONArray lab = colourObj.getJSONArray("lab");
                Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * 255, lab.getDouble(1) + 128, lab.getDouble(2) + 128);
                Size textSizeValue = Imgproc.getTextSize(round(value), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                //draw a rectangle filled with color for ppm value
                Point topleft = new Point(xtrans * d, yColorRect);
                Point bottomright = new Point(topleft.x + xtrans - xMargin, yColorRect + xtrans);
                Imgproc.rectangle(colorRangeMat, topleft, bottomright, scalarLab, -1);

                //draw color value above rectangle
                Point centerText = new Point(topleft.x + (bottomright.x - topleft.x) / 2 - textSizeValue.width / 2, yColorRect - textSizeValue.height);
                Imgproc.putText(colorRangeMat, round(value), centerText, Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return colorRangeMat;
    }

    /*
  * COLOR RANGE AS IN JSON FILE (FROM MANUFACTURER)
  * Create Mat to hold a rectangle for each color
  * the corresponding value written as text above that rectangle
  */
    private Mat createColourRangeMatGroup(List<StripTest.Brand.Patch> patches, int width,double xtrans){
        // horizontal size of mat: width
        // vertical size of mat: size of colour block - xmargin + top distance

        int numPatches = patches.size();
        Mat colorRangeMat = new Mat((int) Math.ceil(numPatches * (xtrans + xMargin) - xMargin + yColorRect), width, CvType.CV_8UC3, labWhite);

        JSONArray colours;
        int offset = 0;
        System.out.println("*** number of patches:" + numPatches);
        for (int p = 0; p < numPatches; p++) {
            colours = patches.get(p).getColours();
            for (int d = 0; d < colours.length(); d++) {
                try {

                    JSONObject colourObj = colours.getJSONObject(d);

                    double value = colourObj.getDouble("value");
                    JSONArray lab = colourObj.getJSONArray("lab");
                    Scalar scalarLab = new Scalar((lab.getDouble(0) / 100) * 255, lab.getDouble(1) + 128, lab.getDouble(2) + 128);
                    Size textSizeValue = Imgproc.getTextSize(round(value), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

                    //draw a rectangle filled with color for ppm value
                    Point topleft = new Point(xtrans * d, yColorRect + offset);
                    Point bottomright = new Point(topleft.x + xtrans - xMargin, yColorRect + xtrans + offset);
                    Imgproc.rectangle(colorRangeMat, topleft, bottomright, scalarLab, -1);

                    //draw color value above rectangle
                    if (p == 0) {
                        Point centerText = new Point(topleft.x + (bottomright.x - topleft.x) / 2 - textSizeValue.width / 2, yColorRect - textSizeValue.height);
                        Imgproc.putText(colorRangeMat, round(value), centerText, Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            offset += xtrans + xMargin;
        }
        return colorRangeMat;
    }

    /*
       * VALUE MEASURED
       * Create Mat to hold a line between min and max values, on it a circle filled with
       * the color detected below which the ppm value measured
       */
    private Mat createValueMeasuredMatSingle(JSONArray colours,double ppm, ColorDetected colorDetected, int width,double xtrans){
        Mat valueMeasuredMat = new Mat(50, width, CvType.CV_8UC3, labWhite);
        JSONObject colourObj;
        JSONObject nextcolourObj;
        boolean ppmIsDrawn = false;

        //grey line with ppm values at left and right
        Imgproc.line(valueMeasuredMat, new Point(xMargin, 25), new Point(valueMeasuredMat.cols() - 2 * xMargin, 25), labGrey, 2, Core.LINE_AA, 0);

        //get values for lowest and highest ppm values from striptest range
        double leftValue = 0;
        try {
            leftValue = colours.getJSONObject(0).getDouble("value");

            double rightValue = colours.getJSONObject(colours.length() - 1).getDouble("value");
            Size textSizeLeftValue = Imgproc.getTextSize(String.format("%.0f", leftValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);
            Size textSizeRightValue = Imgproc.getTextSize(String.format("%.0f", rightValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

            Imgproc.putText(valueMeasuredMat, String.format("%.0f", leftValue), new Point((xtrans - xMargin) / 2 - textSizeLeftValue.width / 2, 15), Core.FONT_HERSHEY_SIMPLEX,
                0.3d, labGrey, 1, Core.LINE_AA, false);
            Imgproc.putText(valueMeasuredMat, String.format("%.0f", rightValue),
                new Point(valueMeasuredMat.cols() - xMargin - (xtrans - xMargin) / 2 - textSizeRightValue.width / 2, 15),
                Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


            // we need to iterate over the ppm values to determine where the circle should be placed
            for (int d = 0; d < colours.length(); d++) {
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
                    double right = left + xtrans - xMargin;
                    Point centerCircle = (transX) + xtrans * d < xMargin ? new Point(xMargin, 25d) :
                        new Point(left + (right - left) / 2 + transX, 25d);

                    //get text size of value test
                    Size textSizePPM = Imgproc.getTextSize(round(ppm), Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, null);

                    Imgproc.circle(valueMeasuredMat, centerCircle, circleRadius, ppmColor, -1, Imgproc.LINE_AA, 0);
                    Imgproc.putText(valueMeasuredMat, round(ppm), new Point(centerCircle.x - textSizePPM.width / 2, 47d), Core.FONT_HERSHEY_SIMPLEX, 0.35d,
                        labGrey, 1, Core.LINE_AA, false);

                    ppmIsDrawn = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return valueMeasuredMat;
    }


    /*
       * VALUE MEASURED
       * Create Mat to hold a line between min and max values, on it a circle filled with
       * the color detected below which the ppm value measured
       */
    private Mat createValueMeasuredMatGroup(JSONArray colours,double ppm, ColorDetected[] colorsDetected, int width, double xtrans){
        int size = 50 + colorsDetected.length * (2 * circleRadius + 5);
        Mat valueMeasuredMat = new Mat(size, width, CvType.CV_8UC3, labWhite);

        JSONObject colourObj;
        JSONObject nextcolourObj;
        boolean ppmIsDrawn = false;

        //grey line with ppm values at left and right
        Imgproc.line(valueMeasuredMat, new Point(xMargin, 25), new Point(valueMeasuredMat.cols() - 2 * xMargin, 25), labGrey, 2, Core.LINE_AA, 0);

        //get values for lowest and highest ppm values from striptest range
        double leftValue = 0;
        try {
            leftValue = colours.getJSONObject(0).getDouble("value");

            double rightValue = colours.getJSONObject(colours.length() - 1).getDouble("value");
            Size textSizeLeftValue = Imgproc.getTextSize(String.format("%.0f", leftValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);
            Size textSizeRightValue = Imgproc.getTextSize(String.format("%.0f", rightValue), Core.FONT_HERSHEY_SIMPLEX, 0.3d, 1, null);

            Imgproc.putText(valueMeasuredMat, String.format("%.0f", leftValue), new Point((xtrans - xMargin) / 2 - textSizeLeftValue.width / 2, 15), Core.FONT_HERSHEY_SIMPLEX,
                0.3d, labGrey, 1, Core.LINE_AA, false);
            Imgproc.putText(valueMeasuredMat, String.format("%.0f", rightValue),
                new Point(valueMeasuredMat.cols() - xMargin - (xtrans - xMargin) / 2 - textSizeRightValue.width / 2, 15),
                Core.FONT_HERSHEY_SIMPLEX, 0.3d, labGrey, 1, Core.LINE_AA, false);


            // we need to iterate over the ppm values to determine where the circle should be placed
            for (int d = 0; d < colours.length(); d++) {
                colourObj = colours.getJSONObject(d);
                if (d < colours.length() - 1) {
                    nextcolourObj = colours.getJSONObject(d + 1);
                } else nextcolourObj = colourObj;

                double value = colourObj.getDouble("value");
                double nextvalue = nextcolourObj.getDouble("value");

                if (ppm < nextvalue && !ppmIsDrawn) {

                    double restPPM = ppm - (value); //calculate the amount above the lowest value
                    double transX = xtrans * (restPPM / (nextvalue - value)); //calculate number of pixs needed to translate in x direction


                    //calculate where the center of the circle should be
                    double left = xtrans * d;
                    double right = left + xtrans - xMargin;
                    Point centerCircle = (transX) + xtrans * d < xMargin ? new Point(xMargin, 25d) :
                        new Point(left + (right - left) / 2 + transX, 25d);

                    //get text size of value test
                    Size textSizePPM = Imgproc.getTextSize(String.format("%.1f", ppm), Core.FONT_HERSHEY_SIMPLEX, 0.35d, 1, null);
                    Imgproc.putText(valueMeasuredMat, String.format("%.1f", ppm), new Point(centerCircle.x - textSizePPM.width / 2, 15d), Core.FONT_HERSHEY_SIMPLEX, 0.35d,
                        labGrey, 1, Core.LINE_AA, false);

                    double offset = circleRadius + 5;
                    for (int p = 0; p < colorsDetected.length; p++) {
                        Scalar ppmColor = colorsDetected[p].getLab();
                        Imgproc.circle(valueMeasuredMat, new Point(centerCircle.x,centerCircle.y + offset), circleRadius, ppmColor, -1, Imgproc.LINE_AA, 0);
                        offset += 2 * circleRadius + 5;
                    }

                    ppmIsDrawn = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return valueMeasuredMat;
    }

    private Mat concatenate(Mat m1, Mat m2){
        int width = Math.max(m1.cols(),m2.cols());
        int height = m1.rows() + m2.rows();

        Mat result = new Mat(height, width, CvType.CV_8UC3, new Scalar(255, 255, 255));

        // rect works with x, y, width, height
        Rect roi1 = new Rect(0, 0, m1.cols(), m1.rows());
        Mat roiMat1 = result.submat(roi1);
        m1.copyTo(roiMat1);

        Rect roi2 = new Rect(0, m1.rows(), m2.cols(), m2.rows());
        Mat roiMat2 = result.submat(roi2);
        m2.copyTo(roiMat2);

        return result;
    }

    private ColorDetected getPatchColour(Mat mat, Point centerPatch, int submatSize){

        //make a submat around center of the patch
        int minRow = (int) Math.round(Math.max(centerPatch.y - submatSize, 0));
        int maxRow = (int) Math.round(Math.min(centerPatch.y + submatSize, mat.height()));
        int minCol = (int) Math.round(Math.max(centerPatch.x - submatSize, 0));
        int maxCol = (int) Math.round(Math.min(centerPatch.x + submatSize, mat.width()));

        //  create submat
        Mat patch = mat.submat(minRow, maxRow, minCol, maxCol);

        // compute the mean colour and return it
        return OpenCVUtils.detectStripColorBrandKnown(patch);

    }

    private class BitmapTask extends AsyncTask<Mat, Void, Void>
    {
        private boolean invalid;
        private Bitmap stripBitmap = null;
        private Bitmap combinedBitmap = null;
        private Mat combined;
        private ColorDetected colorDetected;
        private ColorDetected[] colorsDetected;
        private double ppm = -1;
        private Boolean grouped;
        private StripTest.Brand brand;
        private List<StripTest.Brand.Patch> patches;
        private int patchNum;
        private Mat strip;
        String unit;
        String desc;

        public BitmapTask(boolean invalid, Mat strip, Boolean grouped, StripTest.Brand brand, List<StripTest.Brand.Patch> patches, int patchNum)
        {
            this.invalid = invalid;
            this.grouped = grouped;
            this.strip = strip;
            this.brand = brand;
            this.patches = patches;
            this.patchNum = patchNum;
        }

        @Override
        protected Void doInBackground(Mat... params) {
            Mat mat = params[0];
            int submatSize = 7;
            int borderSize = (int) Math.ceil(mat.height() * 0.5);

            if(mat.empty() || mat.height() < submatSize) {
                return null;
            }

            if (invalid){
                //System.out.println("***invalid mat object***");
                if(!mat.empty()) {
                    //done with lab shema, make rgb to show in imageview
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_Lab2RGB);
                    stripBitmap = makeBitmap(mat);
                }
                return null;
            }

            // get the name and unit of the patch
            desc = patches.get(patchNum).getDesc();
            unit = patches.get(patchNum).getUnit();

            // depending on the boolean grouped, we either handle all patches at once, or we handle only a single one

            JSONArray colours = null;
            Point centerPatch = null;

            // compute location of point to be sampled
            if (grouped){
                // collect colours
                double ratioW = strip.width() / brand.getStripLenght();
                colorsDetected = new ColorDetected[patches.size()];
                double[][] colorsValueLab = new double[patches.size()][3];
                for (int p = 0; p < patches.size(); p++){
                    double x = patches.get(p).getPosition() * ratioW;
                    double y = strip.height() / 2;
                    centerPatch = new Point(x, y);

                    colorDetected = getPatchColour(mat, centerPatch, submatSize);
                    double[] colorValueLab = colorDetected.getLab().val;

                    colorsDetected[p] = colorDetected;
                    colorsValueLab[p]= colorValueLab;
                }

                try {
                    ppm = calculatePpmGroup(colorsValueLab, patches);
                } catch (Exception e) {
                    e.printStackTrace();
                    ppm = Double.NaN;
                }


            } else {
                double ratioW = strip.width() / brand.getStripLenght();
                double x = patches.get(patchNum).getPosition() * ratioW;
                double y = strip.height() / 2;
                centerPatch = new Point(x, y);

                colorDetected = getPatchColour(mat, centerPatch, submatSize);
                double[] colorValueLab = colorDetected.getLab().val;

                //set the colours needed to calculate ppm
                colours = patches.get(patchNum).getColours();

                try {
                    ppm = calculatePpmSingle(colorValueLab, colours);
                } catch (Exception e) {
                    e.printStackTrace();
                    ppm = Double.NaN;
                }
            }

            ////////////// Create Image ////////////////////
            // calculate size of each color range block
            // divide the original strip width by the number of colours
            double xtrans;

            if (grouped){
                colours = patches.get(0).getColours();
                xtrans = (double) mat.cols() / (double) colours.length();
            } else {
                xtrans = (double) mat.cols() / (double) colours.length();
            }

            // create Mat to hold strip itself
            mat = createStripMat(mat, borderSize, centerPatch, grouped);

            // Create Mat to hold description of patch
            Mat descMat = createDescriptionMat(desc, mat.cols());

            // Create Mat to hold the colour range
            Mat colorRangeMat;
            if (grouped){
                colorRangeMat = createColourRangeMatGroup(patches, mat.cols(), xtrans);
            } else {
                colorRangeMat = createColourRangeMatSingle(patches, patchNum, mat.cols(), xtrans);
            }


            // create Mat to hold value measured
            Mat valueMeasuredMat = null;
            if (grouped){
                valueMeasuredMat = createValueMeasuredMatGroup(colours, ppm, colorsDetected, mat.cols(), xtrans);
            } else{
                valueMeasuredMat = createValueMeasuredMatSingle(colours, ppm, colorDetected, mat.cols(), xtrans);
            }

            //PUTTING IT ALL TOGETHER
            // transform all mats to RGB. The strip Matis already RGB
            Imgproc.cvtColor(descMat, descMat, Imgproc.COLOR_Lab2RGB);
            Imgproc.cvtColor(colorRangeMat, colorRangeMat, Imgproc.COLOR_Lab2RGB);
            Imgproc.cvtColor(valueMeasuredMat, valueMeasuredMat, Imgproc.COLOR_Lab2RGB);

            // create empty mat to serve as a template
            combined = new Mat(0, mat.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255));

            combined = concatenate(combined, mat); // add strip
            combined = concatenate(combined, colorRangeMat); // add color range
            combined = concatenate(combined, valueMeasuredMat); // add measured value

            //make bitmap to be rendered on screen, which doesn't contain the patch description
            if (!combined.empty()) {
                stripBitmap = makeBitmap(combined);
            }

            //add description of patch to combined mat, at the top
            combined = concatenate(descMat, combined);

            //make bitmap to be send to server
            if (!combined.empty()) {
                combinedBitmap = makeBitmap(combined);
                FileStorage.writeToSDFile(combinedBitmap);
            }

            //put ppm and image in resultJsonArr
            if (combinedBitmap != null) {
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
            // End making mats to put into image to be send back as an String to server

            return null;
        }

        /*
        * Puts the result on screen.
        * data is taken from the globals stripBitmap, ppm, colorDetected and unit variables
        */
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
                    if (colorDetected != null && !grouped) {
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
        }
    }

    private double[][] createInterpolTable(JSONArray colours){
        JSONArray patchColorValues;
        double ppmPatchValueStart,ppmPatchValueEnd;
        double[] pointStart;
        double[] pointEnd;
        double LInter, aInter, bInter, vInter;
        int INTERPOLNUM = 10;
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

            // add final point
            patchColorValues = colours.getJSONObject(colours.length() - 1).getJSONArray("lab");
            interpolTable[count][0] = patchColorValues.getDouble(0);
            interpolTable[count][1] = patchColorValues.getDouble(1);
            interpolTable[count][2] = patchColorValues.getDouble(2);
            interpolTable[count][3] = colours.getJSONObject(colours.length() - 1).getDouble("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return interpolTable;
    }

    private double calculatePpmSingle(double[] colorValues, JSONArray colours) throws Exception {
        double[][] interpolTable = createInterpolTable(colours);

        // determine closest value
        // create interpolation and extrapolation tables using linear approximation
        if (colorValues == null || colorValues.length < 3){
            throw new Exception("no valid lab colour data.");
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[] labPoint = new double[]{colorValues[0] / 2.55, colorValues[1] - 128, colorValues[2] - 128};

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

        return interpolTable[minPos][3];
    }

    private double calculatePpmGroup(double[][] colorsValueLab, List<StripTest.Brand.Patch>patches) throws Exception{
        double[][][] interpolTables = new double[patches.size()][][];

        // create all interpol tables
        for (int p = 0; p < patches.size(); p++){
            JSONArray colours = patches.get(p).getColours();

            // create interpol table for this patch
            interpolTables[p] = createInterpolTable(colours);

            if (colorsValueLab[p] == null || colorsValueLab[p].length < 3){
                throw new Exception("no valid lab colour data.");
            }
        }

        // normalise lab values to standard ranges L:0..100, a and b: -127 ... 128
        double[][] labPoint = new double[patches.size()][];
        for (int p = 0; p < patches.size(); p++) {
            labPoint[p] = new double[]{colorsValueLab[p][0] / 2.55, colorsValueLab[p][1] - 128, colorsValueLab[p][2] - 128};
        }

        double dist;
        int minPos = 0;
        double smallestE94Dist = Double.MAX_VALUE;

        // compute smallest distance, combining all interpolation tables as we want the global minimum
        // all interpol tables should have the same length here, so we use the length of the first one

        for (int j = 0; j < interpolTables[0].length; j++) {
            dist = 0;
            for (int p = 0; p < patches.size(); p++){
                dist += CalibrationCard.E94(labPoint[p][0],labPoint[p][1],
                    labPoint[p][2],interpolTables[p][j][0], interpolTables[p][j][1], interpolTables[p][j][2], false);
            }
            if (dist < smallestE94Dist){
                smallestE94Dist = dist;
                minPos = j;
            }
        }

        return interpolTables[0][minPos][3];
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
}
