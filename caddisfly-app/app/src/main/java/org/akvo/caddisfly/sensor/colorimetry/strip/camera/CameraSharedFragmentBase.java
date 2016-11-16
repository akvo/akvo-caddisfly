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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

import java.util.Map;

/**
 * Created by linda on 11/25/15
 */
@SuppressWarnings("deprecation")
public abstract class CameraSharedFragmentBase extends Fragment {

    private static final String TAG = "CamSharedFragmentBase";
    ProgressBar progressBar;
    int progressIncrement = 0;
    boolean qualityChecksDone;
    private TextView textMessage;
    private int previousQualityCount = 0;
    private long lastQualityIncrementTime;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textMessage = (TextView) view.findViewById(R.id.textMessage);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setMax(Constant.COUNT_QUALITY_CHECK_LIMIT);
            progressBar.setProgress(0);
        }
        if (textMessage != null) {
            textMessage.setText(R.string.checking_quality);
        }
    }

    // track the last a single quality check was successful
    long getLastQualityIncrementTime() {
        return lastQualityIncrementTime;
    }

    void showBrightness(double value) {
    }

    void showShadow(double value) {
    }

    public void goNext() {
    }

    /*
    *  Display number of successful quality checks
    */
    public void displayCountQuality(@NonNull Map<String, Integer> countMap) {

        try {
            if (textMessage != null) {
                int count = 0;

                // Each parameter counts for 1/3 towards the final count shown.
                for (int i : countMap.values()) {
                    count += Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size(), i);
                }

                count = Math.max(0, Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count));

                if (count > previousQualityCount) {
                    lastQualityIncrementTime = System.currentTimeMillis();
                }

                previousQualityCount = count;

                progressBar.setProgress(count + progressIncrement);

                qualityChecksDone = count >= Constant.COUNT_QUALITY_CHECK_LIMIT;

//                Debugging: Display count per quality parameter
//                if (AppPreferences.isDiagnosticMode()) {
//                    StringBuilder debugText = new StringBuilder();
//                    for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
//                        debugText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
//                    }
//                    textMessage.setText(debugText.toString());
//                }

                textMessage.setTextColor(getResources().getColor(R.color.text_primary));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void showError(String message) {
        if (textMessage != null) {
            textMessage.setText(message);
            textMessage.setTextColor(getResources().getColor(R.color.error));
        }
    }

    TextView getTextMessage() {
        return textMessage;
    }
}
