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

package org.akvo.caddisfly.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestInfo implements Parcelable {

    public static final Creator<TestInfo> CREATOR = new Creator<TestInfo>() {
        @Override
        public TestInfo createFromParcel(Parcel in) {
            return new TestInfo(in);
        }

        @Override
        public TestInfo[] newArray(int size) {
            return new TestInfo[size];
        }
    };
    private final transient DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    private final transient DecimalFormat decimalFormat = new DecimalFormat("#.###", symbols);
    @SerializedName("reagentType")
    @Expose
    private String reagentType = "";
    @SerializedName("reagents")
    @Expose
    private List<Reagent> reagents = null;
    @SerializedName("isCategory")
    @Expose
    private boolean isCategory;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("subtype")
    @Expose
    private TestType subtype;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("brandUrl")
    @Expose
    private String brandUrl = "";
    @SerializedName("groupingType")
    @Expose
    private GroupType groupingType;
    @SerializedName("illuminant")
    @Expose
    private String illuminant;
    @SerializedName("length")
    @Expose
    private Double length;
    @SerializedName("height")
    @Expose
    private Double height;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("hasImage")
    @Expose
    private Boolean hasImage = false;
    @SerializedName("results")
    @Expose
    private List<Result> results = new ArrayList<>();
    @SerializedName("ranges")
    @Expose
    private String ranges;
    @SerializedName("monthsValid")
    @Expose
    private Integer monthsValid;
    @SerializedName("md610_id")
    @Expose
    private String md610Id;
    @SerializedName("sampleQuantity")
    @Expose
    private String sampleQuantity;
    @SerializedName("selectInstruction")
    @Expose
    private String selectInstruction;
    @SerializedName("endInstruction")
    @Expose
    private String endInstruction;
    @SerializedName("hasEndInstruction")
    @Expose
    private Boolean hasEndInstruction;
    @SerializedName("instructions")
    @Expose
    private List<Instruction> instructions = null;
    @SerializedName("instructions2")
    @Expose
    private List<Instruction> instructions2 = null;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("numPatch")
    @Expose
    private Integer numPatch;
    @SerializedName("deviceId")
    @Expose
    private String deviceId;
    @SerializedName("responseFormat")
    @Expose
    private String responseFormat;
    @SerializedName("imageScale")
    @Expose
    private String imageScale;

    public TestInfo() {
    }

    public TestInfo(String categoryName) {
        category = categoryName;
        isCategory = true;
    }

    private TestInfo(Parcel in) {
        isCategory = in.readByte() != 0;
        category = in.readString();
        name = in.readString();
        subtype = TestType.valueOf(in.readString());
        description = in.readString();
        tags = in.createStringArrayList();
        reagentType = in.readString();
        reagents = new ArrayList<>();
        in.readTypedList(reagents, Reagent.CREATOR);
        uuid = in.readString();

        brand = in.readString();
        brandUrl = in.readString();
        String tmpGroupingType = in.readString();
        if (tmpGroupingType != null && !tmpGroupingType.equalsIgnoreCase("null")) {
            groupingType = GroupType.valueOf(tmpGroupingType);
        }
        illuminant = in.readString();
        if (in.readByte() == 0) {
            length = 0.0;
        } else {
            length = in.readDouble();
        }
        if (in.readByte() == 0) {
            height = 0.0;
        } else {
            height = in.readDouble();
        }
        unit = in.readString();
        byte tmpHasImage = in.readByte();
        hasImage = tmpHasImage == 1;
        ranges = in.readString();
        if (in.readByte() == 0) {
            monthsValid = null;
        } else {
            monthsValid = in.readInt();
        }
        md610Id = in.readString();
        sampleQuantity = in.readString();

        results = new ArrayList<>();
        in.readTypedList(results, Result.CREATOR);

        instructions = new ArrayList<>();
        in.readTypedList(instructions, Instruction.CREATOR);
        instructions2 = new ArrayList<>();
        in.readTypedList(instructions2, Instruction.CREATOR);

        selectInstruction = in.readString();
        endInstruction = in.readString();
        byte tmpHasEndInstruction = in.readByte();
        hasEndInstruction = tmpHasEndInstruction == 1;
        image = in.readString();
        imageScale = in.readString();
        if (in.readByte() == 0) {
            numPatch = null;
        } else {
            numPatch = in.readInt();
        }
        deviceId = in.readString();
        responseFormat = in.readString();
    }

    public boolean getIsGroup() {
        return isCategory;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestType getSubtype() {
        return subtype;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBrand() {
        return brand;
    }

    public String getRanges() {
        return ranges;
    }

    public double getMinRangeValue() {
        String[] array = ranges.split(",");

        try {
            return Double.valueOf(array[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public double getMaxRangeValue() {
        String[] array = ranges.split(",");

        try {
            return Double.valueOf(array[array.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getMinMaxRange() {

        if (results != null && results.size() > 0) {
            StringBuilder minMaxRange = new StringBuilder();
            for (Result result : results) {
                if (result.getColors() != null && result.getColors().size() > 0) {
                    int valueCount = result.getColors().size();
                    if (minMaxRange.length() > 0) {
                        minMaxRange.append(", ");
                    }
                    if (result.getColors().size() > 0) {
                        minMaxRange.append(String.format(Locale.US, "%s - %s",
                                decimalFormat.format(result.getColors().get(0).getValue()),
                                decimalFormat.format(result.getColors().get(valueCount - 1).getValue())));
                    }
                    if (groupingType == GroupType.GROUP) {
                        break;
                    }
                } else {
                    if (ranges != null) {
                        String[] rangeArray = ranges.split(",");
                        if (rangeArray.length > 1) {
                            return rangeArray[0].trim() + " - " + rangeArray[rangeArray.length - 1].trim();
                        } else {
                            return "";
                        }
                    } else {
                        return "";
                    }
                }
            }

            return minMaxRange.toString();
        }
        return "";
    }


    public String getMd610Id() {
        return md610Id;
    }

    public String getBrandUrl() {
        return brandUrl;
    }

    public GroupType getGroupingType() {
        return groupingType;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getSampleQuantity() {
        return sampleQuantity;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Instruction> getInstructions2() {
        return instructions2;
    }

    public String getImage() {
        return image;
    }

    public String getImageScale() {
        return imageScale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isCategory ? 1 : 0));
        parcel.writeString(category);
        parcel.writeString(name);
        parcel.writeString(subtype.name());
        parcel.writeString(description);
        parcel.writeStringList(tags);
        parcel.writeString(reagentType);
        parcel.writeTypedList(reagents);
        parcel.writeString(uuid);
        parcel.writeString(brand);
        parcel.writeString(brandUrl);
        parcel.writeString(String.valueOf(groupingType));
        parcel.writeString(illuminant);
        if (length == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(length);
        }
        if (height == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(height);
        }
        parcel.writeString(unit);
        parcel.writeByte((byte) (hasImage == null ? 0 : hasImage ? 1 : 2));
        parcel.writeString(ranges);
        if (monthsValid == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(monthsValid);
        }
        parcel.writeString(md610Id);
        parcel.writeString(sampleQuantity);
        parcel.writeTypedList(results);
        parcel.writeTypedList(instructions);
        parcel.writeTypedList(instructions2);
        parcel.writeString(selectInstruction);
        parcel.writeString(endInstruction);
        parcel.writeByte((byte) (hasEndInstruction == null ? 0 : hasEndInstruction ? 1 : 2));
        parcel.writeString(image);
        parcel.writeString(imageScale);
        if (numPatch == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(numPatch);
        }
        parcel.writeString(deviceId);
        parcel.writeString(responseFormat);
    }

    public Reagent getReagent(int i) {
        if (reagents != null && reagents.size() > i) {
            return reagents.get(i);
        } else {
            return new Reagent();
        }
    }

    public List<Result> getResults() {
        return results;
    }

    public String getSelectInstruction() {
        return selectInstruction;
    }

    public String getEndInstruction() {
        return endInstruction;
    }

    public double getStripLength() {
        return length;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public Boolean getHasImage() {
        return hasImage;
    }

    public String getReagentType() {
        return reagentType;
    }

}