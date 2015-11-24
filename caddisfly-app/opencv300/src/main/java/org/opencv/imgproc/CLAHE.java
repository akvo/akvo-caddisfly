
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

// C++: class CLAHE
//javadoc: CLAHE
public class CLAHE extends Algorithm {

    protected CLAHE(long addr) {
        super(addr);
    }


    //
    // C++:  void setClipLimit(double clipLimit)
    //

    // C++:  void setClipLimit(double clipLimit)
    private static native void setClipLimit_0(long nativeObj, double clipLimit);


    //
    // C++:  void apply(Mat src, Mat& dst)
    //

    // C++:  void apply(Mat src, Mat& dst)
    private static native void apply_0(long nativeObj, long src_nativeObj, long dst_nativeObj);


    //
    // C++:  double getClipLimit()
    //

    // C++:  double getClipLimit()
    private static native double getClipLimit_0(long nativeObj);


    //
    // C++:  void setTilesGridSize(Size tileGridSize)
    //

    // C++:  void setTilesGridSize(Size tileGridSize)
    private static native void setTilesGridSize_0(long nativeObj, double tileGridSize_width, double tileGridSize_height);


    //
    // C++:  void collectGarbage()
    //

    // C++:  void collectGarbage()
    private static native void collectGarbage_0(long nativeObj);


    //
    // C++:  Size getTilesGridSize()
    //

    // C++:  Size getTilesGridSize()
    private static native double[] getTilesGridSize_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: CLAHE::apply(src, dst)
    public void apply(Mat src, Mat dst) {

        apply_0(nativeObj, src.nativeObj, dst.nativeObj);

        return;
    }

    //javadoc: CLAHE::getClipLimit()
    public double getClipLimit() {

        double retVal = getClipLimit_0(nativeObj);

        return retVal;
    }

    //javadoc: CLAHE::setClipLimit(clipLimit)
    public void setClipLimit(double clipLimit) {

        setClipLimit_0(nativeObj, clipLimit);

        return;
    }

    //javadoc: CLAHE::collectGarbage()
    public void collectGarbage() {

        collectGarbage_0(nativeObj);

        return;
    }

    //javadoc: CLAHE::getTilesGridSize()
    public Size getTilesGridSize() {

        Size retVal = new Size(getTilesGridSize_0(nativeObj));

        return retVal;
    }

    //javadoc: CLAHE::setTilesGridSize(tileGridSize)
    public void setTilesGridSize(Size tileGridSize) {

        setTilesGridSize_0(nativeObj, tileGridSize.width, tileGridSize.height);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
