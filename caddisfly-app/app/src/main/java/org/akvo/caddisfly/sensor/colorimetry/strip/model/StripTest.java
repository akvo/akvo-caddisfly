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
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda on 8/19/15
 */
public class StripTest {

    private static final String TAG = "StripTest";

    private static final String TESTS = "tests";

    private static JSONArray stripTests = null;

    public StripTest() {
    }

    private JSONArray getTestsFromJson(Context context) {
        if (stripTests == null || stripTests.length() == 0) {

            try {
                JSONObject object = new JSONObject(AssetsManager.getInstance().loadJSONFromAsset("tests_config.json"));
                //JSONObject object = getJsonFromAssets(context, "tests_config.json");
                if (!object.isNull(TESTS)) {
                    stripTests = object.getJSONArray(TESTS);

                    try {
                        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), "strip-tests.json");
                        if (file.exists()) {
                            String jsonText = FileUtil.loadTextFromFile(file);
                            JSONObject customTests = new JSONObject(jsonText);
                            JSONArray customTestsArray = customTests.getJSONArray(TESTS);

                            boolean isUnique = true;
                            for (int i = 0; i < customTestsArray.length(); i++) {

                                String uuid = customTestsArray.getJSONObject(i).getString(SensorConstants.UUID);
                                for (int j = 0; j < stripTests.length(); j++) {
                                    if (stripTests.getJSONObject(j).getString(SensorConstants.UUID).equalsIgnoreCase(uuid)) {
                                        isUnique = false;
                                        break;
                                    }
                                }

                                // only add the custom test if it has an unique uuid and not a duplicate
                                if (isUnique) {
                                    JSONObject test = new JSONObject(customTestsArray.getJSONObject(i).toString());
                                    String brandDescription = test.getString("brand");
                                    String image = test.has(SensorConstants.IMAGE)
                                            ? test.getString(SensorConstants.IMAGE) : brandDescription.replace(" ", "-");
                                    if (image.isEmpty()) {
                                        image = brandDescription.replace(" ", "-");
                                    }
                                    File imageFile = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), "image/" + image);
                                    if (imageFile.exists()) {
                                        test.put(SensorConstants.IMAGE, imageFile.getPath());
                                    }
                                    test.put("customTest", true);
                                    stripTests.put(test);
                                }
                            }
                        }
                    } catch (JSONException ignored) {
                        // skip trying to load custom tests
                    }

                    for (int i = stripTests.length() - 1; i >= 0; i--) {
                        JSONObject strip = stripTests.getJSONObject(i);
                        String subtype = strip.getString("subtype");
                        if (!subtype.equals("striptest")) {
                            stripTests.remove(i);
                        }
                    }

                    return stripTests;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            return stripTests;
        }

        return null;
    }

    public List<Brand> getBrandsAsList(Context context) {

        stripTests = null;
        List<Brand> brandNames = new ArrayList<>();

        try {
            JSONArray stripsJson = getTestsFromJson(context);

            JSONObject strip;
            if (stripsJson != null) {
                for (int i = 0; i < stripsJson.length(); i++) {
                    strip = stripsJson.getJSONObject(i);

                    //Only show experimental tests if in diagnostic mode
                    if (!AppPreferences.isDiagnosticMode()
                            && (strip.has("experimental") && strip.getBoolean("experimental"))) {
                        continue;
                    }

                    brandNames.add(getBrand(context, strip.getString(SensorConstants.UUID)));
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

                //add instructions
                JSONArray stripsJson = getTestsFromJson(context);
                if (stripsJson != null) {

                    JSONObject strip;

                    JSONObject instructionObj = getJsonFromAssets(context, R.string.strips_instruction_json);
                    JSONArray instructionsJson = instructionObj.getJSONArray(TESTS);

                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString(SensorConstants.UUID).equalsIgnoreCase(uuid)) {
                            try {
                                stripLength = strip.getDouble("length");
                                //stripHeight = strip.getDouble("height");
                                groupingType = strip.getString("groupingType")
                                        .equals(GroupType.GROUP.toString()) ? GroupType.GROUP : GroupType.INDIVIDUAL;

                                //Get the name for this test
                                JSONArray nameArray = strip.getJSONArray("name");

                                name = nameArray.getJSONObject(0).getString("en");

                                brandDescription = strip.getString("brand");
                                image = strip.has(SensorConstants.IMAGE)
                                        ? strip.getString(SensorConstants.IMAGE) : brandDescription.replace(" ", "-");
                                imageScale = strip.has("imageScale") ? strip.getString("imageScale") : "";

                                if (strip.has("instructions")) {
                                    instructions = strip.getJSONArray("instructions");
                                } else {
                                    if (instructionsJson != null) {
                                        for (int j = 0; j < instructionsJson.length(); j++) {
                                            JSONObject instructionObject = instructionsJson.getJSONObject(j);
                                            if (instructionObject.getString(SensorConstants.UUID).equalsIgnoreCase(uuid)) {
                                                instructions = instructionObject.getJSONArray("instructions");
                                                break;
                                            }
                                        }
                                    }
                                }

                                JSONArray patchesArray = strip.getJSONArray("results");
                                for (int ii = 0; ii < patchesArray.length(); ii++) {

                                    JSONObject patchObj = patchesArray.getJSONObject(ii);

                                    String patchName = patchObj.getString("name");
                                    double patchPos = patchObj.getDouble("patchPos");
                                    int id = patchObj.getInt("id");
                                    int patchWidth = patchObj.getInt("patchWidth");
                                    double timeLapse = patchObj.getDouble("timeLapse");
                                    String unit = patchObj.getString("unit");
                                    JSONArray colors;
                                    if (patchObj.has("colours")) {
                                        colors = patchObj.getJSONArray("colours");
                                    } else {
                                        colors = patchObj.getJSONArray("colors");
                                    }

                                    patches.add(new Patch(id, patchName, patchWidth, 0, patchPos, timeLapse, unit, colors));
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
