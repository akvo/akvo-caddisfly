/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor;

import android.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class CameraDialog extends DialogFragment {

    protected final List<PictureTaken> pictureTakenObservers = new ArrayList<>();

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
