/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.colorimetry.stripv2.models;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Holds information about the test.
 */
public class StripTest {

    private static final String TESTS = "tests";

    private static JSONArray stripTests = null;

    public StripTest() {
    }

    private static void clearStripTests() {
        StripTest.stripTests = null;
    }

    private JSONArray getTestsFromJson() {
        if (stripTests == null || stripTests.length() == 0) {

            try {
                JSONObject object = new JSONObject(AssetsManager.getInstance().loadJSONFromAsset(SensorConstants.TESTS_META_FILENAME));
                if (!object.isNull(TESTS)) {
                    stripTests = object.getJSONArray(TESTS);

                    try {
                        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG),
                                SensorConstants.TESTS_META_FILENAME);
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
                Timber.e(e);
            }
        } else {
            return stripTests;
        }

        return null;
    }

    public List<Brand> getBrandsAsList() {

        StripTest.clearStripTests();
        List<Brand> brandNames = new ArrayList<>();

        try {
            JSONArray stripsJson = getTestsFromJson();

            JSONObject strip;
            if (stripsJson != null) {
                for (int i = 0; i < stripsJson.length(); i++) {
                    strip = stripsJson.getJSONObject(i);

                    //Only show experimental tests if in diagnostic mode
                    if (!AppPreferences.isDiagnosticMode()
                            && (strip.has("experimental") && strip.getBoolean("experimental"))) {
                        continue;
                    }

                    brandNames.add(getBrand(strip.getString(SensorConstants.UUID)));
                }
            }

        } catch (Exception e) {
            return null;
        }
        return brandNames;
    }

    public Brand getBrand(String uuid) {
        return new Brand(uuid);
    }

    public int getPatchCount(String uuid) {
        return getBrand(uuid).getPatches().size();
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

        Brand(String uuid) {

            this.uuid = uuid;
            try {

                //add instructions
                JSONArray stripsJson = getTestsFromJson();
                if (stripsJson != null) {

                    JSONObject strip;

                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString(SensorConstants.UUID).equalsIgnoreCase(uuid)) {
                            try {
                                stripLength = strip.getDouble("length");
                                //stripHeight = strip.getDouble("height");
                                groupingType = strip.getString("groupingType")
                                        .equals(GroupType.GROUP.toString()) ? GroupType.GROUP : GroupType.INDIVIDUAL;

                                name = strip.getString("name");

                                brandDescription = strip.getString("brand");
                                image = strip.has(SensorConstants.IMAGE)
                                        ? strip.getString(SensorConstants.IMAGE) : brandDescription.replace(" ", "-");
                                imageScale = strip.has("imageScale") ? strip.getString("imageScale") : "";

                                if (strip.has("instructions")) {
                                    instructions = strip.getJSONArray("instructions");
                                }

                                JSONArray patchesArray = strip.getJSONArray("results");
                                for (int ii = 0; ii < patchesArray.length(); ii++) {

                                    JSONObject patchObj = patchesArray.getJSONObject(ii);

                                    int id = patchObj.getInt("id");
                                    String patchName = patchObj.getString("name");
                                    String unit = patchObj.has("unit") ? patchObj.getString("unit") : "";
                                    String formula = patchObj.has("formula") ? patchObj.getString("formula") : "";
                                    double patchPos = patchObj.has("patchPos") ? patchObj.getDouble("patchPos") : 0;
                                    int patchWidth = patchObj.has("patchWidth") ? patchObj.getInt("patchWidth") : 0;
                                    double timeDelay = patchObj.has("timeDelay") ? patchObj.getDouble("timeDelay") : 0;
                                    JSONArray colors = patchObj.has("colors") ? patchObj.getJSONArray("colors") : new JSONArray();
                                    int phase = patchObj.has("phase") ? patchObj.getInt("phase") : 1;

                                    patches.add(new Patch(id, patchName, patchWidth, 0, patchPos,
                                            timeDelay, unit, formula, colors, phase));
                                }

                                switch (groupingType) {

                                    case GROUP:
                                        // sort by position of the patches on the strip
                                        Collections.sort(patches, (lhs, rhs) -> Double.compare(lhs.getPosition(), rhs.getPosition()));
                                        break;
                                    case INDIVIDUAL:
                                    default:
                                        // sort by time delay for analyzing each patch
                                        Collections.sort(patches, (lhs, rhs) -> Double.compare(lhs.timeLapse, rhs.timeLapse));
                                        break;
                                }

                            } catch (JSONException e) {
                                Timber.e(e);
                            }
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        public List<Patch> getPatches() {
            return patches;
        }

        public List<Patch> getPatchesSortedByPosition() {

            // sort after moving to new list so that the order of original list is not changed
            List<Patch> newList = new ArrayList<>(patches);
            Collections.sort(newList, (lhs, rhs) -> Double.compare(lhs.getPosition(), rhs.getPosition()));
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
            private final int phase;
            private final String formula;

            @SuppressWarnings("SameParameterValue")
            Patch(int id, String desc, double width, double height, double position,
                  double timeLapse, String unit, String formula, JSONArray colors, int phase) {
                this.id = id;
                this.desc = desc;
                this.width = width;
                this.height = height;
                this.position = position;
                this.timeLapse = timeLapse;
                this.unit = unit;
                this.colors = colors;
                this.phase = phase;
                this.formula = formula;
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
                return timeLapse;
            }

            public String getUnit() {
                return unit;
            }

            public double getWidth() {
                return width;
            }

            public int getPhase() {
                return phase;
            }

            public String getFormula() {
                return formula;
            }
        }
    }
}
