package org.akvo.akvoqr.choose_striptest;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.akvo.akvoqr.CameraActivity;
import org.akvo.akvoqr.R;
import org.akvo.akvoqr.instructions_app.InstructionActivity;
import org.akvo.akvoqr.util.Constant;

import java.io.InputStream;

/**
 * Created by linda on 9/12/15.
 */
public class ChooseStripTestDetailFragment extends Fragment {

    private String brandName;
    private ImageView imageView;
    private Drawable drawable;

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

            if (getArguments() != null) {

                this.brandName = getArguments().getString(Constant.BRAND);

                //images in assets
                try {

                    if (drawable == null) {
                        // get input stream
                        String path = getActivity().getResources().getString(R.string.striptest_images);
                        InputStream ims = getActivity().getAssets().open(path + "/" + brandName + ".png");
                        // load image as Drawable

                        drawable = Drawable.createFromStream(ims, null);

                        ims.close();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_choose_strip_test, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.fragment_choose_strip_testImageView);

        // set image to ImageView
        if(drawable!=null) {
            imageView.setImageDrawable(drawable);
        }

        if(brandName!=null) {
            Button button = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButtonPerform);
            button.setOnClickListener(new ChooseBrandOnClickListener(brandName));

            Button buttonInstruction = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButtonInstruction);
            buttonInstruction.setOnClickListener(new ShowInstructionsOnClickListener(brandName));

            AppCompatActivity appCompatActivity = ((AppCompatActivity) getActivity());
            if(appCompatActivity!=null) {
                StripTest stripTest = StripTest.getInstance(getActivity());
                appCompatActivity.getSupportActionBar().setTitle(stripTest.getBrand(brandName).getName());
            }
        }
        return rootView;
    }

    private class ChooseBrandOnClickListener implements View.OnClickListener{

        private String brand;

        public ChooseBrandOnClickListener(String brand)
        {
            this.brand = brand;
        }

        @Override
        public void onClick(View v) {
            v.setActivated(!v.isActivated());
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtra(Constant.BRAND, brand);
            startActivity(intent);
        }
    }

    private class ShowInstructionsOnClickListener implements View.OnClickListener{

        private String brandName;
        public ShowInstructionsOnClickListener(String brandName)
        {
            this.brandName = brandName;
        }
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(getActivity(), InstructionActivity.class);
            intent.putExtra(Constant.BRAND, brandName);
            startActivity(intent);

        }
    }

}
