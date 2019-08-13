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

package org.akvo.caddisfly.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.databinding.FragmentInstructionsBinding;
import org.akvo.caddisfly.helper.InstructionHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;

import java.util.ArrayList;

public class InstructionFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FragmentInstructionsBinding b;

    private ArrayList<Instruction> instructionList = new ArrayList<>();

    public static InstructionFragment getInstance(Parcelable testInfo) {
        InstructionFragment fragment = new InstructionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(inflater, R.layout.fragment_instructions, container, false);

        if (getArguments() != null) {
            TestInfo testInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);
            if (testInfo != null) {
                InstructionHelper.setupInstructions(testInfo.getInstructions(), instructionList);
            }
            b.imagePageRight.setOnClickListener(view ->
                    b.viewPager.setCurrentItem(Math.min(instructionList.size() - 1,
                            b.viewPager.getCurrentItem() + 1)));
        }

        b.setCallback(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        b.viewPager.setAdapter(mSectionsPagerAdapter);

        b.imagePageLeft.setOnClickListener(view ->
                b.viewPager.setCurrentItem(Math.max(0, b.viewPager.getCurrentItem() - 1)));

        b.pagerIndicator.showDots(true);
        b.pagerIndicator.setPageCount(mSectionsPagerAdapter.getCount());

        b.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                b.pagerIndicator.setActiveIndex(position);
                if (position < 1) {
                    b.imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    b.imagePageLeft.setVisibility(View.VISIBLE);
                }
                if (position > mSectionsPagerAdapter.getCount() - 2) {
                    b.imagePageRight.setVisibility(View.INVISIBLE);
                } else {
                    b.imagePageRight.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Nothing to do here
            }
        });

        if (mSectionsPagerAdapter.getCount() < 2) {
            b.imagePageLeft.setVisibility(View.GONE);
            b.imagePageRight.setVisibility(View.GONE);
        }

        return b.getRoot();
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
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            fragmentInstructionBinding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_instruction, container, false);

            if (getArguments() != null) {
                instruction = getArguments().getParcelable(ARG_SECTION_NUMBER);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            return fragmentInstructionBinding.getRoot();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(instructionList.get(position));
        }

        @Override
        public int getCount() {
            return instructionList.size();
        }
    }
}
