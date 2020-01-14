package org.akvo.caddisfly.sensor.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;

import java.util.Locale;

public class WaitingFragment extends Fragment {

    private static final String INSTRUCTION_INDEX = "instruction_index";

    public static WaitingFragment getInstance(int index) {
        WaitingFragment fragment = new WaitingFragment();
        Bundle args = new Bundle();
        args.putInt(INSTRUCTION_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiting_result, container, false);

        ProgressBar progressCircle = view.findViewById(R.id.progressCircle);

        if (getArguments() != null) {
            TextView textIndex = view.findViewById(R.id.text_index);
            textIndex.setText(String.format(Locale.US, "%d.", getArguments().getInt(INSTRUCTION_INDEX)));
        }

        if (!BuildConfig.TEST_RUNNING) {
            progressCircle.setVisibility(View.VISIBLE);
        }

        return view;
    }

}
