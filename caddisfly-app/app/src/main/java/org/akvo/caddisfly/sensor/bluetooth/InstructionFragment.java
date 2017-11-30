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

package org.akvo.caddisfly.sensor.bluetooth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.striptest.widget.PageIndicatorView;
import org.akvo.caddisfly.ui.instructions.InstructionDetailFragment;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class InstructionFragment extends Fragment {

    private final List<Fragment> fragments = new ArrayList<>();
    @BindView(R.id.pager_indicator)
    PageIndicatorView pageIndicatorView;
    @BindView(R.id.image_pageLeft)
    ImageView imagePageLeft;
    @BindView(R.id.image_pageRight)
    ImageView imagePageRight;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InstructionFragment.
     */
    public static InstructionFragment newInstance() {
        return new InstructionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_instructions, container, false);

        ButterKnife.bind(this, view);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        if (fragments.size() < 1) {
            JSONArray instructions = testInfo.getInstructions();

            if (instructions != null) {
                for (int i = 0; i < instructions.length(); i++) {
                    try {

                        Object item = instructions.getJSONObject(i).get("section");
                        JSONArray jsonArray;

                        if (item instanceof JSONArray) {
                            jsonArray = (JSONArray) item;
                        } else {
                            String text = (String) item;
                            jsonArray = new JSONArray();
                            jsonArray.put(text);
                        }

                        fragments.add(InstructionDetailFragment.newInstance(testInfo,
                                jsonArray,
                                instructions.getJSONObject(i).has(SensorConstants.IMAGE)
                                        ? instructions.getJSONObject(i).getString(SensorConstants.IMAGE) : ""));
                    } catch (JSONException e) {
                        Timber.e(e);
                    }
                }

                pageIndicatorView.setPageCount(instructions.length());
            }
        }

        final PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                pageIndicatorView.setActiveIndex(position);
                if (position < 1) {
                    imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    imagePageLeft.setVisibility(View.VISIBLE);
                }
                if (position > pagerAdapter.getCount() - 2) {
                    imagePageRight.setVisibility(View.INVISIBLE);
                } else {
                    imagePageRight.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Nothing to do here
            }
        });

        if (pagerAdapter.getCount() < 2) {
            imagePageLeft.setVisibility(View.GONE);
            imagePageRight.setVisibility(View.GONE);
        }
        return view;
    }

    @OnClick(R.id.image_pageLeft)
    public void pageLeft() {
        mViewPager.setCurrentItem(Math.max(0, mViewPager.getCurrentItem() - 1));
    }

    @OnClick(R.id.image_pageRight)
    void pageRight() {
        mViewPager.setCurrentItem(Math.min(fragments.size() - 1, mViewPager.getCurrentItem() + 1));
    }

    public void returnToFirstPage() {
        mViewPager.setCurrentItem(0, false);
    }

    public int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
