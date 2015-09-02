package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.DialogFragment;

import java.util.ArrayList;

public abstract class CameraDialog extends DialogFragment {

    final ArrayList<PictureTaken> pictureTakenObservers = new ArrayList<>();

    public abstract void takePictureSingle();

    public abstract void takePictures(int count, long delay);

    public void setPictureTakenObserver(PictureTaken observer) {
        pictureTakenObservers.add(observer);
    }

    public abstract void stopCamera();

    public interface PictureTaken {
        void onPictureTaken(byte[] bytes, boolean completed);
    }
}
