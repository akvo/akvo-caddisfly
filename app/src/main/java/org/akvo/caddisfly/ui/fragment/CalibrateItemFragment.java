/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.GalleryListAdapter;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.ui.activity.ProgressActivity;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ColorUtils;
import org.akvo.caddisfly.util.DataHelper;
import org.akvo.caddisfly.util.PreferencesHelper;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.SoundPoolPlayer;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class CalibrateItemFragment extends ListFragment {

    protected String mTestType;
    protected GalleryListAdapter mAdapter;
    private SoundPoolPlayer sound;

    private PowerManager.WakeLock wakeLock;

    private Button mValueButton;
    private Button mColorButton;

    private LinearLayout mErrorLayout;
    private TextView mErrorTextView;
    private TextView mErrorSummaryTextView;

    public CalibrateItemFragment() {
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        @SuppressLint("InflateParams") View header = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_calibrate_item, null, false);
        ListView listView = getListView();

        assert header != null;
        mValueButton = (Button) header.findViewById(R.id.valueButton);
        Button startButton = (Button) header.findViewById(R.id.startButton);
        mColorButton = (Button) header.findViewById(R.id.colorButton);
        mErrorLayout = (LinearLayout) header.findViewById(R.id.errorLayout);
        mErrorTextView = (TextView) header.findViewById(R.id.errorTextView);
        mErrorSummaryTextView = (TextView) header.findViewById(R.id.errorSummaryTextView);

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        //final int index = position * INDEX_INCREMENT_STEP;

        mTestType = getArguments().getString(getString(R.string.currentTestTypeId));

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertUtils.askQuestion(getActivity(), R.string.calibrate,
                        R.string.calibrate_info,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                calibrate(position);
                            }
                        }
                );
            }
        });

        setListAdapter(null);

        assert listView != null;
        listView.addHeaderView(header);

        updateListView(position);
    }

    private void calibrate(int position) {
        PreferencesUtils
                .setInt(getActivity(), R.string.currentSamplingCountKey, 0);

        //deleteCalibration(position);
        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getActivity()
                    .getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
        }
        startCalibration(position);
    }

/*
    private void deleteCalibration(int position) {
        File file = new File(
                FileUtils.getStoragePath(getActivity(), -1,
                        String.format("%s/%d/%d/", Config.CALIBRATE_FOLDER, mTestType,
                                position),
                        false
                )
        );

        FileUtils.deleteFolder(getActivity(), -1, file.getAbsolutePath());
    }
*/

    @Override
    public void onStart() {
        super.onStart();

        displayInfo();
    }

    void displayInfo() {

        final MainApp mainApp = ((MainApp) getActivity().getApplicationContext());
        //mainApp.setSwatches(mainApp.currentTestType);
        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;

        int color = mainApp.colorList.get(index).getColor();

        int error = mainApp.colorList.get(index).getErrorCode();
        if (error > 0) {
            mErrorLayout.setVisibility(View.VISIBLE);
            if (error == Config.ERROR_NOT_YET_CALIBRATED) {
                mErrorSummaryTextView.setVisibility(View.GONE);
                mErrorTextView.setVisibility(View.GONE);
            } else {
                mErrorSummaryTextView.setVisibility(View.VISIBLE);
                mErrorTextView.setVisibility(View.VISIBLE);
            }
            mErrorTextView.setText(DataHelper.getSwatchError(getActivity(), error));
        } else {
            mErrorLayout.setVisibility(View.GONE);
        }

        if (color != -1) {
            mColorButton.setBackgroundColor(color);
            mColorButton.setText("");
        } else {
            mColorButton.setBackgroundColor(Color.argb(0, 10, 10, 10));
            mColorButton.setText("?");
        }

        mValueButton.setText(mainApp.doubleFormat
                .format(mainApp.rangeStart + (position * (mainApp.rangeIncrementStep
                        * mainApp.rangeIncrementValue))));

    }

    public void startCalibration(final int index) {

        Context context = getActivity();

        final Intent intent = new Intent();
        intent.setClass(context, ProgressActivity.class);
        intent.putExtra("isCalibration", true);
        intent.putExtra("position", index);
        intent.putExtra(PreferencesHelper.CURRENT_LOCATION_ID_KEY, -1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                //Bundle bundle = data.getExtras();
                sound.playShortResource(R.raw.done);
                storeCalibratedData(data.getIntExtra("position", 0), data.getIntExtra(Config.RESULT_COLOR_KEY, -1),
                        data.getIntExtra(Config.QUALITY_KEY, -1));
                displayInfo();

            } else {
                if (data != null) {
                    storeCalibratedData(data.getIntExtra("position", 0), -1, -1);
                    displayInfo();
                }
            }
        }
    }

    protected void storeCalibratedData(final int position, final int resultColor,
                                       final int accuracy) {
        Context context = getActivity().getApplicationContext();
        final MainApp mainApp = ((MainApp) context.getApplicationContext());

        int index = position * mainApp.rangeIncrementStep;
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String colorKey = String.format("%s-%s", mTestType, String.valueOf(index));

        if (resultColor == -1) {
            editor.remove(colorKey);
        } else {
            ColorInfo colorInfo = new ColorInfo(resultColor, accuracy);
            mainApp.colorList.set(index, colorInfo);

            editor.putInt(colorKey, resultColor);
            editor.putInt(String.format("%s-a-%s", mTestType, String.valueOf(index)),
                    accuracy);

            ColorUtils.autoGenerateColors(
                    index,
                    mainApp.currentTestInfo.getCode(),
                    mainApp.colorList,
                    mainApp.rangeIncrementStep, editor, 0);
        }
        editor.apply();
        updateListView(position);
        displayInfo();
    }

    protected void updateListView(int position) {

        // override and show only headers by not providing data to list adapter
        ArrayList<String> files = new ArrayList<String>();
        mAdapter = new GalleryListAdapter(getActivity(), mTestType, position, files, false);
        setListAdapter(mAdapter);
    }

    private void releaseResources() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseResources();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sound = new SoundPoolPlayer(getActivity());
        super.onCreate(savedInstanceState);
    }
}
