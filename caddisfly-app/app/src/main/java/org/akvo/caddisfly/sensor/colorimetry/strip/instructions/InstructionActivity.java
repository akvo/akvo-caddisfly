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

package org.akvo.caddisfly.sensor.colorimetry.strip.instructions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.widget.PageIndicatorView;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/*
This class assumes that there are .png images in res/drawable that have the same name as the brand
*/
public class InstructionActivity extends BaseActivity {

    private static final String TAG = "InstructionActivity";
    private final List<Fragment> fragments = new ArrayList<>();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        final PageIndicatorView footerView = (PageIndicatorView) findViewById(R.id.pager_indicator);

        StripTest.Brand brand = (new StripTest()).getBrand(this, getIntent().getStringExtra(Constant.UUID));

        setTitle(brand.getName());

        JSONArray instructions = brand.getInstructions();
        if (instructions != null) {
            for (int i = 0; i < instructions.length(); i++) {
                try {
                    fragments.add(InstructionDetailFragment.newInstance(
                            instructions.getJSONObject(i).getString("text"),
                            instructions.getJSONObject(i).has("png")
                                    ? instructions.getJSONObject(i).getString("png") : ""));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            footerView.setPageCount(instructions.length());
        }

        mViewPager = (ViewPager) findViewById(R.id.pager);

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                footerView.setActiveIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImageView imagePageLeft = (ImageView) findViewById(R.id.image_pageLeft);
        ImageView imagePageRight = (ImageView) findViewById(R.id.image_pageRight);

        imagePageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(Math.max(0, mViewPager.getCurrentItem() - 1));
            }
        });

        imagePageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(Math.min(fragments.size() - 1, mViewPager.getCurrentItem() + 1));
            }
        });

        if (pagerAdapter.getCount() < 2) {
            imagePageLeft.setVisibility(View.GONE);
            imagePageRight.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

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
