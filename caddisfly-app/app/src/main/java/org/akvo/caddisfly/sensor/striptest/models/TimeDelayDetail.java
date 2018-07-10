package org.akvo.caddisfly.sensor.striptest.models;


import android.support.annotation.NonNull;

public class TimeDelayDetail implements Comparable<TimeDelayDetail> {
    private int testStage;
    private int timeDelay;

    public TimeDelayDetail(int testStage, int timeDelay) {
        this.testStage = testStage;
        this.timeDelay = timeDelay;
    }

    public int getTestStage() {
        return testStage;
    }

    public int getTimeDelay() {
        return timeDelay;
    }

    public int compareTo(@NonNull TimeDelayDetail o) {
        int result = Integer.compare(testStage, o.testStage);
        if (result == 0) {
            return Integer.compare(timeDelay, o.timeDelay);
        } else {
            return result;
        }
    }

}
