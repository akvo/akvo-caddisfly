package org.akvo.akvoqr.util.detector;

import org.akvo.akvoqr.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by linda on 9/13/15.
 */
public class FinderPatternInfoToJson {

    public static String toJson(FinderPatternInfo info)
    {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray topleft = new JSONArray();
            topleft.put(info.getTopLeft().getX());
            topleft.put(info.getTopLeft().getY());
            jsonObject.put(Constant.TOPLEFT, topleft);

            JSONArray topright = new JSONArray();
            topright.put(info.getTopRight().getX());
            topright.put(info.getTopRight().getY());
            jsonObject.put(Constant.TOPRIGHT, topright);

            JSONArray bottomleft = new JSONArray();
            bottomleft.put(info.getBottomLeft().getX());
            bottomleft.put(info.getBottomLeft().getY());
            jsonObject.put(Constant.BOTTOMLEFT, bottomleft);

            JSONArray bottomright = new JSONArray();
            bottomright.put(info.getBottomRight().getX());
            bottomright.put(info.getBottomRight().getY());
            jsonObject.put(Constant.BOTTOMRIGHT, bottomright);

            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}
