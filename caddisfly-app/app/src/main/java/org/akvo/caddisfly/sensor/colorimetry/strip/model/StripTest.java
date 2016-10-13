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

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
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

    public StripTest() {

    }

    public List<Brand> getBrandsAsList() {
        List<Brand> brandNames = new ArrayList<>();

        String json = fromJson();
        try {
            JSONObject object = new JSONObject(json);

            if (!object.isNull("strips")) {
                JSONArray stripsJson = object.getJSONArray("strips");
                JSONObject strip;
                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        brandNames.add(getBrand(strip.getString("uuid")));
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return brandNames;
    }

    /*
    * Matches a test UUID to a brand name
    * if not found, returns null
     */
    public String matchUuidToBrand(String uuid) {
        String json = fromJson();
        String result = null;
        try {
            JSONObject object = new JSONObject(json);

            if (!object.isNull("strips")) {
                JSONArray stripsJson = object.getJSONArray("strips");
                JSONObject strip;
                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString("uuid").equals(uuid)) {
                            result = strip.getString("brand");
                        }
                    }
                }
            }
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    public Brand getBrand(String uuid) {
        return new Brand(uuid);
    }

    private String fromJson() {
        String filename = CaddisflyApp.getApp().getApplicationContext().getString(R.string.strips_json);

        return AssetsManager.getInstance().loadJSONFromAsset(filename);

    }

    private String instructionsFromJson() {
        String filename = CaddisflyApp.getApp().getApplicationContext().getString(R.string.strips_instruction_json);
        return AssetsManager.getInstance().loadJSONFromAsset(filename);
    }

    public enum GroupType {GROUP, INDIVIDUAL}

    public class Brand {
        private final List<Patch> patches = new ArrayList<>();
        private String name;
        private String brandDescription;
        private String image;

        private String uuid;
        private double stripLength;
        @SuppressWarnings("unused")
        private double stripHeight;
        private GroupType groupingType;
        private JSONArray instructions;

        Brand(String uuid) {

            this.uuid = uuid;
            try {
                // read the json file with strip information from assets
                String json = fromJson();
                JSONObject object = new JSONObject(json);

                if (!object.isNull("strips")) {
                    JSONArray stripsJson = object.getJSONArray("strips");
                    JSONObject strip;

                    if (stripsJson != null) {
                        for (int i = 0; i < stripsJson.length(); i++) {
                            strip = stripsJson.getJSONObject(i);

                            if (strip.getString("uuid").equalsIgnoreCase(uuid)) {
                                try {
                                    stripLength = strip.getDouble("length");
                                    stripHeight = strip.getDouble("height");
                                    groupingType = strip.getString("groupingType")
                                            .equals(GroupType.GROUP.toString()) ? GroupType.GROUP : GroupType.INDIVIDUAL;
                                    name = strip.getString("name");
                                    brandDescription = strip.getString("brand");
                                    image = strip.has("image") ? strip.getString("image") : brandDescription;

                                    JSONArray patchesArr = strip.getJSONArray("patches");
                                    for (int ii = 0; ii < patchesArr.length(); ii++) {

                                        JSONObject patchObj = patchesArr.getJSONObject(ii);

                                        String patchDesc = patchObj.getString("patchDesc");
                                        int patchPos = patchObj.getInt("patchPos");
                                        int id = patchObj.getInt("id");
                                        int patchWidth = patchObj.getInt("patchWidth");
                                        double timeLapse = patchObj.getDouble("timeLapse");
                                        String unit = patchObj.getString("unit");
                                        JSONArray colours = patchObj.getJSONArray("colours");

                                        patches.add(new Patch(id, patchDesc, patchWidth, 0, patchPos, timeLapse, unit, colours));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                //add instructions
                String instructions = instructionsFromJson();
                JSONObject instructionObj = new JSONObject(instructions);
                JSONArray stripsJson = instructionObj.getJSONArray("strips");
                JSONObject strip;

                if (stripsJson != null) {
                    for (int i = 0; i < stripsJson.length(); i++) {
                        strip = stripsJson.getJSONObject(i);
                        if (strip.getString("uuid").equalsIgnoreCase(uuid)) {
                            this.instructions = strip.getJSONArray("instructions");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<Patch> getPatches() {
            return patches;
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


        public List<Patch> getPatchesOrderedByTimeLapse() {
            Collections.sort(patches, new PatchComparator());
            return patches;
        }

        public String getImage() {
            return image;
        }

        public class Patch {
            final int id;
            final String desc;
            final double width; //mm
            @SuppressWarnings("unused")
            final double height;//mm
            final double position;//x in mm
            final double timeLapse; //seconds between this and previous patch
            final String unit;

            final JSONArray colours;

            @SuppressWarnings("SameParameterValue")
            Patch(int id, String desc, double width, double height, double position,
                  double timeLapse, String unit, JSONArray colours) {
                this.id = id;
                this.desc = desc;
                this.width = width;
                this.height = height;
                this.position = position;
                this.timeLapse = timeLapse;
                this.unit = unit;
                this.colours = colours;
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

            public JSONArray getColours() {
                return colours;
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
        }
    }

    private class PatchComparator implements Comparator<Brand.Patch> {

        @Override
        public int compare(Brand.Patch lhs, Brand.Patch rhs) {
            if (lhs.timeLapse < rhs.timeLapse)
                return -1;
            if (lhs.timeLapse == rhs.timeLapse)
                return 0;

            return 1;
        }
    }
}