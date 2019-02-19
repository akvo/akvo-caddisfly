package org.akvo.caddisfly.sensor.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WaitingFragment extends Fragment {

    public static WaitingFragment getInstance() {
        return new WaitingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiting_result, container, false);

        ProgressBar progressCircle = view.findViewById(R.id.progressCircle);

        if (!AppConfig.STOP_ANIMATIONS) {
            progressCircle.setVisibility(View.VISIBLE);
        }

        return view;
    }

}
