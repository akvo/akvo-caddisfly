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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class CalibrateItemFragment extends ListFragment {

    protected int mTestType = Config.FLUORIDE_2_INDEX;

    protected GalleryListAdapter mAdapter;

    private OnLoadCalibrationListener mOnLoadCalibrationListener;

    //protected View mListHeader;

    //File calibrateFolder;

    private PowerManager.WakeLock wakeLock;

    private Button mValueButton;

    private Button mColorButton;

    private Button mStartButton;

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
        //mListHeader = header;
        ListView listView = getListView();

        assert header != null;
        mValueButton = (Button) header.findViewById(R.id.valueButton);
        mStartButton = (Button) header.findViewById(R.id.startButton);
        mColorButton = (Button) header.findViewById(R.id.colorButton);
        mErrorLayout = (LinearLayout) header.findViewById(R.id.errorLayout);
        mErrorTextView = (TextView) header.findViewById(R.id.errorTextView);
        mErrorSummaryTextView = (TextView) header.findViewById(R.id.errorSummaryTextView);

        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        //final int index = position * INDEX_INCREMENT_STEP;

        mTestType = getArguments().getInt(getString(R.string.currentTestTypeId));

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mStartButton.setEnabled(false);
                AlertUtils.askQuestion(getActivity(), R.string.calibrate,
                        R.string.calibrate_info,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface,
                                    int i) {
                                calibrate(position);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mStartButton.setEnabled(true);
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

        /*calibrateFolder = new File(
                FileUtils.getStoragePath(getActivity(), -1,
                        String.format("%s/%d/%d/small/", Config.CALIBRATE_FOLDER, mTestType,
                                position),
                        false
                )
        );*/

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
        mainApp.setSwatches(mainApp.currentTestType);
        final int position = getArguments().getInt(getString(R.string.swatchIndex));
        final int index = position * mainApp.rangeIncrementStep;
        //ArrayList<ColorInfo> colorRange = mainApp.colorList;

        int color = mainApp.colorList.get(index).getColor();

/*
        int color = PreferencesUtils.getInt(mainApp,
                String.format("%s-%s", String.valueOf(mTestType), String.valueOf(index)),
                -1);

        int accuracy = Math.max(0, PreferencesUtils.getInt(mainApp,
                String.format("%s-a-%s", String.valueOf(mTestType), String.valueOf(index)),
                101));

        //check if calibration is factory preset or set by user
        if (accuracy >= 101) {
            accuracy = -1;
        }

        if (color == -1) {
            color = colorRange.get(index).getColor();
        }
*/

        //int minAccuracy = PreferencesUtils
        //      .getInt(mainApp, R.string.minPhotoQualityKey, Config.MINIMUM_PHOTO_QUALITY);

        //if (accuracy < minAccuracy && accuracy > -1) {
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

        //if (accuracy == -1) {
        //mColorButton.setText(getActivity().getString(R.string.notCalibrated));
        //  color = Color.BLACK;
        //}

        if (color != -1) {
            mColorButton.setBackgroundColor(color);
            mColorButton.setText("");
        } else {
            mColorButton.setBackgroundColor(Color.argb(0, 10, 10, 10));
            mColorButton.setText("?");
        }

        mValueButton.setText(mainApp.doubleFormat
                .format((position + mainApp.rangeStartIncrement) * (mainApp.rangeIncrementStep
                        * mainApp.rangeIncrementValue)));


/*
        if (Config.isExternalFlavor) {
            switch (position) {
                case 0:
                    mValueButton.setText(R.string.pink);
                    break;
                case 1:
                    mValueButton.setText(R.string.yellow);
                    break;
            }
        }
*/

        mStartButton.setEnabled(true);
    }

    /**
     * Calibrate by taking a photo and using its color for the chosen index
     *
     * @param index The index of the value to be calibrated
     */
    /*void startCalibration(final int index) {
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                PhotoHandler photoHandler = new PhotoHandler(
                        getActivity().getApplicationContext(), mPhotoTakenHandler, index, "",
                        mTestType);
                //camera.takePicture(null, null, photoHandler);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("cameraDialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                mCameraFragment = CameraFragment.newInstance();
                mCameraFragment.pictureCallback = photoHandler;
                //mCameraFragment.makeShutterSound = true;
                mCameraFragment.show(ft, "cameraDialog");

            }
        }).execute();

    }*/
    public void startCalibration(final int index) {

        Context context = getActivity();

        //MainApp mainApp = (MainApp) context.getApplicationContext();

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

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Context context = getActivity().getApplicationContext();

                if (context != null) {
                    MainApp mainApp = ((MainApp) context.getApplicationContext());
                    ArrayList<ColorInfo> colorList = ((MainApp) context).colorList;
                    int index = position * mainApp.rangeIncrementStep;
                    SharedPreferences sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String colorKey = String.format("%d-%s", mTestType, String.valueOf(index));

                    if (resultColor == -1) {
                        editor.remove(colorKey);
                    } else {
                        ColorInfo colorInfo = new ColorInfo(resultColor, accuracy);
                        colorList.set(index, colorInfo);

                        editor.putInt(colorKey, resultColor);
                        editor.putInt(String.format("%d-a-%s", mTestType, String.valueOf(index)),
                                accuracy);

                        if (PreferencesUtils.getBoolean(getActivity(), R.string.oneStepCalibrationKey, false)) {
                            ColorUtils.autoGenerateColorCurve(
                                    mainApp.currentTestType,
                                    mainApp.colorList,
                                    resultColor,
                                    31,
                                    editor);
                        } else {
                            ColorUtils.autoGenerateColors(
                                    index,
                                    mainApp.currentTestType,
                                    mainApp.colorList,
                                    mainApp.rangeIncrementStep, editor);
                        }

                        // if (Config.isExternalFlavor) {
                        //   ColorUtils.autoGenerateColorCurve(mTestType, colorList, resultColor, 31, mainApp.rangeIncrementStep, editor);
                        //} else {
//                        ColorUtils.autoGenerateColors(index, mTestType, colorList, mainApp.rangeIncrementStep, editor);
                        //}
                    }
                    editor.apply();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                updateListView(position);
                displayInfo();
            }
        }).execute();
    }

/*    private boolean validateCalibration(final Message msg) {
        Context context = getActivity();

        //double result = msg.getData().getDouble(MainApp.RESULT_VALUE_KEY, -1);
        int accuracy = msg.getData().getInt(Config.QUALITY_KEY, 0);
        String message = getString(R.string.testFailedMessage);

        int minAccuracy = PreferencesUtils
                .getInt(context, R.string.minPhotoQualityKey, Config.MINIMUM_PHOTO_QUALITY);
        if (accuracy < minAccuracy) {
            message = String.format(getString(R.string.testFailedQualityMessage), minAccuracy);
        }

        if (accuracy < minAccuracy) {

            AlertDialog myDialog;
            View alertView;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            ViewGroup parent = (ViewGroup) getActivity().findViewById(R.id.linearLayout);

            alertView = inflater.inflate(R.layout.dialog_error, parent, false);
            builder.setView(alertView);

            builder.setTitle(R.string.error);

            builder.setMessage(message);

            ImageView image = (ImageView) alertView.findViewById(R.id.image);

            image.setImageBitmap(
                    ImageUtils.getAnalysedBitmap(msg.getData().getString("file"))
            );

            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    calibrate(msg.getData().getInt("position"));
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            builder.setCancelable(false);
            myDialog = builder.create();

            myDialog.show();
            return false;
        }
        return true;

    }*/

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
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Intent LaunchIntent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(Config.CADDISFLY_PACKAGE_NAME);
        if (LaunchIntent != null) {
            //File external = Environment.getExternalStorageDirectory();
            //String path = external.getPath() + "/org.akvo.caddisfly/calibrate/";
            //File folder = new File(path);
            //if (folder.exists()) {
            inflater.inflate(R.menu.calibrate, menu);
            //}
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_swatches:
                SwatchFragment fragment = new SwatchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.container, fragment, "SwatchFragment");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                return true;
            case R.id.menu_load:

                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        displayInfo();
                        return true;
                    }
                };
                mOnLoadCalibrationListener.onLoadCalibration(callback);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnLoadCalibrationListener = (OnLoadCalibrationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadCalibrationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnLoadCalibrationListener = null;
    }

    public interface OnLoadCalibrationListener {
        public void onLoadCalibration(Handler.Callback callback);
    }
}
