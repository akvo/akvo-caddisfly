package org.akvo.caddisfly.sensor;

public class CalibrationException extends Exception {

    public CalibrationException(String message) {
        super(message);
    }

    public CalibrationException(String message, Throwable e) {
        super(message, e);
    }
}
