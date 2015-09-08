package org.akvo.akvoqr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.akvoqr.instructions_app.InstructionListActivity;
import org.akvo.akvoqr.opencv.StripTest;
import org.akvo.akvoqr.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChooseStripTestActivity extends AppCompatActivity implements ActionBar.TabListener {


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    StripTest stripTest;

    List<ChooseStripTestFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_strip_test);

        stripTest = StripTest.getInstance();
        Set<String> allBrands = stripTest.getAllBrands();
        for(String brandString: allBrands)
        {
            StripTest.Brand brand = stripTest.getBrand(brandString);
            String brandName = brand.getName();
            System.out.println("***brandname: " + brandName);

            fragments.add(ChooseStripTestFragment.newInstance(brandString));
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_strip_test2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            ChooseStripTestFragment fragment = fragments.get(position);
            String brandName = fragment.getArguments().getString(Constant.BRAND);

            StripTest.Brand brand = StripTest.getInstance().getBrand(brandName);
            String title = brand.getName();
            if(title!=null) {
                return title.toUpperCase(l);
            }

            return null;

        }
    }

    /**
     * This class assumes that there are .png images in res/drawable that have the same name
     * as the String 'brand' in the JsonObject 'strip' in strips.json from assets
     */
    public static class ChooseStripTestFragment extends Fragment {

        private String brandName;
        private LinearLayout linearLayout;
        private ImageView imageView;
        private Button buttonInstruction;
        private boolean instructionsVisible = false;

        public static ChooseStripTestFragment newInstance(String brandName) {
            ChooseStripTestFragment fragment = new ChooseStripTestFragment();
            Bundle args = new Bundle();
            args.putString(Constant.BRAND, brandName);
            fragment.setArguments(args);
            return fragment;
        }

        public ChooseStripTestFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_choose_strip_test, container, false);
            imageView = (ImageView) rootView.findViewById(R.id.fragment_choose_strip_testImageView);
            linearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_choose_strip_testTableLayout);

            if(getArguments()!=null) {

                this.brandName = getArguments().getString(Constant.BRAND);

                int resId = getResources().getIdentifier(brandName.toLowerCase(Locale.US), "drawable", this.getActivity().getPackageName());
                imageView.setImageResource(resId);

                Button button = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButton);
                button.setOnClickListener(new ChooseBrandOnClickListener(brandName));
                buttonInstruction = (Button) rootView.findViewById(R.id.fragment_choose_strip_testButtonInstruction);
                buttonInstruction.setOnClickListener(new ShowInstructionsOnClickListener(brandName));

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
                Intent intent = new Intent(getActivity(), InstructionListActivity.class);
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

                if (!instructionsVisible) {

                    linearLayout.removeAllViews();
                    StripTest.Brand brand = StripTest.getInstance().getBrand(brandName);
                    try {
                        JSONArray instructions = brand.getInstructions();

                        for(int i=0;i<instructions.length();i++) {
                            TextView textView = new TextView(getActivity());

                            textView.setText(i + ". " + instructions.getString(i));

                            textView.setTextColor(Color.BLACK);

                            linearLayout.addView(textView);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                imageView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                instructionsVisible = true;
                buttonInstruction.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_black_up, 0);
            }
            else {
                linearLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                instructionsVisible = false;
                buttonInstruction.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.arrow_black_down,0);
            }
        }
    }
}

}
