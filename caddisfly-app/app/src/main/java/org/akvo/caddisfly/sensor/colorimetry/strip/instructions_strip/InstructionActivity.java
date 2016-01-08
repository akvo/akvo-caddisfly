package org.akvo.caddisfly.sensor.colorimetry.strip.instructions_strip;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.ColorimetryStripDetailActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstructionActivity extends AppCompatActivity implements  InstructionsListener{

    /**
     * This class assumes that there are .png images in res/drawable that have the same name
     * as the String 'brand' in the JsonObject 'strip' in strips.json from assets
     */

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    InstructionFooterView footerView;
    ImageView arrowLeft;
    ImageView arrowRight;
    JSONArray instructions;

    List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        int countFragments = 0;
        try {

            StripTest stripTest = new StripTest();
            StripTest.Brand brand = stripTest.getBrand(this, getIntent().getStringExtra(Constant.BRAND));
            instructions = brand.getInstructions();

            for(int i=0;i<instructions.length();i++) {
                fragments.add(InstructionBrandDetailFragment.newInstance(countFragments));
                countFragments ++;
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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
                footerView.setActive(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }

        arrowLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                int itemid = Math.max(0, mViewPager.getCurrentItem() - 1);
                mViewPager.setCurrentItem(itemid);
            }
        });

        arrowRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                int itemid = Math.min(fragments.size() - 1, mViewPager.getCurrentItem() + 1);
                mViewPager.setCurrentItem(itemid);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_strip_test2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //

            Intent intent = new Intent(this, ColorimetryStripDetailActivity.class);
            intent.putExtra(Constant.BRAND, getIntent().getStringExtra(Constant.BRAND));
            NavUtils.navigateUpTo(this, intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /* @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
*/
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

    //
    @Override
    public String getInstruction(int id) throws JSONException {
        return instructions.getJSONObject(id).getString("text");
    }

    @Override
    public Drawable getInstructionDrawable(int id) throws JSONException
    {
        String resName =  instructions.getJSONObject(id).getString("png");

        String path = getResources().getString(R.string.instruction_images);

        path = getPathToDrawable(path, resName);

        try {
            // get input stream
            InputStream ims = getAssets().open(path + "/" + resName.toLowerCase(Locale.US)+".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            return d;
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //change path to file in assets if file is available for locale
    private String getPathToDrawable(String path, String resName)
    {
        String localeLanguage = Locale.getDefault().getLanguage();
        try {

            getAssets().open(path + "-" + localeLanguage + "/" + resName.toLowerCase(Locale.US) + ".png");

            path += "-" + localeLanguage;
        }
        catch(IOException ex) {
            //ignore
        }
        finally {
            return path;
        }

    }
}
