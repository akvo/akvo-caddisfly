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

package org.akvo.caddisfly.sensor.colorimetry.strip.instructions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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

import timber.log.Timber;

/*
This class assumes that there are .png images in res/drawable that have the same name as the brand
*/
public class InstructionActivity extends BaseActivity {

    private final List<Fragment> fragments = new ArrayList<>();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        final PageIndicatorView pageIndicatorView = (PageIndicatorView) findViewById(R.id.pager_indicator);
        final ImageView imagePageLeft = (ImageView) findViewById(R.id.image_pageLeft);
        final ImageView imagePageRight = (ImageView) findViewById(R.id.image_pageRight);

        StripTest.Brand brand = (new StripTest()).getBrand(getIntent().getStringExtra(Constant.UUID));

        setTitle(brand.getName());

        JSONArray instructions = brand.getInstructions();
        if (instructions != null) {
            for (int i = 0; i < instructions.length(); i++) {
                try {

                    Object item = instructions.getJSONObject(i).get("text");
                    JSONArray jsonArray;

                    if (item instanceof JSONArray) {
                        jsonArray = (JSONArray) item;
                    } else {
                        String text = (String) item;
                        jsonArray = new JSONArray();
                        jsonArray.put(text);
                    }

                    fragments.add(InstructionDetailFragment.newInstance(
                            jsonArray,
                            instructions.getJSONObject(i).has("png")
                                    ? instructions.getJSONObject(i).getString("png") : ""));
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            pageIndicatorView.setPageCount(instructions.length());
        }

        mViewPager = (ViewPager) findViewById(R.id.pager);

        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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

            }
        });

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
        if (item.getItemId() == android.R.id.home) {
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
