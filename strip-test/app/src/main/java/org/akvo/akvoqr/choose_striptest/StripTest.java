package org.akvo.akvoqr.choose_striptest;

import android.content.Context;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.AssetsManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 8/19/15.
 */
public class StripTest{

    public static StripTest instance;
    private static Map<String, JSONObject> stripObjects;

    private StripTest()
    {

    }
    public static StripTest getInstance(Context context)
    {
        if(instance==null)
        {
            instance = new StripTest();
            fromJson(context);
        }

       return instance;
    }

    public List<String> getBrandsAsList()
    {
       List<String> brandnames = new ArrayList<>();
        for(String b: stripObjects.keySet()){
            brandnames.add(b);
        }
       return brandnames;
    }

    public Brand getBrand(String brand)
    {
        return new Brand(brand);
    }

    public class Brand
    {
        private String name;
        private double stripLenght;
        private double stripHeight;
        private List<Patch> patches = new ArrayList<>();
        private JSONArray instructions;

        public Brand(String brand) {

            if (stripObjects != null) {
                JSONObject strip = stripObjects.get(brand);
                if(strip!=null) {
                    try {
                        this.stripLenght = strip.getDouble("length");
                        this.stripHeight = strip.getDouble("height");
                        this.name = strip.getString("name");
                        this.instructions = strip.getJSONArray("instructions");

                        JSONArray patchesArr = strip.getJSONArray("patches");
                        for(int i=0;i<patchesArr.length();i++) {
                            JSONObject patchObj = patchesArr.getJSONObject(i);
                            String patchDesc = patchObj.getString("patchDesc");
                            int patchPos = patchObj.getInt("patchPos");
                            int patchWidth = patchObj.getInt("patchWidth");
                            double timeLapse = patchObj.getDouble("timeLapse");
                            String unit = patchObj.getString("unit");
                            JSONArray colours = patchObj.getJSONArray("colours");

                            patches.add(new Patch(i, patchDesc, patchWidth, 0, patchPos, timeLapse, unit, colours));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

        public String getName() {
            return name;
        }

        public JSONArray getInstructions() {
            return instructions;
        }

        public boolean hasTimeLapse()
        {
            double totalTime = 0;
            for(Patch patch: patches)
            {
                totalTime += patch.timeLapse;
            }

            return totalTime>0;
        }

        public double getDuration()
        {
            return patches.get(patches.size()-1).getTimeLapse();
        }

        public int getNumberOfPicturesNeeded()
        {
            int number = 1;
            for(int i=0;i<patches.size()-1;i++)
            {
                //compare next patch with current
                //if time lapse is larger, add one to number
                if(patches.get(i+1).getTimeLapse() > patches.get(i).getTimeLapse())
                {
                    number ++;
                }
            }

            return number;
        }

        public List<Patch> getPatchesOrderedByTimelapse()
        {
            Collections.sort(patches, new PatchComparator());
            return patches;
        }

        public class Patch {
            int order;
            String desc;
            double width; //mm
            double height;//mm
            double position;//x in mm
            double timeLapse; //seconds between this and previous patch
            String unit;
            JSONArray colours;

            public Patch(int order, String desc, double width, double height, double position,
                         double timeLapse, String unit, JSONArray colours) {
                this.order = order;
                this.desc = desc;
                this.width = width;
                this.height = height;
                this.position = position;
                this.timeLapse = timeLapse;
                this.unit = unit;
                this.colours = colours;
            }

            public int getOrder() {
                return order;
            }

            public String getDesc() {
                return desc;
            }

            public double getPosition() {
                return position;
            }

            public JSONArray getColours() {
                return colours;
            }

            public double getTimeLapse() {
                return timeLapse;
            }

            public String getUnit() {
                return unit;
            }

        }
    }

    private static void fromJson(Context context)
    {
        String filename = context.getString(R.string.strips_json);
        JSONArray stripsJson;

        String json = AssetsManager.getInstance(context).loadJSONFromAsset(filename);
        try {

            JSONObject object = new JSONObject(json);
            if(!object.isNull("strips"))
            {
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

    private class PatchComparator implements Comparator<Brand.Patch>
    {

        @Override
        public int compare(Brand.Patch lhs, Brand.Patch rhs) {
            if (lhs.timeLapse < rhs.timeLapse)
                return -1;
            if(lhs.timeLapse == rhs.timeLapse)
                return 0;

            return 1;
        }
    }


}
