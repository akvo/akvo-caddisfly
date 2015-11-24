
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

//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.imgproc;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Size;

// C++: class LineSegmentDetector
//javadoc: LineSegmentDetector
public class LineSegmentDetector extends Algorithm {

    protected LineSegmentDetector(long addr) {
        super(addr);
    }


    //
    // C++:  void detect(Mat _image, Mat& _lines, Mat& width = Mat(), Mat& prec = Mat(), Mat& nfa = Mat())
    //

    // C++:  void detect(Mat _image, Mat& _lines, Mat& width = Mat(), Mat& prec = Mat(), Mat& nfa = Mat())
    private static native void detect_0(long nativeObj, long _image_nativeObj, long _lines_nativeObj, long width_nativeObj, long prec_nativeObj, long nfa_nativeObj);

    private static native void detect_1(long nativeObj, long _image_nativeObj, long _lines_nativeObj);


    //
    // C++:  void drawSegments(Mat& _image, Mat lines)
    //

    // C++:  void drawSegments(Mat& _image, Mat lines)
    private static native void drawSegments_0(long nativeObj, long _image_nativeObj, long lines_nativeObj);


    //
    // C++:  int compareSegments(Size size, Mat lines1, Mat lines2, Mat& _image = Mat())
    //

    // C++:  int compareSegments(Size size, Mat lines1, Mat lines2, Mat& _image = Mat())
    private static native int compareSegments_0(long nativeObj, double size_width, double size_height, long lines1_nativeObj, long lines2_nativeObj, long _image_nativeObj);

    private static native int compareSegments_1(long nativeObj, double size_width, double size_height, long lines1_nativeObj, long lines2_nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: LineSegmentDetector::detect(_image, _lines, width, prec, nfa)
    public void detect(Mat _image, Mat _lines, Mat width, Mat prec, Mat nfa) {

        detect_0(nativeObj, _image.nativeObj, _lines.nativeObj, width.nativeObj, prec.nativeObj, nfa.nativeObj);

        return;
    }

    //javadoc: LineSegmentDetector::detect(_image, _lines)
    public void detect(Mat _image, Mat _lines) {

        detect_1(nativeObj, _image.nativeObj, _lines.nativeObj);

        return;
    }

    //javadoc: LineSegmentDetector::drawSegments(_image, lines)
    public void drawSegments(Mat _image, Mat lines) {

        drawSegments_0(nativeObj, _image.nativeObj, lines.nativeObj);

        return;
    }

    //javadoc: LineSegmentDetector::compareSegments(size, lines1, lines2, _image)
    public int compareSegments(Size size, Mat lines1, Mat lines2, Mat _image) {

        int retVal = compareSegments_0(nativeObj, size.width, size.height, lines1.nativeObj, lines2.nativeObj, _image.nativeObj);

        return retVal;
    }

    //javadoc: LineSegmentDetector::compareSegments(size, lines1, lines2)
    public int compareSegments(Size size, Mat lines1, Mat lines2) {

        int retVal = compareSegments_1(nativeObj, size.width, size.height, lines1.nativeObj, lines2.nativeObj);

        return retVal;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
