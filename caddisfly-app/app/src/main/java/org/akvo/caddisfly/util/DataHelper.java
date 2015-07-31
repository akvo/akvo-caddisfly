/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.graphics.Color;

import org.akvo.caddisfly.model.Result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataHelper {

    private DataHelper() {
    }

    private static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) ret[i] = doubles.get(i);
        return ret;
    }

    public static int getAverageColor(ArrayList<Result> colors, int samplingTimes) {

//      return colors.get(colors.size() - 1);
        int counter = 0;

        int red = 0;
        int green = 0;
        int blue = 0;

        ArrayList<Double> distances = new ArrayList<>();

        for (int i = 1; i < colors.size() - 1; i++) {
            distances.add(ColorUtils.getDistance(colors.get(i).getColor(), colors.get(i + 1).getColor()));
        }

        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > 20) {
                return 0;
            }
        }

        //Ignore the first result
        for (int i = 1; i < colors.size(); i++) {
            int color = colors.get(i).getColor();
            if (color != 0) {
                counter++;
                red += Color.red(color);
                green += Color.green(color);
                blue += Color.blue(color);
            }
        }

        if (counter >= samplingTimes - 1) {
            return Color.rgb(red / counter, green / counter, blue / counter);
        } else {
            return 0;
        }
    }

    private static int getClosestMatchIndex(ArrayList<Double> resultArray, double commonResult) {
        double difference = 9999999;
        int index = -1;

        for (int i = 0; i < resultArray.size(); i++) {
            double value = resultArray.get(i);
            if (value != -1 && Math.abs(commonResult - value) < difference) {
                difference = Math.abs(commonResult - value);
                index = i;
            }
        }
        return index;
    }

    private static double mostFrequent(double[] array) {
        Map<Double, Integer> map = new HashMap<>();

        for (double a : array) {
            if (a >= 0) {
                Integer freq = map.get(a);
                map.put(a, (freq == null) ? 1 : freq + 1);
            }
        }

        int max = -1;
        double mostFrequent = -1;

        for (Map.Entry<Double, Integer> e : map.entrySet()) {
            if (e.getValue() > max) {
                mostFrequent = e.getKey();
                max = e.getValue();
            }
        }

        return mostFrequent;
    }

    public static double getAverageResult(ArrayList<Result> results, int samplingTimes) {

        double result = 0;

        int counter = 0;
        ArrayList<Double> resultValues = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            resultValues.add(results.get(i).getValue());
        }

        double commonResult;

        double[] resultArray = convertDoubles(resultValues);
        //ignore first value;
        resultArray[0] = -1;
        commonResult = mostFrequent(resultArray);

        ArrayList<Double> tempResults = new ArrayList<>();

        //Ignore the first result
        for (int i = 1; i < results.size(); i++) {
            double value = results.get(i).getValue();
            if (value > -1 && Math.abs(value - commonResult) < 0.21) {
                tempResults.add(value);
            }
        }

        int totalCount = tempResults.size();
        while (tempResults.size() > 0 && counter < Math.min(totalCount, samplingTimes)) {
            int index = getClosestMatchIndex(tempResults, commonResult);
            result += tempResults.get(index);
            counter++;
            tempResults.remove(index);
        }

        if (counter >= Math.min(results.size() - 1, samplingTimes)) {
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

    public static boolean validDouble(String doubleString) {
        return (doubleString.contains(".") && !doubleString.startsWith(".")) &&
                doubleString.indexOf(".") == doubleString.length() - 3;


    }
}