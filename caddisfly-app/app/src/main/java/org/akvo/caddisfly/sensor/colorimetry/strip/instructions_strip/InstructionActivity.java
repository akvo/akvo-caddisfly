package org.akvo.caddisfly.sensor.colorimetry.strip.instructions_strip;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
This class assumes that there are .png images in res/drawable that have the same name
as the String 'brand' in the JsonObject 'strip' in strips.json from assets
*/
public class InstructionActivity extends BaseActivity implements InstructionsListener {

    private final List<Fragment> fragments = new ArrayList<>();
    private ViewPager mViewPager;
    private InstructionFooterView footerView;
    private ImageView arrowLeft;
    private ImageView arrowRight;
    private JSONArray instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        int countFragments = 0;
        try {

            StripTest stripTest = new StripTest();
            StripTest.Brand brand = stripTest.getBrand(getIntent().getStringExtra(Constant.BRAND));

            setTitle(brand.getName());

            instructions = brand.getInstructions();

            for (int i = 0; i < instructions.length(); i++) {
                fragments.add(InstructionBrandDetailFragment.newInstance(countFragments));
                countFragments++;
            }

            footerView = (InstructionFooterView) findViewById(R.id.activity_instructionFooterView);
            footerView.setNumSteps(countFragments);

            arrowLeft = (ImageView) findViewById(R.id.activity_instructionFooterArrowLeft);
            arrowRight = (ImageView) findViewById(R.id.activity_instructionFooterArrowRight);


        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(sectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                footerView.setActive(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                int itemId = Math.max(0, mViewPager.getCurrentItem() - 1);
                mViewPager.setCurrentItem(itemId);
            }
        });

        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                int itemId = Math.min(fragments.size() - 1, mViewPager.getCurrentItem() + 1);
                mViewPager.setCurrentItem(itemId);
            }
        });
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

    //
    @Override
    public String getInstruction(int id) throws JSONException {
        return instructions.getJSONObject(id).getString("text");
    }

    @Override
    public Drawable getInstructionDrawable(int id) throws JSONException {
        String resName = instructions.getJSONObject(id).getString("png");

        String path = getResources().getString(R.string.instruction_images);

        path = getPathToDrawable(path, resName);

        try {
            // get input stream
            InputStream ims = getAssets().open(path + "/" + resName.toLowerCase(Locale.US) + ".png");
            // load image as Drawable
            // set image to ImageView
            return Drawable.createFromStream(ims, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //change path to file in assets if file is available for locale
    private String getPathToDrawable(String path, String resName) {
        String localeLanguage = Locale.getDefault().getLanguage();
        try {

            getAssets().open(path + "-" + localeLanguage + "/" + resName.toLowerCase(Locale.US) + ".png");

            path += "-" + localeLanguage;
        } catch (IOException ex) {
            //ignore
        }
        return path;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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
            Locale l = Locale.getDefault();

            return String.valueOf(position + 1).toUpperCase(l);

        }
    }
}
