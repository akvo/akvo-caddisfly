package org.akvo.akvoqr.opencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.akvo.akvoqr.App;
import org.akvo.akvoqr.AssetsManager;
import org.akvo.akvoqr.R;
import org.akvo.akvoqr.color.ColorDetected;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 8/19/15.
 */
public class StripTest {

    public static enum brand{
        HACH883738 };
    public static StripTest instance;
    private static JSONArray stripsJson;
    private static Map<String, JSONObject> stripObjects;

    public static StripTest getInstance()
    {
        if(instance==null)
        {
            instance = new StripTest();
            fromJson();
        }
        return instance;
    }

    public Brand getBrand(brand brand)
    {
        return new Brand(brand);
    }

    public class Brand
    {
        private double stripLenght;
        private double stripHeight;
        private List<String> patchDescList = new ArrayList<>();
        private List<Patch> patches = new ArrayList<>();
        private Map<String, JSONArray> ppmValues = new HashMap<>();

        public Brand(brand brandEnum) {

            if (stripObjects != null) {
                JSONObject strip = stripObjects.get(brandEnum.name());
                try {
                    this.stripLenght = strip.getDouble("length");
                    this.stripHeight = strip.getDouble("height");
                    JSONArray ppmVals = strip.getJSONArray("ppmVals");
                    JSONArray patchDesc = strip.getJSONArray("patchDesc");
                    for(int i=0;i<patchDesc.length();i++)
                    {
                        patchDescList.add(patchDesc.getString(i));
                    }
                    for (int i = 0; i < ppmVals.length(); i++) {
                        JSONArray ppms = ppmVals.getJSONArray(i);
                        if (patchDesc.get(i) != null)
                            ppmValues.put(patchDesc.getString(i), ppms);
                        else
                            ppmValues.put("no-name", ppms);
                    }
                    JSONArray patchPos = strip.getJSONArray("patchPos");
                    JSONArray patchWidth = strip.getJSONArray("patchWidth");

                    for (int i = 0; i < patchPos.length(); i++) {
                        patches.add(new Patch(i, patchWidth.getDouble(i), 0, patchPos.getDouble(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public List<Patch> getPatches() {
            return patches;
        }

        public double getStripHeight() {
            return stripHeight;
        }

        public double getStripLenght() {
            return stripLenght;
        }

        public List<String> getPatchDescList() {
            return patchDescList;
        }

        public Map<String, JSONArray> getPpmValues() {
            return ppmValues;
        }

        public class Patch {
            int order;
            double width; //mm
            double height;//mm
            double position;//x in mm

            public Patch(int order, double width, double height, double position) {
                this.order = order;
                this.width = width;
                this.height = height;
                this.position = position;
            }
        }
    }
    public static void fromJson()
    {
        String json = AssetsManager.getInstance().loadJSONFromAsset("strips.json");
        try {

            JSONObject object = new JSONObject(json);
            if(!object.isNull("strips"))
            {
                System.out.println(object.toString(2));
                stripsJson = object.getJSONArray("strips");
                if(stripsJson!=null) {
                    stripObjects = new HashMap<>();
                    for (int i = 0; i < stripsJson.length(); i++) {
                        JSONObject strip = stripsJson.getJSONObject(i);
                        String key = strip.getString("brand");
                        stripObjects.put(key, strip);
                    }
                }
            }
            else
            {
                System.out.println("***json object has no strips");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Get the mean Scalars from an image in res/drawable. The image contains color patches
     * that correspond to ppm values. This is supposed to be a temporary solution, until we find
     * reliable values for color. It gives the possiblity to test with different color schemes, e.g.
     * rgb and lab
     */
    public ArrayList getPPMColorsFromImage()
    {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        MatOfPoint2f mop2f = new MatOfPoint2f();
        ArrayList<ColorDetected> colors = new ArrayList<>();

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(App.getMyApplicationContext().getResources(), R.drawable.total_chlorine_cal);

            Mat free_chl = new Mat();

            Utils.bitmapToMat(bitmap, free_chl);

            Imgproc.medianBlur(free_chl, free_chl, 5);
            Mat gray = new Mat();
            Imgproc.cvtColor(free_chl, gray, Imgproc.COLOR_RGB2GRAY);

            Imgproc.Canny(gray,gray,40,120);
            Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

            System.out.println("***strip test chlorine colors: ");

            for (int x = 0; x < contours.size(); x++) {


                if(Imgproc.contourArea(contours.get(x))>1) {

                    contours.get(x).convertTo(mop2f, CvType.CV_32FC2);
                    Imgproc.approxPolyDP(mop2f, mop2f, 30, true);
                    mop2f.convertTo(contours.get(x), CvType.CV_32S);

                    if (contours.get(x).rows() > 2) {
                        RotatedRect rotatedRect = Imgproc.minAreaRect(mop2f);

                        Point[] points = new Point[4];
                        rotatedRect.points(points);
                        Point tl = new Point(Math.max(0,rotatedRect.center.x - 10), Math.max(0, rotatedRect.center.y - 10));
                        Point br = new Point(Math.min(free_chl.cols(), rotatedRect.center.x + 10), Math.min(free_chl.rows(), rotatedRect.center.y + 10));
                        Rect roi = new Rect(tl, br);
                        Mat submat = free_chl.submat(roi);

                        Scalar mean = Core.mean(submat);
                        System.out.println("***rgb: ");
                        System.out.println(Math.round(mean.val[0]) + ", " + Math.round(mean.val[1]) + ", " + Math.round(mean.val[2]));
                        ColorDetected colorDetected = new ColorDetected((int)points[0].x);

                        colorDetected.setRgb(mean);

                        int color = Color.rgb((int) Math.round(mean.val[0]),
                                (int) Math.round(mean.val[1]), (int) Math.round(mean.val[2]));
                        colorDetected.setColor(color);

//
                        Imgproc.cvtColor(submat, submat, Imgproc.COLOR_RGB2Lab);
                        mean = Core.mean(submat);

                        colorDetected.setLab(mean);

                        System.out.println("***lab: ");
                        System.out.println(Math.round(mean.val[0]) + ", " + Math.round(mean.val[1]) + ", " + Math.round(mean.val[2]));

                        colors.add(colorDetected);

                        Imgproc.rectangle(free_chl, points[0], points[2], new Scalar(0, 255, 0, 255), 1);

                      }
                }
            }

            return colors;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
