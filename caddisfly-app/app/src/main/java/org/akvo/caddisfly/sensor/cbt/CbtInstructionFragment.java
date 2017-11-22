package org.akvo.caddisfly.sensor.cbt;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentInstructionCbtBinding;
import org.akvo.caddisfly.databinding.FragmentInstructionsCbtBinding;
import org.akvo.caddisfly.model.TestInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class CbtInstructionFragment extends Fragment {

    private static final String KEY_TEST_INFO = "TestInfo";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FragmentInstructionsCbtBinding mBinding;

    private TestInfo mTestInfo;

    public static CbtInstructionFragment forProduct(TestInfo mTestInfo) {
        CbtInstructionFragment fragment = new CbtInstructionFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TEST_INFO, mTestInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mTestInfo = getArguments().getParcelable(KEY_TEST_INFO);

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_instructions_cbt, container, false);

        mBinding.setCallback(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);

        mBinding.pagerIndicator.setPageCount(mSectionsPagerAdapter.getCount());

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                mBinding.pagerIndicator.setActiveIndex(position);
                if (position < 1) {
                    mBinding.imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.imagePageLeft.setVisibility(View.VISIBLE);
                }
                if (position > mSectionsPagerAdapter.getCount() - 2) {
                    mBinding.imagePageRight.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.imagePageRight.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Nothing to do here
            }
        });

        if (mSectionsPagerAdapter.getCount() < 2) {
            mBinding.imagePageLeft.setVisibility(View.GONE);
            mBinding.imagePageRight.setVisibility(View.GONE);
        }

        return mBinding.getRoot();
    }

    public void onClickLeft() {
        mBinding.viewPager.setCurrentItem(Math.max(0, mBinding.viewPager.getCurrentItem() - 1));
    }

    public void onClickRight() {
        mBinding.viewPager.setCurrentItem(Math.min(mTestInfo.getInstructions().length() - 1,
                mBinding.viewPager.getCurrentItem() + 1));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        FragmentInstructionCbtBinding fragmentInstructionBinding;
        JSONObject instruction;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         *
         * @param instruction : The information to to display
         */
        public static PlaceholderFragment newInstance(JSONObject instruction) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SECTION_NUMBER, instruction.toString());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            fragmentInstructionBinding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_instruction_cbt, container, false);

            try {
                instruction = new JSONObject(getArguments().getString(ARG_SECTION_NUMBER));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            fragmentInstructionBinding.setInstruction(instruction);

            return fragmentInstructionBinding.getRoot();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return PlaceholderFragment.newInstance((JSONObject) mTestInfo.getInstructions().get(position));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mTestInfo.getInstructions().length();
        }
    }
}
