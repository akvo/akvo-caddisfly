package org.akvo.akvoqr.color;

import java.util.Comparator;

/**
 * Created by linda on 8/14/15.
 */
public class mColorComparator implements Comparator<ColorDetected> {

    /* sort ColorDetected by x-position (from left to right) */
    @Override
    public int compare(ColorDetected lhs, ColorDetected rhs) {
        if (lhs.getX() < rhs.getX())
            return -1;

        return 1;
    }
}
