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

package org.akvo.caddisfly.usb;

import java.nio.ByteBuffer;

/**
 * Callback interface for UVCCamera class
 * If you need frame data as ByteBuffer, you can use this callback interface with UVCCamera#setFrameCallback
 */
public interface IFrameCallback {
    /**
     * This method is called from native library via JNI on the same thread as UVCCamera#startCapture.
     * You can use both UVCCamera#startCapture and #setFrameCallback
     * but it is better to use either for better performance.
     * You can also pass pixel format type to UVCCamera#setFrameCallback for this method.
     * Some frames may drops if this method takes a time.
     *
     * @param frame the captured frame
     */
    void onFrame(ByteBuffer frame);
}
