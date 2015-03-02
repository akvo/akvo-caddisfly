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

package org.akvo.caddisfly.ui;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.PreferencesUtils;
import org.akvo.caddisfly.util.SoundPoolPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link org.akvo.caddisfly.ui.CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link org.akvo.caddisfly.ui.CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public Camera.PictureCallback pictureCallback;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int samplingCount;
    private OnFragmentInteractionListener mListener;
    private SoundPoolPlayer sound;
    private boolean mCancelled = false;
    private AlertDialog progressDialog;
    private Camera mCamera;

    //    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
    // View to display the camera output.
    private CameraPreview mPreview;

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sound = new SoundPoolPlayer(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //if (PreferencesUtils.getBoolean(getActivity(), R.string.autoAnalyzeKey, true)) {
        getDialog().setTitle(R.string.analysisInProgress);
        //} else {
        //  getDialog().setTitle(R.string.clickAnalyze);
        //}

        final View view = inflater.inflate(R.layout.fragment_camera, container, false);

        // Create preview and set it as the content
        boolean opened = safeCameraOpenInView(view);

        if (!opened) {
            return view;
        }

//        Button captureButton = (Button) view.findViewById(R.id.button_capture);
//        captureButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startTakingPictures();
//                    }
//                }
//        );
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PreferencesUtils.getBoolean(getActivity(), R.string.autoAnalyzeKey, true)) {
            startTakingPictures();
        }
    }

    private void startTakingPictures() {

        samplingCount = 0;
        Context context = getActivity();
        progressDialog = new AlertDialog.Builder(context).create();
        progressDialog.setMessage(getString(R.string.analyzingWait));
        progressDialog.setCancelable(false);

        //ProgressDialog.Builder builder = new ProgressDialog.Builder(context);
        //builder.setTitle(R.string.working);
        //builder.setMessage(R.string.analyzingWait);

        //progressDialog = builder.create();
        progressDialog.getWindow().setGravity(Gravity.BOTTOM);
        progressDialog.show();
        //progressDialog.setCancelable(false);

        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

        mCamera.setParameters(parameters);

        takePicture();
    }

    private void takePicture() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (getActivity() != null) {
                    mPreview.startCameraPreview();
                    PictureCallback localCallback = new PictureCallback();
                    try {
                        mCamera.takePicture(null, null, localCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, Config.INITIAL_DELAY);
    }

    public void stopCamera() {
        mCancelled = true;
        releaseCameraAndPreview();
    }

    public boolean hasTestCompleted() {
        return samplingCount > 5;
//        int currentSamplingCount = PreferencesUtils
//                .getInt(context, R.string.currentSamplingCountKey, 0);
//        int samplingCount = PreferencesUtils
//                .getInt(context, R.string.samplingCountKey, Config.SAMPLING_COUNT_DEFAULT);
//        return currentSamplingCount >= (samplingCount - 1);
    }

    private boolean safeCameraOpenInView(View view) {
        boolean qOpened;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        qOpened = (mCamera != null);

        if (qOpened) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() != null && getActivity() instanceof Cancelled) {
            ((Cancelled) getActivity()).dialogCancelled();
        }
        super.onCancel(dialog);
    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        sound.release();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public interface Cancelled {
        public void dialogCancelled();
    }

    static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private final SurfaceHolder mHolder;

        private Camera mCamera;

        private Camera.Size mPreviewSize;

        private List<Camera.Size> mSupportedPreviewSizes;

        private List<String> mSupportedFlashModes;

        public CameraPreview(Context context) {
            super(context);
            mHolder = getHolder();
        }

        public CameraPreview(Context context, Camera camera) {
            super(context);

            setCamera(context, camera);

            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
        }

        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setCamera(Context context, Camera camera) {
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
            parameters.setZoom(0);

            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<>();
                Rect areaRect1 = new Rect(-100, -100, 100, 100);
                meteringAreas.add(new Camera.Area(areaRect1, 1000));
                parameters.setMeteringAreas(meteringAreas);
            }

            if (!PreferencesUtils.getBoolean(context, R.string.autoAnalyzeKey, false)) {
                if (mSupportedFlashModes != null && mSupportedFlashModes
                        .contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }

            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);

            requestLayout();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

            if (mHolder.getSurface() == null) {
                return;
            }

            try {
                Camera.Parameters parameters = mCamera.getParameters();

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                if (mPreviewSize != null) {
                    Camera.Size previewSize = mPreviewSize;
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                }

                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            //final int width = Globals.IMAGE_SAMPLE_LENGTH;
            //final int height = Globals.IMAGE_SAMPLE_LENGTH;
            //final int length = Math.min(width, height);

            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
            Camera.Size optimalSize = null;

            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) height / width;

            for (Camera.Size size : sizes) {

                if (size.height != width) {
                    continue;
                }
                double ratio = (double) size.width / size.height;
                if (ratio <= targetRatio + ASPECT_TOLERANCE
                        && ratio >= targetRatio - ASPECT_TOLERANCE) {
                    optimalSize = size;
                }
            }

            return optimalSize;
        }
    }

    private class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            samplingCount++;
            if (!mCancelled) {
                if (!hasTestCompleted()) {
                    pictureCallback.onPictureTaken(bytes, camera);
                    sound.playShortResource(R.raw.beep);
                    takePicture();
                } else {
                    pictureCallback.onPictureTaken(bytes, camera);
                }
            }
        }
    }
}
