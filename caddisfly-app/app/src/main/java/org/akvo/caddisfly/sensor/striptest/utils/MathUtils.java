package org.akvo.caddisfly.sensor.striptest.utils;

import java.util.Arrays;

class MathUtils {

//    public static float mean(float[] m) {
//        float sum = 0;
//        for (float aM : m) {
//            sum += aM;
//        }
//        return sum / m.length;
//    }
//
//    public static float median(float[] m) {
//        Arrays.sort(m);
//        int middle = m.length / 2;
//        if (m.length % 2 == 1) {
//            return m[middle];
//        } else {
//            return (m[middle - 1] + m[middle]) / 2.0f;
//        }
//    }

    public static float[] meanMedianMax(float[] m) {
        // compute mean
        float sum = 0;
        for (float aM : m) {
            sum += aM;
        }
        float mean = sum / m.length;

        // sort array
        Arrays.sort(m);

        // compute median
        float median;
        int middle = m.length / 2;
        if (m.length % 2 == 1) {
            median = m[middle];
        } else {
            median = (m[middle - 1] + m[middle]) / 2.0f;
        }

        // max (we have already sorted the array)
        float max = m[m.length - 1];

        return new float[]{mean, median, max};
    }
}
