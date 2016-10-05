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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.detect.DetectStripTask;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.PercentageMeterView;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.ProgressIndicatorView;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.json.JSONArray;

import java.util.List;

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
 * It uses an instance of
 * ProgressIndicatorView to indicate if
 * a. it is time to take a picture
 * b. the picture is taken
 * <p/>
 * If all pictures are taken it shows an animation of a spinning circle while doing a DetectStripTask
 */
public class CameraStartTestFragment extends CameraSharedFragmentBase {

    private CameraViewListener mListener;
    private List<StripTest.Brand.Patch> patches;
    private ProgressIndicatorView progressIndicatorViewAnim;
    private int timeLapsed = 0;
    private Handler handler;
    private String brandName;
    private int patchesCovered = -1;
    private int stepsCovered = 0;
    private int imageCount = 0;
    private JSONArray imagePatchArray = new JSONArray();
    private long initTimeMillis;
    /*
     * Update the ProgressIndicatorView every second
    */
    private final Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {

            if (progressIndicatorViewAnim != null && handler != null) {

                timeLapsed = (int) (Math.floor((System.currentTimeMillis() - initTimeMillis) / 1000d));
                progressIndicatorViewAnim.setTimeLapsed(timeLapsed);
                handler.postDelayed(this, 1000);

            }
        }
    };
    //private TextView countQualityView;
    private PercentageMeterView exposureView;
    private PercentageMeterView contrastView;
    private ImageView finishImage;
    private Animation rotate;


    public CameraStartTestFragment() {
        // Required empty public constructor
    }

    public static CameraStartTestFragment newInstance(String brandName) {

        CameraStartTestFragment fragment = new CameraStartTestFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_start, container, false);

        exposureView = (PercentageMeterView) rootView.findViewById(R.id.quality_brightness);
        contrastView = (PercentageMeterView) rootView.findViewById(R.id.quality_shadows);
        countQualityView = (TextView) rootView.findViewById(R.id.text_startIndicator);
        finishImage = (ImageView) rootView.findViewById(R.id.image_finishIndicator);

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

        if (getArguments() != null) {

            brandName = getArguments().getString(Constant.BRAND);

            StripTest stripTest = new StripTest();
            //get the patches ordered by time-lapse
            patches = stripTest.getBrand(brandName).getPatchesOrderedByTimeLapse();

            progressIndicatorViewAnim = (ProgressIndicatorView) rootView.findViewById(R.id.progress_indicator);

            //Add a step per time lapse to progressIndicatorView
            for (int i = 0; i < patches.size(); i++) {

                //Skip if there is no timeLapse.
                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                        continue;
                    }
                }
                progressIndicatorViewAnim.addStep((int) patches.get(i).getTimeLapse());
            }
        }

        //Prepare handler
        handler = new Handler(Looper.getMainLooper());

        //use brightness view as a button to switch on and off the flash
        //TODO: remove in release version?
        PercentageMeterView exposureView = (PercentageMeterView) rootView.findViewById(R.id.quality_brightness);
        if (exposureView != null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.toggleFlashMode(true);
                }
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CameraViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CameraViewListener");
        }

        //reset quality checks count to zero
        if (mListener != null)
            mListener.setQualityCheckCountZero();

        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);

        mListener.startPreview();
        if (mListener.isTorchModeOn()) {
            mListener.toggleFlashMode(false);
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

        try {

            setHeightOfOverlay(0);

            startCountdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.removeCallbacks(countdownRunnable);
        }
        super.onPause();
    }

    @Override
    protected void showBrightness(double value) {

        if (exposureView != null) {
            exposureView.setPercentage((float) (100 - value));
        }
    }

    @Override
    protected void showShadow(double value) {

        if (contrastView != null) {
            contrastView.setPercentage((float) value);
        }
    }

    /*
   * Keep track of the time.
   * We start a runnable (countdownRunnable) that update the partial_progress view (afterwards that takes care of itself)
   * We know beforehand at what intervals the patches are due from the values in strips.json
   * so we can use a handler and runnable to post them at exactly that interval from start countdown
    */
    private void startCountdown() {

        //reset
        imageCount = 0;
        patchesCovered = -1;
        stepsCovered = 0;
        imagePatchArray = new JSONArray();

        //reset timeLapsed to zero just to make sure
        timeLapsed = 0;

        //initialise the global initial time
        initTimeMillis = System.currentTimeMillis();

        //post the runnable responsible for updating the
        // ProgressIndicatorView
        if (handler != null) {
            handler.post(countdownRunnable);
        }

        //start the CameraPreviewCallback in preview mode (not taking pictures, but doing quality checks
        //mListener.startNextPreview(0);

        //Post the CameraPreviewCallback in take picture mode on time for each patch (the posting is done in the CameraActivity itself)
        brandName = getArguments().getString(Constant.BRAND);

        System.out.println("***brandName: " + brandName);

        StripTest stripTest = new StripTest();

        patches = stripTest.getBrand(brandName).getPatchesOrderedByTimeLapse();

        for (int i = 0; i < patches.size(); i++) {

            //if next patch is no later than previous, skip.
            if (i > 0) {
                if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                    continue;
                }
            }
            //tell CameraActivity when it is time to take the next picture
            //add 10 milliseconds to avoid it being on same time as preview if timeLapse is 0;
            mListener.takeNextPicture((long) patches.get(i).getTimeLapse() * 1000 + 10);

            System.out.println("***posting takeNextPicture: " + i + " timeLapse: " + patches.get(i).getTimeLapse());
        }

    }

    /*
    * Update the start button in progressIndicatorView to show green checked box
    * Update progressIndicatorView to set its property: 'start' to true.
    * ProgressIndicatorView depends on start == true to draw on its Canvas and to animate its Views
     */
    @Override
    public void goNext() {

        countQualityView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked_box, 0, 0, 0);

        if (progressIndicatorViewAnim != null) {
            progressIndicatorViewAnim.start();
        }
    }

    /*
    * Update progressIndicatorView with the number of steps that we have a picture of
     */
    private void setStepsTaken(final int number) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progressIndicatorViewAnim != null) {
                    progressIndicatorViewAnim.setStepsTaken(number);
                }
            }
        };

        if (handler != null) {
            handler.post(runnable);
        }

    }

    /*
    * Store image data together with FinderPatternInfo data.
    * Update JSONArray imagePatchArray to keep track of which picture goes with which patch.
     */
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info) {
        //check if image count is lower than patches size. if not, abort
        if (imageCount >= patches.size())
            return;

        //check if picture is taken on time for the patch.
        //assumed is that some tests require time for a color to develop.
        //reading may be done after that time, but not before.

        //we start patchesCovered at -1, because 0 would mean the first patch, and we do not want anything to happen
        // before this loop has run.
        //we need to add 1 to patchesCovered at the start of the for loop, because we use it to get an object from
        // the list of patches, which of course starts counting at 0.
        for (int i = patchesCovered + 1; i < patches.size(); i++) {

            //in case the reading is done after the time lapse we want to save the data for all patches before the time-lapse...
            if (timeMillis > initTimeMillis + patches.get(i).getTimeLapse() * 1000) {

                //keep track of which patches are 'done'
                patchesCovered = i;

                //keep track of stepsCovered ('step' is the patches that have the same time lapse)
                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() > 0) {
                        stepsCovered++;
                    }
                }
                //keep track of which image belongs to which patch
                JSONArray array = new JSONArray();
                array.put(imageCount);
                array.put(patches.get(i).getId());
                imagePatchArray.put(array);

            }
        }

        //update UI
        setStepsTaken(stepsCovered);

        //store the data as file in internal memory using the value of imageCount as part of its name,
        //at the same time store the data that the FinderPatternInfo object contains.
        //The two files can be retrieved and combined later, because they share 'imageCount'
        // and imagePatchArray contains a value that corresponds with that.
        new StoreDataTask(getActivity(), imageCount, data, info).execute();

        System.out.println("***imageCount: " + imageCount + " stepsCovered: " + stepsCovered + " patchesCovered: " + patchesCovered);

        //add one to imageCount
        imageCount++;
    }

    public void showSpinner() {
        if (finishImage != null) {
            finishImage.setImageResource(R.drawable.spinner);
            finishImage.startAnimation(rotate);
        }
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
    public void dataSent(int format, int width, int height) {

        //check if we do have images for all patches
        if (patchesCovered == patches.size() - 1) {
            //stop the preview callback from repeating itself
            if (mListener != null) {
                mListener.stopCallback();
            }

            //check if we really do have data in the json-array
            if (imagePatchArray.length() > 0) {
                //write image/patch info to internal storage
                FileStorage fileStorage = new FileStorage(getActivity());
                fileStorage.writeToInternalStorage(Constant.IMAGE_PATCH, imagePatchArray.toString());

                Intent detectStripIntent = createDetectStripIntent(format, width, height);

                new DetectStripTask(getActivity()).execute(detectStripIntent);
            }
        }
    }

    /*
    * Create an Intent that holds information about the preview data:
    * preview format
    * preview width
    * preview height
    *
    * and information about the strip test brand we are now handling
    *
    * It is used to
    * a. start an Activity with this intent
    * b. start an AsyncTask passing this intent as param
    *
    * in the method dataSent() above
     */
    private Intent createDetectStripIntent(int format, int width, int height) {
        Intent detectStripIntent = new Intent();
        //put Extras into intent
        detectStripIntent.putExtra(Constant.BRAND, brandName);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);

        detectStripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return detectStripIntent;
    }
}
