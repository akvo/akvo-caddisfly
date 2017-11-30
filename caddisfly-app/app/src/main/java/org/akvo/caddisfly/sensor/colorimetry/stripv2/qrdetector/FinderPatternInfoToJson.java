package org.akvo.caddisfly.sensor.colorimetry.stripv2.qrdetector;

import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by linda on 9/13/15
 */
public class FinderPatternInfoToJson {

    public static String toJson(FinderPatternInfo info) {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray topLeft = new JSONArray();
            topLeft.put(info.getTopLeft().getX());
            topLeft.put(info.getTopLeft().getY());
            jsonObject.put(Constants.TOP_LEFT, topLeft);

            JSONArray topRight = new JSONArray();
            topRight.put(info.getTopRight().getX());
            topRight.put(info.getTopRight().getY());
            jsonObject.put(Constants.TOP_RIGHT, topRight);

            JSONArray bottomLeft = new JSONArray();
            bottomLeft.put(info.getBottomLeft().getX());
            bottomLeft.put(info.getBottomLeft().getY());
            jsonObject.put(Constants.BOTTOM_LEFT, bottomLeft);

            JSONArray bottomRight = new JSONArray();
            bottomRight.put(info.getBottomRight().getX());
            bottomRight.put(info.getBottomRight().getY());
            jsonObject.put(Constants.BOTTOM_RIGHT, bottomRight);

            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}
