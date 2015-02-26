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
import android.os.Bundle;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DataHelper {

    public static String getSwatchError(Context context, int errorCode) {
        switch (errorCode) {
            case Config.ERROR_NOT_YET_CALIBRATED:
                return context.getString(R.string.notCalibrated);
            case Config.ERROR_DUPLICATE_SWATCH:
                return context.getString(R.string.duplicateSwatch);
            case Config.ERROR_SWATCH_OUT_OF_PLACE:
                return context.getString(R.string.outOfSequence);
            case Config.ERROR_OUT_OF_RANGE:
                return context.getString(R.string.outOfRange);
            case Config.ERROR_COLOR_IS_GRAY:
                return context.getString(R.string.outOfRange);
            case Config.ERROR_LOW_QUALITY:
                return context.getString(R.string.photoQualityError);
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

/*    public static void saveResult(Context context, int testType, int id, int position, double resultValue, int resultColor, int quality) {

        PreferencesUtils.setDouble(context,
                String.format(context.getString(R.string.resultValueKey), testType, id, position),
                resultValue);

        PreferencesUtils.setInt(context,
                String.format(context.getString(R.string.resultColorKey), testType, id, position),
                resultColor);

        PreferencesUtils.setInt(context,
                String.format(context.getString(R.string.resultQualityKey), testType, id, position),
                quality);
    }*/

    public static void getAverageResult(Context context, Bundle bundle) {

        double result = 0;

        int samplingCount = PreferencesUtils
                .getInt(context, R.string.samplingCountKey, Config.SAMPLING_COUNT_DEFAULT);
        int counter = 0;
        double commonResult = 0;
        double[] results = new double[samplingCount];
        int[] colors = new int[samplingCount];
        for (int i = 1; i < samplingCount; i++) {
            String key = String.format(context.getString(R.string.samplingIndexKey), i);
            results[i] = PreferencesUtils.getDouble(context, key);
            key = String.format(context.getString(R.string.samplingColorIndexKey), i);
            colors[i] = PreferencesUtils.getInt(context, key, -1);
            commonResult = ColorUtils.mostFrequent(results);
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        //Ignore the first result
        for (int i = 1; i < results.length; i++) {
            if (results[i] >= 0 && colors[i] != -1) {
                if (Math.abs(results[i] - commonResult) < 0.3) {
                    counter++;
                    result += results[i];
                    red += Color.red(colors[i]);
                    green += Color.green(colors[i]);
                    blue += Color.blue(colors[i]);
                }
            }
        }

        //Ignore the first result
        if (counter == (Config.SAMPLING_COUNT_DEFAULT - 1)) {
            result = round(result / counter, 2);
            bundle.putInt(Config.RESULT_COLOR_KEY,
                    Color.rgb(red / counter, green / counter, blue / counter));
        } else {
            result = -1;
        }
        bundle.putDouble(Config.RESULT_VALUE_KEY, result);

//        return result;
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
