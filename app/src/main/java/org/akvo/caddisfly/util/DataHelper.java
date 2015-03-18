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

package org.akvo.caddisfly.util;

import android.content.Context;
import android.graphics.Color;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    public static String getSwatchError(Context context, int errorCode) {
        switch (errorCode) {
            case Config.ERROR_NOT_YET_CALIBRATED:
                return context.getString(R.string.errorNotCalibrated);
            case Config.ERROR_DUPLICATE_SWATCH:
                return context.getString(R.string.errorDuplicateSwatch);
            case Config.ERROR_SWATCH_OUT_OF_PLACE:
                return context.getString(R.string.errorOutOfSequence);
            case Config.ERROR_OUT_OF_RANGE:
                return context.getString(R.string.errorOutOfRange);
            case Config.ERROR_COLOR_IS_GRAY:
                return context.getString(R.string.errorOutOfRange);
            case Config.ERROR_LOW_QUALITY:
                return context.getString(R.string.errorPhotoQuality);
            default:
                return context.getString(R.string.error);
        }
    }

    public static void saveResultToPreferences(Context context, String testType, long id, String folderName,
                                               double finalResult, int resultColor) {
        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Config.SAMPLING_COUNT_DEFAULT);
        String text = "";
        String separator = System.getProperty("line.separator");
        for (int i = 0; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            double tempResult = PreferencesUtils.getDouble(context, key);
            PreferencesUtils.setDouble(context,
                    String.format(context.getString(R.string.resultValueKey), testType, id, i),
                    tempResult);

            key = String.format(context.getString(R.string.samplingColorIndexKey), i);
            int tempColor = PreferencesUtils.getInt(context, key, -1);
            PreferencesUtils.setInt(context,
                    String.format(context.getString(R.string.resultColorKey), testType, id, i),
                    tempColor);

            key = String.format(context.getString(R.string.samplingQualityIndexKey), i);
            int tempQuality = PreferencesUtils.getInt(context, key, -1);
            PreferencesUtils.setInt(context,
                    String.format(context.getString(R.string.resultQualityKey), testType, id, i),
                    tempQuality);

            text += tempResult + "," + tempColor + "," + tempQuality + separator;
        }
        text += finalResult + "," + resultColor + separator;
        FileUtils.saveText(folderName + "result.txt", text);
    }

    public static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) ret[i] = doubles.get(i);
        return ret;
    }


    public static int getAverageColor(Context context, ArrayList<Integer> colors) {
        int counter = 0;

        int red = 0;
        int green = 0;
        int blue = 0;
        //Ignore the first result
        for (int i = 1; i < colors.size(); i++) {
            int color = colors.get(i);
            if (color != -1) {
                counter++;
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
            }
        }

        if (counter >= Config.SAMPLING_COUNT_DEFAULT - 1) {
            return Color.rgb(red / counter, green / counter, blue / counter);
        } else {
            return -1;
        }
    }


    public static double getAverageResult(Context context, ArrayList<Double> results) {

        double result = 0;

        int counter = 0;
        double commonResult = 0;

        commonResult = ColorUtils.mostFrequent(convertDoubles(results));

        //Ignore the first result
        for (int i = 1; i < results.size(); i++) {
            if (results.get(i) >= 0) {
                if (Math.abs(results.get(i) - commonResult) < 0.2) {
                    counter++;
                    result += results.get(i);
                }
            }
        }

        if (counter >= Config.SAMPLING_COUNT_DEFAULT - 1) {
            try {
                result = round(result / counter, 2);
            } catch (Exception ex) {
                result = -1;
            }
        } else {
            result = -1;
        }

        return result;
    }

    //Ref: http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    @SuppressWarnings("SameParameterValue")
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void saveTempResult(Context context, double resultValue, int resultColor, int quality) {

        int samplingCount = PreferencesUtils.getInt(context, R.string.currentSamplingCountKey, 0);
        String key = String.format(context.getString(R.string.samplingIndexKey), samplingCount);
        PreferencesUtils.setDouble(context, key, resultValue);
        key = String.format(context.getString(R.string.samplingColorIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, resultColor);
        key = String.format(context.getString(R.string.samplingQualityIndexKey), samplingCount);
        PreferencesUtils.setInt(context, key, quality);
    }
}