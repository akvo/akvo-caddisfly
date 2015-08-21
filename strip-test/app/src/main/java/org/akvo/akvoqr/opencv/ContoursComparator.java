package org.akvo.akvoqr.opencv;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.Comparator;

/**
 * Created by linda on 8/20/15.
 */
public class ContoursComparator implements Comparator<MatOfPoint> {
    @Override
    public int compare(MatOfPoint lhs, MatOfPoint rhs) {

        double areasizelhs = Imgproc.contourArea(lhs);
        double areasizerhs = Imgproc.contourArea(rhs);
        if(areasizelhs > areasizerhs)
            return 1;
        else if (areasizelhs == areasizerhs)
            return 0;
        else  return -1;

    }
}
