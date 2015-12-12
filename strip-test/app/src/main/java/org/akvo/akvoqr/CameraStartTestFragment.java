package org.akvo.akvoqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.ui.ProgressIndicatorView;
import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;


/**
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraStartTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This fragment shows the progress of the strip test.
 *
 * At the beginning, we start a countdown.
 * We need to know when it is time for each patch to get its picture taken.
 *
 * Then we update the UI:
 * First it shows if quality checks are OK
 *
 * It uses an instance of
 * ProgressIndicatorView to indicate if
 * a. it is time to take a picture
 * b. the picture is taken
 *
 * If all pictures are taken it
 * a. if develop is false: shows an animation of a spinning circle while doing a DetectStripTask
 * b. if develop is true: starts the DetectStripActivity (which does a DetectStripTask)
 */
public class CameraStartTestFragment extends CameraSharedFragment {

    private CameraViewListener mListener;
    private Button startButton;
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
    private TextView countQualityView;
    private QualityCheckView exposureView;
    private QualityCheckView contrastView;

    /*
     * Update the ProgressIndicatorView every second
    */
    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {

            if(progressIndicatorViewAnim!=null && handler!=null) {
                progressIndicatorViewAnim.setTimeLapsed(timeLapsed);
                handler.postDelayed(this, 1000);
            }
            timeLapsed++;

        }
    };


    public static CameraStartTestFragment newInstance(String brandName) {

        CameraStartTestFragment fragment = new CameraStartTestFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    public CameraStartTestFragment() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.fragment_camera_starttest, container, false);
        startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);

        exposureView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewExposure);
        contrastView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewContrast);

        countQualityView = (TextView) rootView.findViewById(R.id.fragment_camera_starttestCountQualityTextView);

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

        if(getArguments()!=null) {

            brandName = getArguments().getString(Constant.BRAND);

            StripTest stripTest = StripTest.getInstance(getActivity());
            //get the patches ordered by time-lapse
            patches = stripTest.getBrand(brandName).getPatchesOrderedByTimelapse();

            progressIndicatorViewAnim = (ProgressIndicatorView) rootView.findViewById(R.id.activity_cameraProgressIndicatorViewAnim);

            //Add a step per time lapse to progressIndicatorView
            for (int i = 0; i < patches.size(); i++) {

                //Skip if there is no timelapse.
                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                        continue;
                    }
                }
                progressIndicatorViewAnim.addStep(i, (int) patches.get(i).getTimeLapse());
            }
        }

        //Prepare handler
        handler = new Handler(Looper.getMainLooper());

        //use brightness view as a button to switch on and off the flash
        //TODO: remove in release version?
        QualityCheckView exposureView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewExposure);
        if(exposureView!=null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.switchFlash();
                }
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CameraViewListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CameraViewListener");
        }

        //reset quality checks count to zero
        if(mListener!=null)
            mListener.setQualityCheckCountZero();
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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        try {

            setHeightOfOverlay(0);

            startCountdown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void showExposure(double value) {

        if(exposureView!=null)
            exposureView.setPercentage((float)value);
    }

    @Override
    protected void showShadow(double value) {

        if(contrastView!=null)
            contrastView.setPercentage((float) value);
    }

    /*
   * Keep track of the time.
   * We start a runnable (countdownRunnable) that update the progress view (afterwards that takes care of itself)
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
        if(handler!=null) {
            handler.post(countdownRunnable);
        }

        //start the CameraPreviewCallback in preview mode (not taking pictures, but doing quality checks
        mListener.startNextPreview(0);

        //Post the CameraPreviewCallback in take picture mode on time for each patch (the posting is done in the CameraActivity itself)
        brandName = getArguments().getString(Constant.BRAND);

        System.out.println("***brandname: " + brandName);

        patches = StripTest.getInstance(getActivity()).getBrand(brandName).getPatchesOrderedByTimelapse();

        for (int i = 0; i < patches.size(); i++) {

            //if next patch is no later than previous, skip.
            if (i > 0) {
                if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                    continue;
                }
            }
            //tell CameraActivity when it is time to take the next picture
            //add 10 milliseconds to avoid it being on same time as preview if timelapse is 0;
            mListener.takeNextPicture((long) patches.get(i).getTimeLapse() * 1000 + 10);

            System.out.println("***posting takeNextPicture: " + i + " timelapse: " + patches.get(i).getTimeLapse());
        }

    }

    /*
    * Update the start button in progressIndicatorView to show green checked box
    * Update progressIndicatorView to set its property: 'start' to true.
    * ProgressIndicatorView depends on start == true to draw on its Canvas and to animate its Views
     */
    @Override
    public void showStartButton() {

        if(startButton==null)
            return;

        startButton.setVisibility(View.VISIBLE);
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked_box, 0, 0, 0);

        if(progressIndicatorViewAnim!=null) {
            progressIndicatorViewAnim.setStart(true);
        }
    }

    /*
    * Update progressIndicatorView with the number of steps that we have a picture of
     */
    public void setStepsTaken(final int number)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(progressIndicatorViewAnim!=null) {
                    progressIndicatorViewAnim.setStepsTaken(number);
                }
            }
        };

        if(handler!=null) {
            handler.post(runnable);
        }

    }

    /*
    * Store image data together with FinderPatternInfo data.
    * Update JSONArray imagePatchArray to keep track of which picture goes with which patch.
     */
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info)
    {
        //check if image count is lower than patches size. if not, abort
        if(imageCount >= patches.size())
            return;

        //what happens in the for-loop:
        //check if picture is taken on time for the patch.
        //assumed is that some tests require time for a color to develop.
        //reading may be done after that time, but not before.

        //we start patchesCovered at -1, because 0 would mean the first patch, and we do not want anything to happen
        // before this loop has run.
        //we need to add 1 to patchesCovered at the start of the for loop, because we use it to get an object from
        // the list of patches, which of course starts counting at 0.
        for(int i = patchesCovered + 1; i < patches.size(); i++) {

            //in case the reading is done after the time lapse we want to save the data for all patches before the time-lapse...
            if (timeMillis > initTimeMillis + patches.get(i).getTimeLapse() * 1000) {

                //keep track of which patches are 'done'
                patchesCovered = i;

                //keep track of stepsCovered ('step' is the patches that have the same time lapse)
                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() > 0) {
                        stepsCovered ++;
                    }
                }
                //keep track of which image belongs to which patch
                JSONArray array = new JSONArray();
                array.put(imageCount);
                array.put(patches.get(i).getOrder());
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

        System.out.println("***xxximageCount: " + imageCount + " stepsCovered: " + stepsCovered + " patchesCovered: " + patchesCovered);

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
    * @params: format, widht, height. Those should be the format, width and height of the Camera.Size
     */
    public void dataSent(int format, int width, int height)
    {

        //set to true if you want to see the original and calibrated images in DetectStripActivity
        //set to false if you want to go to the ResultActivity directly
        boolean develop = false;

        //check if we do have images for all patches
        if(patchesCovered == patches.size()-1)
        {
            //stop the preview callback from repeating itself
            if(mListener!=null)
            {
                mListener.stopCallback(true);
            }
            if(handler!=null)
            {
                handler.removeCallbacks(countdownRunnable);
            }
            //check if we really do have data in the json-array
            if(imagePatchArray.length()>0) {
                //write image/patch info to internal storage
                FileStorage fileStorage = new FileStorage(getActivity());
                fileStorage.writeToInternalStorage(Constant.IMAGE_PATCH, imagePatchArray.toString());

                Intent detectStripIntent = createDetectStripIntent(format, width, height);

                //if develop
                if (develop) {
                    detectStripIntent.setClass(getActivity(), DetectStripActivity.class);
                    startActivity(detectStripIntent);
                } else {
                    new DetectStripTask(getActivity()).execute(detectStripIntent);
                }

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
    private Intent createDetectStripIntent(int format, int width, int height)
    {
        Intent detectStripIntent = new Intent();
        //put Extras into intent
        detectStripIntent.putExtra(Constant.BRAND, brandName);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);

        detectStripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return detectStripIntent;
    }

    /*
    *  Show the number of succesful quality checks as text on the start button
     */
    @Override
    public void countQuality(Map<String, Integer> countMap)
    {

        if(startButton!=null)
        {
            try {

                int count = 0;

                for(int i: countMap.values()){
                    count += Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT / countMap.size(), i);
                }

                count = Math.max(0, Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count));

                startButton.setText("Quality checks: " + String.valueOf(count) + " out of " + Constant.COUNT_QUALITY_CHECK_LIMIT);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        boolean develop = true;

        if(develop) {
            countQualityView.setText("");
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                countQualityView.append(entry.getKey() + ": " + entry.getValue() + " ");
            }
        }
    }
}
