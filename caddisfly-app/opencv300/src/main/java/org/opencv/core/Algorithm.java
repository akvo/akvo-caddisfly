
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
package org.opencv.core;

// C++: class Algorithm
//javadoc: Algorithm
public class Algorithm {

    protected final long nativeObj;

    protected Algorithm(long addr) {
        nativeObj = addr;
    }


    //
    // C++:  void clear()
    //

    // C++:  void clear()
    private static native void clear_0(long nativeObj);


    //
    // C++:  void save(String filename)
    //

    // C++:  void save(String filename)
    private static native void save_0(long nativeObj, String filename);


    //
    // C++:  String getDefaultName()
    //

    // C++:  String getDefaultName()
    private static native String getDefaultName_0(long nativeObj);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: Algorithm::clear()
    public void clear() {

        clear_0(nativeObj);

        return;
    }

    //javadoc: Algorithm::save(filename)
    public void save(String filename) {

        save_0(nativeObj, filename);

        return;
    }

    //javadoc: Algorithm::getDefaultName()
    public String getDefaultName() {

        String retVal = getDefaultName_0(nativeObj);

        return retVal;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
