package org.akvo.akvoqr;

import java.util.Comparator;

/**
 * Created by linda on 8/14/15.
 */
public class mColorComparator implements Comparator<ResultActivity.ColorDetected> {

    @Override
    public int compare(ResultActivity.ColorDetected lhs, ResultActivity.ColorDetected rhs) {
        if (lhs.getX() < rhs.getX())
            return -1;

        return 1;
    }
}
