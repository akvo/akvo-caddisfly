/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.model;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 8/19/15
 */
public class StripTest {

    private static final String TAG = "StripTest";

    private static final String STRIPS = "strips";

    public StripTest() {
    }

    public List<Brand> getBrandsAsList(Context context) {
        List<Brand> brandNames = new ArrayList<>();

        try {
            JSONObject object = getJsonFromAssets(context, R.string.strips_json);
            if (!object.isNull(STRIPS)) {
                JSONArray stripsJson = object.getJSONArray(STRIPS);
                JSONObject strip;
                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);

                        //Only show experimental tests if in diagnostic mode
                        if (!AppPreferences.isDiagnosticMode()
                                && (strip.has("experimental") && strip.getBoolean("experimental"))) {
                            continue;
                        }

                        brandNames.add(getBrand(context, strip.getString("uuid")));
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return brandNames;
    }

    public Brand getBrand(Context context, String uuid) {
        return new Brand(context, uuid);
    }

    private JSONObject getJsonFromAssets(Context context, @StringRes int id) throws JSONException {
        String filename = context.getString(id);
        String jsonString = AssetsManager.getInstance().loadJSONFromAsset(filename);
        return new JSONObject(jsonString);
    }

    public int getPatchCount(Context context, String uuid) {
        return getBrand(context, uuid).getPatches().size();
    }

    public enum GroupType {
        GROUP, INDIVIDUAL
    }

    public class Brand {
        private final List<Patch> patches = new ArrayList<>();
        private final String uuid;
        private String name;
        private String brandDescription;
        private String image;
        private String imageScale;
        private double stripLength;
        //private double stripHeight;
        private GroupType groupingType;
        private JSONArray instructions;

        Brand(Context context, String uuid) {

            this.uuid = uuid;
            try {
                // read the json file with strip information from assets
                JSONObject object = getJsonFromAssets(context, R.string.strips_json);

                if (!object.isNull(STRIPS)) {
                    JSONArray stripsJson = object.getJSONArray(STRIPS);
                    JSONObject strip;

                    if (stripsJson != null) {
                        for (int i = 0; i < stripsJson.length(); i++) {
                            strip = stripsJson.getJSONObject(i);

                            if (strip.getString("uuid").equalsIgnoreCase(uuid)) {
                                try {
                                    stripLength = strip.getDouble("length");
                                    //stripHeight = strip.getDouble("height");
                                    groupingType = strip.getString("groupingType")
                                            .equals(GroupType.GROUP.toString()) ? GroupType.GROUP : GroupType.INDIVIDUAL;
                                    name = strip.getString("name");
                                    brandDescription = strip.getString("brand");
                                    image = strip.has("image") ? strip.getString("image") : brandDescription.replace(" ", "-");
                                    imageScale = strip.has("imageScale") ? strip.getString("imageScale") : "";

                                    JSONArray patchesArray = strip.getJSONArray("patches");
                                    for (int ii = 0; ii < patchesArray.length(); ii++) {

                                        JSONObject patchObj = patchesArray.getJSONObject(ii);

                                        String patchDesc = patchObj.getString("patchDesc");
                                        double patchPos = patchObj.getDouble("patchPos");
                                        int id = patchObj.getInt("id");
                                        int patchWidth = patchObj.getInt("patchWidth");
                                        double timeLapse = patchObj.getDouble("timeLapse");
                                        String unit = patchObj.getString("unit");
                                        JSONArray colors = patchObj.getJSONArray("colours");

                                        patches.add(new Patch(id, patchDesc, patchWidth, 0, patchPos, timeLapse, unit, colors));
                                    }

                                    switch (groupingType) {

                                        case GROUP:
                                            // sort by position of the patches on the strip
                                            Collections.sort(patches, new Comparator<Patch>() {
                                                @Override
                                                public int compare(Patch lhs, Patch rhs) {
                                                    return Double.compare(lhs.getPosition(), rhs.getPosition());
                                                }
                                            });
                                            break;
                                        case INDIVIDUAL:
                                            // sort by time delay for analyzing each patch
                                            Collections.sort(patches, new Comparator<Patch>() {
                                                @Override
                                                public int compare(final Patch lhs, final Patch rhs) {
                                                    return Double.compare(lhs.timeLapse, rhs.timeLapse);
                                                }
                                            });
                                            break;
                                    }

                                } catch (JSONException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                                break;
                            }
                        }
                    }
                }

                //add instructions
                JSONObject instructionObj = getJsonFromAssets(context, R.string.strips_instruction_json);
                JSONArray stripsJson = instructionObj.getJSONArray(STRIPS);
                JSONObject strip;

                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString("uuid").equalsIgnoreCase(uuid)) {
                            this.instructions = strip.getJSONArray("instructions");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public List<Patch> getPatches() {
            return patches;
        }

        public List<Patch> getPatchesSortedByPosition() {

            // sort after moving to new list so that the order of original list is not changed
            List<Patch> newList = new ArrayList<>(patches);
            Collections.sort(newList, new Comparator<Patch>() {
                @Override
                public int compare(Patch lhs, Patch rhs) {
                    return Double.compare(lhs.getPosition(), rhs.getPosition());
                }
            });
            return newList;
        }

//        public double getStripHeight() {
//            return stripHeight;
//        }

        public GroupType getGroupingType() {
            return groupingType;
        }

        public double getStripLength() {
            return stripLength;
        }

        public String getName() {
            return name;
        }

        public String getBrandDescription() {
            return brandDescription;
        }

        public String getUuid() {
            return uuid;
        }

        public JSONArray getInstructions() {
            return instructions;
        }

        public String getImage() {
            return image;
        }

        public String getImageScale() {
            return imageScale;
        }

        public class Patch {
            private final int id;
            private final String desc;
            private final double width; //mm
            @SuppressWarnings("unused")
            private final double height; //mm
            private final double position; //x in mm
            private final double timeLapse; //seconds between this and previous patch
            private final String unit;

            private final JSONArray colors;

            @SuppressWarnings("SameParameterValue")
            Patch(int id, String desc, double width, double height, double position,
                  double timeLapse, String unit, JSONArray colors) {
                this.id = id;
                this.desc = desc;
                this.width = width;
                this.height = height;
                this.position = position;
                this.timeLapse = timeLapse;
                this.unit = unit;
                this.colors = colors;
            }

            public int getId() {
                return id;
            }

            public String getDesc() {
                return desc;
            }

            public double getPosition() {
                return position;
            }

            public JSONArray getColors() {
                return colors;
            }

            public double getTimeLapse() {
                if (AppPreferences.ignoreStripTestDelay()) {
                    return 3;
                } else {
                    return timeLapse;
                }
            }

            public String getUnit() {
                return unit;
            }

            public double getWidth() {
                return width;
            }
        }
    }
}
