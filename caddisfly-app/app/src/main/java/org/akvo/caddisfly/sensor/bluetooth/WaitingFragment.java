package org.akvo.caddisfly.sensor.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WaitingFragment extends Fragment {

    public static WaitingFragment getInstance() {
        return new WaitingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_result, container, false);
    }

}
