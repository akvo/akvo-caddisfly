package org.akvo.akvoqr;

import android.support.v4.app.Fragment;

/**
 * Created by linda on 11/25/15.
 * Contains methods that are shared by child classes
 * So the activity that inflates an instance of this fragment has access to them
 * in a simple way
 */
public abstract class CameraSharedFragment extends Fragment {

    public void countQuality(int count){};

    public void showStartButton(){};
}
