package org.akvo.caddisfly.sensor.colorimetry.stripv2.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.PatchResult;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest.Brand.Patch;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by markwestra on 03/08/2017
 */
public class ResultUtils {
    public static final int INTERPOLATION_NUMBER = 10;

    public static float[] calculateResultSingle(@Nullable float[] colorValues, @NonNull JSONArray colors) {
        double[][] interpolTable = createInterpolTable(colors);

        // determine closest value
        // create interpolation and extrapolation tables using linear approximation
        if (colorValues == null || colorValues.length < 3) {
            throw new IllegalArgumentException("invalid color data.");
        }

        double distance;
        int index = 0;
        double nearest = Double.MAX_VALUE;

        for (int j = 0; j < interpolTable.length; j++) {
            // Find the closest point using the E94 distance
            // the values are already in the right range, so we don't need to normalize
            float[] lab1 = new float[]{colorValues[0], colorValues[1], colorValues[2]};
            float[] lab2 = new float[]{(float) interpolTable[j][0], (float) interpolTable[j][1], (float) interpolTable[j][2]};
            distance = ColorUtils.deltaE2000(lab1, lab2);

            if (distance < nearest) {
                nearest = distance;
                index = j;
            }
        }

        if (nearest < Constants.MAX_COLOR_DISTANCE) {
            // return result only if the color distance is not too big
            int leftIndex = Math.max(0, index - INTERPOLATION_NUMBER / 2);
            int rightIndex = Math.min(interpolTable.length - 1, index + INTERPOLATION_NUMBER / 2);
            float leftBracket = (float) interpolTable[leftIndex][3];
            float rightBracket = (float) interpolTable[rightIndex][3];
            return new float[]{index, (float) interpolTable[index][3], leftBracket, rightBracket};
        } else {
            return new float[]{-1, Float.NaN, -1, -1};
        }
    }

    public static float[] calculateResultGroup(List<PatchResult> patchResultList, @NonNull List<Patch> patches) {
        float[][] colorsValueLab = new float[patches.size()][3];
        double[][][] interpolTables = new double[patches.size()][][];

        // create all interpol tables
        for (int p = 0; p < patches.size(); p++) {
            PatchResult patchResult = patchResultList.get(p);
            colorsValueLab[p] = patchResult.getLab();

            JSONArray colors = patches.get(p).getColors();

            // create interpol table for this patch
            interpolTables[p] = createInterpolTable(colors);

            if (colorsValueLab[p] == null || colorsValueLab[p].length < 3) {
                throw new IllegalArgumentException("invalid color data.");
            }
        }

        double distance;
        int index = 0;
        double nearest = Double.MAX_VALUE;

        // compute smallest distance, combining all interpolation tables as we want the global minimum
        // all interpol tables should have the same length here, so we use the length of the first one

        for (int j = 0; j < interpolTables[0].length; j++) {
            distance = 0;
            for (int p = 0; p < patches.size(); p++) {
                float[] lab1 = new float[]{colorsValueLab[p][0], colorsValueLab[p][1], colorsValueLab[p][2]};
                float[] lab2 = new float[]{(float) interpolTables[p][j][0], (float) interpolTables[p][j][1], (float) interpolTables[p][j][2]};
                distance += ColorUtils.deltaE2000(lab1, lab2);
            }
            if (distance < nearest) {
                nearest = distance;
                index = j;
            }
        }

        if (nearest < Constants.MAX_COLOR_DISTANCE * patches.size()) {
            // return result only if the color distance is not too big
            int leftIndex = Math.max(0, index - INTERPOLATION_NUMBER / 2);
            int rightIndex = Math.min(interpolTables[0].length - 1, index + INTERPOLATION_NUMBER / 2);
            float leftBracket = (float) interpolTables[0][leftIndex][3];
            float rightBracket = (float) interpolTables[0][rightIndex][3];
            return new float[]{index, (float) interpolTables[0][index][3], leftBracket, rightBracket};
        } else {
            return new float[]{-1, Float.NaN, -1, -1};
        }
    }

    private static double[][] createInterpolTable(@NonNull JSONArray colors) {
        JSONArray patchColorValues;
        double resultPatchValueStart, resultPatchValueEnd;
        double[] pointStart;
        double[] pointEnd;
        double lInter, aInter, bInter, vInter;
        double[][] interpolTable = new double[(colors.length() - 1) * INTERPOLATION_NUMBER + 1][4];
        int count = 0;

        for (int i = 0; i < colors.length() - 1; i++) {
            try {
                patchColorValues = colors.getJSONObject(i).getJSONArray(SensorConstants.LAB);
                resultPatchValueStart = colors.getJSONObject(i).getDouble(SensorConstants.VALUE);
                pointStart = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                patchColorValues = colors.getJSONObject(i + 1).getJSONArray(SensorConstants.LAB);
                resultPatchValueEnd = colors.getJSONObject(i + 1).getDouble(SensorConstants.VALUE);
                pointEnd = new double[]{patchColorValues.getDouble(0), patchColorValues.getDouble(1), patchColorValues.getDouble(2)};

                double lStart = pointStart[0];
                double aStart = pointStart[1];
                double bStart = pointStart[2];

                double dL = (pointEnd[0] - pointStart[0]) / INTERPOLATION_NUMBER;
                double da = (pointEnd[1] - pointStart[1]) / INTERPOLATION_NUMBER;
                double db = (pointEnd[2] - pointStart[2]) / INTERPOLATION_NUMBER;
                double dV = (resultPatchValueEnd - resultPatchValueStart) / INTERPOLATION_NUMBER;

                // create 10 interpolation points, including the start point,
                // but excluding the end point

                for (int ii = 0; ii < INTERPOLATION_NUMBER; ii++) {
                    lInter = lStart + ii * dL;
                    aInter = aStart + ii * da;
                    bInter = bStart + ii * db;
                    vInter = resultPatchValueStart + ii * dV;

                    interpolTable[count][0] = lInter;
                    interpolTable[count][1] = aInter;
                    interpolTable[count][2] = bInter;
                    interpolTable[count][3] = vInter;
                    count++;
                }

                // add final point
                patchColorValues = colors.getJSONObject(colors.length() - 1).getJSONArray(SensorConstants.LAB);
                interpolTable[count][0] = patchColorValues.getDouble(0);
                interpolTable[count][1] = patchColorValues.getDouble(1);
                interpolTable[count][2] = patchColorValues.getDouble(2);
                interpolTable[count][3] = colors.getJSONObject(colors.length() - 1).getDouble(SensorConstants.VALUE);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
        return interpolTable;
    }

    // creates formatted string including unit from float value
    public static String createValueUnitString(float value, String unit) {
        String valueString = "No Result";
        if (value > -1) {
            if (value < 1.0) {
                valueString = String.format(Locale.US, "%.2f %s", value, unit);
            } else {
                valueString = String.format(Locale.US, "%.1f %s", value, unit);
            }
        }
        return valueString;
    }

    // creates formatted string from float value, for display of colour charts
    // here, we use points as decimal separator always, as this is also used
    // to format numbers that are returned by json.
    public static String createValueString(float value) {
        if (value < 1.0f) return String.format(Locale.US, "%.2f", value);
        if (value < 10.0f) return String.format(Locale.US, "%.1f", value);
        return String.format(Locale.US, "%.0f", value);
    }


    /*
       * Restricts number of significant digits depending on size of number
        */
    public static double roundSignificant(double value) {
        if (value < 1.0) {
            return Math.round(value * 100) / 100.0;
        } else {
            return Math.round(value * 10) / 10.0;
        }
    }


}
