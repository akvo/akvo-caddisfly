package org.akvo.akvoqr.choose_striptest;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.Constant;

import java.io.InputStream;

/**
 * Created by linda on 9/12/15.
 */
public class ChooseStripTestDetailFragment extends Fragment {

    private Callbacks mCallbacks;

    public static ChooseStripTestDetailFragment newInstance(String brandName) {
        ChooseStripTestDetailFragment fragment = new ChooseStripTestDetailFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    public ChooseStripTestDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("*** ChooseStripTestDetailFragment onCreateView called with Arguments: " + getArguments());

        View rootView = inflater.inflate(R.layout.fragment_choose_strip_test, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.fragment_choose_strip_testImageView);

        if(getArguments()!=null) {
            final String brandName = getArguments().getString(Constant.BRAND);

            System.out.println("***brandname ChooseStripTestDetailFragment onCreateView: " + brandName);

            if (brandName != null) {

                try {
                    //images in assets
                    // get input stream
                    String path = getResources().getString(R.string.striptest_images);
                    InputStream ims = getActivity().getAssets().open(path + "/" + brandName + ".png");
                    // load image as Drawable

                    Drawable drawable = Drawable.createFromStream(ims, null);

                    ims.close();

                    // set image to ImageView
                    imageView.setImageDrawable(drawable);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                Button button = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButtonPerform);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.startCameraActivity(brandName);
                    }
                });

                Button buttonInstruction = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButtonInstruction);
                buttonInstruction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.startInstructionActivity(brandName);
                    }
                });

                AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
                if (appCompatActivity != null) {
                    StripTest stripTest = new StripTest();
                    appCompatActivity.getSupportActionBar().setTitle(stripTest.getBrand(getActivity(), brandName).getName());
                }
            }
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = null;
    }

    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void startCameraActivity(String brandname);
        void startInstructionActivity(String brandname);
    }



}
