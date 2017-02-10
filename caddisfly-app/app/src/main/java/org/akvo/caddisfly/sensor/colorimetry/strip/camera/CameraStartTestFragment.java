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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestStatus;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.PercentageMeterView;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import timber.log.Timber;

/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraStartTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * This fragment shows the partial_progress of the strip test.
 * <p/>
 * At the beginning, we start a countdown.
 * We need to know when it is time for each patch to get its picture taken.
 * <p/>
 * Then we update the UI:
 * First it shows if quality checks are OK
 * <p/>
 */
public class CameraStartTestFragment extends CameraSharedFragmentBase {

    private static final int SHADOW_UNKNOWN_VALUE = 101;
    @Nullable
    private CameraViewListener mListener;
    private List<StripTest.Brand.Patch> patches;
    private int timeLapsed = 0;
    private Handler handler;
    @Nullable
    private String uuid;
    private int patchesCovered = -1;
    private int imageCount = 0;
    @NonNull
    private JSONArray imagePatchArray = new JSONArray();
    private long initTimeMillis;
    /*
     * Update the progress every second
    */
    @Nullable
    private final Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {

            if (handler != null) {
                timeLapsed = (int) (Math.floor((System.currentTimeMillis() - initTimeMillis) / 1000d));
                handler.postDelayed(this, 1000);
            }

            if (getStatus() == TestStatus.QUALITY_CHECK_DONE) {
                if (imagePatchArray.length() < patches.size()) {
                    if (mListener != null) {
                        int secondsLeft = (int) (Math.max(0, patches.get(patchesCovered + 1).getTimeLapse() - timeLapsed));

                        mListener.showCountdownTimer(secondsLeft,
                                patches.get(patchesCovered + 1).getTimeLapse()
                                        - (patchesCovered >= 0 ? patches.get(patchesCovered).getTimeLapse() : 0));

                        if (secondsLeft > Constant.GET_READY_SECONDS) {
                            showMessage(getString(R.string.waiting_for, patches.get(patchesCovered + 1).getDesc()));
                        } else if (secondsLeft <= 2) {
                            showMessage(R.string.taking_photo);
                        } else {
                            showMessage(R.string.ready_for_photo);
                        }
                    }
                } else {
                    showMessage(R.string.taking_photo);
                }
            }
        }
    };
    private PercentageMeterView exposureView;
    private PercentageMeterView contrastView;
    private boolean secondPhase;

    public CameraStartTestFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static CameraStartTestFragment newInstance(String uuid) {

        CameraStartTestFragment fragment = new CameraStartTestFragment();
        Bundle args = new Bundle();
        args.putString(Constant.UUID, uuid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(false);
    }

    /*
    * Set global properties with the values we now know.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_start, container, false);

        exposureView = (PercentageMeterView) rootView.findViewById(R.id.quality_brightness);
        contrastView = (PercentageMeterView) rootView.findViewById(R.id.quality_shadows);

        //************ HACK FOR TESTING ON EMULATOR ONLY *********************
//        TextView finishTextView = (TextView) rootView.findViewById(R.id.activity_cameraFinishText);
//        finishTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent detectStripIntent = createDetectStripIntent(17,800, 400);
//
//                if (1==0) {
//                    detectStripIntent.setClass(getActivity(), DetectStripActivity.class);
//                    startActivity(detectStripIntent);
//                } else {
//                    new DetectStripTask(getActivity()).execute(detectStripIntent);
//                }
//
//            }
//        });
        // ************* END HACK FOR TESTING ******************

        //Prepare handler
        handler = new Handler(Looper.getMainLooper());

        //use brightness view as a button to switch on and off the flash
        if (AppPreferences.isDiagnosticMode() && exposureView != null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.toggleFlashMode(true);
                    }
                }
            });
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {

            uuid = getArguments().getString(Constant.UUID);

            StripTest stripTest = new StripTest();
            //get the patches ordered by time-lapse
            patches = stripTest.getBrand(uuid).getPatches();
        }

        showBrightness(-1);
        showShadow(SHADOW_UNKNOWN_VALUE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mListener = (CameraViewListener) context;

        //reset quality checks count to zero
        if (mListener != null) {
            mListener.setQualityCheckCountZero();

            mListener.startPreview();
            if (mListener.isTorchModeOn()) {
                mListener.toggleFlashMode(false);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
    * When we can be sure that we have a parent Activity, we use its size to calculate the
    * size we want this fragment to have.
    * Then we start a countdown.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startCountdown();
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.removeCallbacks(countdownRunnable);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (secondPhase && mListener != null) {
            mListener.takeNextPicture((long) 1000);
        }
    }

    @Override
    void showBrightness(double value) {

        if (exposureView != null) {
            exposureView.setPercentage((float) (100 - value));
        }
    }

    @Override
    void showShadow(double value) {

        if (contrastView != null) {
            contrastView.setPercentage((float) value);
        }
    }

    /*
   * Keep track of the time.
   * We start a runnable (countdownRunnable) that update the partial_progress view (afterwards that takes care of itself)
   * We know beforehand at what intervals the patches are due from the values in tests config json
   * so we can use a handler and runnable to post them at exactly that interval from start countdown
    */
    private void startCountdown() {

        //reset
        imageCount = 0;
        patchesCovered = -1;
        imagePatchArray = new JSONArray();

        //reset timeLapsed to zero just to make sure
        timeLapsed = 0;

        //initialise the global initial time
        initTimeMillis = System.currentTimeMillis();

        //post the runnable responsible for updating progress
        if (handler != null) {
            handler.post(countdownRunnable);
        }

        uuid = getArguments().getString(Constant.UUID);

        StripTest stripTest = new StripTest();

        patches = stripTest.getBrand(uuid).getPatches();

        for (int i = 0; i < patches.size(); i++) {

            //if next patch is no later than previous, skip.
            if (i > 0 && patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                continue;
            }

            //tell CameraActivity when it is time to take the next picture
            //add 10 milliseconds to avoid it being on same time as preview if timeLapse is 0;
            if (mListener != null && patches.get(i).getPhase() < 2) {
                mListener.takeNextPicture((long) patches.get(i).getTimeLapse() * 1000 + 10);
            }
        }
    }

    /*
    * Store image data together with FinderPatternInfo data.
    * Update JSONArray imagePatchArray to keep track of which picture goes with which patch.
     */
    public void sendData(final byte[] data, long timeMillis, final FinderPatternInfo info) {

        //check if image count is lower than patches size. if not, abort
        if (imageCount >= patches.size()) {
            return;
        }

        //check if picture is taken on time for the patch.
        //assumed is that some tests require time for a color to develop.
        //reading may be done after that time, but not before.

        //we start patchesCovered at -1, because 0 would mean the first patch, and we do not want anything to happen
        // before this loop has run.
        //we need to add 1 to patchesCovered at the start of the for loop, because we use it to get an object from
        // the list of patches, which of course starts counting at 0.
        for (int i = 0; i < patches.size(); i++) {

            if (!secondPhase && patches.get(i).getPhase() > 1) {
                continue;
            }

            //in case the reading is done after the time lapse we want to save the data for all patches before the time-lapse...
            if (timeMillis > initTimeMillis + patches.get(i).getTimeLapse() * 1000) {

                boolean found = false;
                try {
                    for (int j = 0; j < imagePatchArray.length(); j++) {
                        JSONArray temp = (JSONArray) imagePatchArray.get(j);
                        if (temp.getInt(1) == patches.get(i).getId()) {
                            found = true;
                        }
                    }
                } catch (JSONException e) {
                    Timber.e(e);
                }

                if (!found) {
                    //keep track of which image belongs to which patch
                    JSONArray array = new JSONArray();
                    array.put(imageCount);
                    array.put(patches.get(i).getId());
                    imagePatchArray.put(array);

                    //keep track of which patches are 'done'
                    patchesCovered = imagePatchArray.length() - 1;
                }


            }
        }

        //store the data as file in internal memory using the value of imageCount as part of its name,
        //at the same time store the data that the FinderPatternInfo object contains.
        //The two files can be retrieved and combined later, because they share 'imageCount'
        // and imagePatchArray contains a value that corresponds with that.
        new StoreDataTask(getActivity(), imageCount, data, info).execute();

        //add one to imageCount
        imageCount++;
    }

    /*
    * If picture data (Camera Preview data) is stored,
    * proceed to calibrate and detect the strip from it.
    * That happens in the
    * {@link DetectStripTask} (AsyncTask).
    * For development purposes: if you set the boolean 'develop' to true,
    * you can see images of the original and the calibrated images in a separate activity
    * Otherwise, the whole process runs in the onBackground() of the AsyncTask.
    *
    * Even though it may be called any time, we take care that the detect-strip-process only starts
    * if all patches are 'covered', meaning that we have counted that number beforehand,
    * in the 'sendData' method above.
    *
    * @params: format, width, height. Those should be the format, width and height of the Camera.Size
     */
    public boolean dataSent() {

        //check if we do have images for all patches
        if (imagePatchArray.length() == patches.size()) {
            //stop the preview callback from repeating itself
            if (mListener != null) {
                mListener.stopCallback();
            }

            //check if we really do have data in the json-array
            if (imagePatchArray.length() > 0) {
                //write image/patch info to internal storage
                FileUtil.writeToInternalStorage(getActivity(), Constant.IMAGE_PATCH, imagePatchArray.toString());
                return true;
            }
        } else if (imagePatchArray.length() == 3) {
            if (handler != null) {
                handler.removeCallbacks(countdownRunnable);
            }

            secondPhase = true;
            if (mListener != null) {
                mListener.nextFragment();
            }
        }
        return false;
    }
}
