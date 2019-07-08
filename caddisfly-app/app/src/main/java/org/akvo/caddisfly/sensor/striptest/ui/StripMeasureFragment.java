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

package org.akvo.caddisfly.sensor.striptest.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.striptest.utils.Constants;
import org.akvo.caddisfly.sensor.striptest.utils.MessageUtils;
import org.akvo.caddisfly.sensor.striptest.widget.PercentageMeterView;

public class StripMeasureFragment extends Fragment {
    private static final long INITIAL_DELAY_MILLIS = 200;
    private static StriptestHandler mStriptestHandler;
    private ProgressBar progressBar;
    private TextSwitcher textSwitcher;
    private PercentageMeterView exposureView;
    private StriptestHandler.State currentState;

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
        View rootView = inflater.inflate(R.layout.fragment_strip_measure, container, false);
        exposureView = rootView.findViewById(R.id.quality_brightness);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setMax(Constants.COUNT_QUALITY_CHECK_LIMIT);
            progressBar.setProgress(0);
        }

        TextView textBottom = view.findViewById(R.id.text_bottom);

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

        if (currentState == StriptestHandler.State.MEASURE) {
            textBottom.setText(R.string.measure_instruction);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // hand over to state machine
        MessageUtils.sendMessage(mStriptestHandler, StriptestHandler.START_PREVIEW_MESSAGE, 0);
    }

    void showQuality(int value) {

        if (exposureView != null) {
            exposureView.setPercentage((float) value);
        }
    }

    void setState(StriptestHandler.State state) {
        currentState = state;
    }

    void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
}
