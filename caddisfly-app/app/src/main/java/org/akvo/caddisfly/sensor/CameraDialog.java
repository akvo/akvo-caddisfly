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

package org.akvo.caddisfly.sensor;

import android.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class CameraDialog extends DialogFragment {

    final List<PictureTaken> pictureTakenObservers = new ArrayList<>();

    public abstract void takePictureSingle();

    @SuppressWarnings("SameParameterValue")
    public abstract void takePictures(int count, long delay);

    public void setPictureTakenObserver(PictureTaken observer) {
        pictureTakenObservers.add(observer);
    }

    public abstract void stopCamera();

    public interface Cancelled {
        void dialogCancelled();
    }

    public interface PictureTaken {
        void onPictureTaken(byte[] bytes, boolean completed);
    }
}
