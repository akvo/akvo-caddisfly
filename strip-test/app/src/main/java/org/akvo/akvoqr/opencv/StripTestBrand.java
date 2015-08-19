package org.akvo.akvoqr.opencv;

import org.akvo.akvoqr.AssetsManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linda on 8/19/15.
 */
public class StripTestBrand {

    public static enum brand{
        HACH883738 };

    private JSONArray stripsJson;
    private Map<String, JSONObject> stripObjects;
    private double stripLenght;
    private double stripHeight;
    private List<Patch> patches = new ArrayList<>();


    public StripTestBrand(brand brandEnum)
    {
        fromJson();

        if(stripObjects!=null)
        {
            JSONObject strip = stripObjects.get(brandEnum.name());
            try {
                this.stripLenght = strip.getDouble("length");
                JSONArray patchPos = strip.getJSONArray("patchPos");
                JSONArray patchWidth = strip.getJSONArray("patchWidth");
                for(int i=0;i<patchPos.length();i++)
                {
                    patches.add(new Patch(i,patchWidth.getDouble(i), 0, patchPos.getDouble(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        switch (brandEnum)
//        {
//            case HACH883738:
//
//                this.stripHeight = 5; //mm
//                this.stripLenght = 83.1; //mm
//                patches.add(new Patch(0,8,8,3.3));
//                patches.add(new Patch(1,8,8,17.2));
//                break;
//        }
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

    public class Patch{
        int order;
        double width; //mm
        double height;//mm
        double position;//x in mm

        public Patch(int order, double width, double height, double position)
        {
            this.order = order;
            this.width = width;
            this.height = height;
            this.position = position;
        }
    }

    public void fromJson()
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
}
