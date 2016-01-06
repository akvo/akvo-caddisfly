package org.akvo.akvoqr.calibration;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by markwestra on 01/08/2015.
 */
public class CalibrationResultData {
    public Mat calibratedImage;
    public double meanE94;
    public double maxE94;
    public double totalE94;

    public CalibrationResultData(Mat img, double meanE94, double maxE94, double totalE94){
        this.calibratedImage = img;
        this.meanE94 = meanE94;
        this.maxE94 = maxE94;
        this.totalE94 = totalE94;
    }
}
