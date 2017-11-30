package org.akvo.caddisfly.sensor.colorimetry.stripv2.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.MessageUtils;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.widget.PercentageMeterView;

/**
 * Created by markwestra on 19/07/2017
 */
public class StripMeasureFragment extends Fragment {
    private static final long INITIAL_DELAY_MILLIS = 200;
    private static StriptestHandler mStriptestHandler;
    private ProgressBar progressBar;
    private TextView textBottom;
    private TextSwitcher textSwitcher;
    private PercentageMeterView exposureView;

    public StripMeasureFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static StripMeasureFragment newInstance(StriptestHandler striptestHandler) {
        mStriptestHandler = striptestHandler;
        return new StripMeasureFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.v2fragment_strip_measure, container, false);
        exposureView = rootView.findViewById(R.id.quality_brightness);

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setMax(Constants.COUNT_QUALITY_CHECK_LIMIT);
            progressBar.setProgress(0);
        }

        textBottom = view.findViewById(R.id.text_bottom);

        textSwitcher = view.findViewById(R.id.textSwitcher);

        if (textSwitcher != null) {
            textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                private TextView textView;
                private boolean isFirst = true;

                @Override
                public View makeView() {
                    if (isFirst) {
                        isFirst = false;
                        textView = view.findViewById(R.id.textMessage1);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    } else {
                        textView = view.findViewById(R.id.textMessage2);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    }
                    return textView;
                }
            });

            new Handler().postDelayed(() -> {
                if (isAdded()) {
                    textSwitcher.setText(getString(R.string.detecting_color_card));
                }
            }, INITIAL_DELAY_MILLIS);
        }

        mStriptestHandler.setTextSwitcher(textSwitcher);
    }

    @Override
    public void onResume() {
        super.onResume();

        // hand over to state machine
        MessageUtils.sendMessage(mStriptestHandler, StriptestHandler.START_PREVIEW_MESSAGE, 0);
    }

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // TODO figure out messages
//    void showMessage(@StringRes int id) {
//        showMessage(getString(id));
//    }

//    void showMessage(final String message) {
//        if (!((TextView) textSwitcher.getCurrentView()).getText().equals(message)) {
//            textSwitcher.setText(message);
//        }
//    }

    void showQuality(int value) {

        if (exposureView != null) {
            exposureView.setPercentage((float) value);
        }
    }

    public void clearProgress() {
        if (progressBar != null) {
            progressBar.setProgress(0);
        }
    }

    public void setMeasureText() {
        textBottom.setText(R.string.measure_instruction);
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
}
