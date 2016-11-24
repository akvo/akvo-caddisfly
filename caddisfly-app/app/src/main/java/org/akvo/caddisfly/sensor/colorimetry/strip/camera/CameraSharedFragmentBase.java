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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestStatus;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

import java.util.Map;

/**
 * Created by linda on 11/25/15
 */
@SuppressWarnings("deprecation")
public abstract class CameraSharedFragmentBase extends Fragment {

    private static final String TAG = "CamSharedFragmentBase";

    private static final int PROGRESS_FADE_DURATION_MILLIS = 3000;
    private static final long INITIAL_DELAY_MILLIS = 200;
    private TestStatus status = TestStatus.DETECTING_COLOR_CARD;
    private TextSwitcher textSwitcher;
    private ProgressBar progressBar;
    private int previousQualityCount = 0;
    private long lastQualityIncrementTime;

    TestStatus getStatus() {
        return status;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setMax(Constant.COUNT_QUALITY_CHECK_LIMIT);
            progressBar.setProgress(0);
        }

        textSwitcher = (TextSwitcher) view.findViewById(R.id.textSwitcher);

        if (textSwitcher != null) {

            textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                private TextView textView;
                private boolean isFirst = true;

                @Override
                public View makeView() {

                    if (isFirst) {
                        isFirst = false;
                        textView = (TextView) view.findViewById(R.id.textMessage1);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    } else {
                        textView = (TextView) view.findViewById(R.id.textMessage2);
                        ((ViewGroup) textView.getParent()).removeViewAt(0);
                    }
                    return textView;
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textSwitcher.setText(getString(R.string.detecting_color_card));
                }
            }, INITIAL_DELAY_MILLIS);
        }
    }

    // track the last time a single quality check was successful
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
            int count = 0;

            // Each parameter counts for 1/3 towards the final count shown.
            for (int i : countMap.values()) {
                count += Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size(), i);
            }

            count = Math.max(0, Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count));

            if (status != TestStatus.QUALITY_CHECK_DONE) {

                if (count > previousQualityCount) {
                    lastQualityIncrementTime = System.currentTimeMillis();
                }

                previousQualityCount = count;

                progressBar.setProgress(count);

                if (count >= Constant.COUNT_QUALITY_CHECK_LIMIT) {
                    hideProgressBar();
                    status = TestStatus.QUALITY_CHECK_DONE;
                } else if (count > 0) {
                    showMessage(R.string.checking_quality);
                    status = TestStatus.CHECKING_QUALITY;
                }
            }

//                Debugging: Display count per quality parameter
//                if (AppPreferences.isDiagnosticMode()) {
//                    StringBuilder debugText = new StringBuilder();
//                    for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
//                        debugText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
//                    }
//                    textMessage.setText(debugText.toString());
//                }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    void showMessage(@StringRes int id) {
        showMessage(getString(id));
    }

    void showMessage(final String message) {
        if (!((TextView) textSwitcher.getCurrentView()).getText().equals(message)) {
            textSwitcher.setText(message);
        }
    }

    private void clearProgress() {
        if (progressBar != null) {
            progressBar.setProgress(0);
        }
    }

    private void hideProgressBar() {
        AlphaAnimation animation = new AlphaAnimation(1f, 0);
        animation.setDuration(PROGRESS_FADE_DURATION_MILLIS);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearProgress();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progressBar.startAnimation(animation);
    }
}
