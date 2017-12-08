package org.akvo.caddisfly.sensor.striptest.models;


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

    public int compareTo(TimeDelayDetail o) {
        int result = Integer.valueOf(testStage).compareTo(o.testStage);
        if (result == 0) {
            return Integer.valueOf(timeDelay).compareTo(o.timeDelay);
        } else {
            return result;
        }
    }

}
