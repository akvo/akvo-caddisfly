package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

public class SwatchSelectTestActivity extends BaseActivity
        implements SwatchSelectFragment.OnSwatchSelectListener {

    ImageView imagePageRight;
    ImageView imagePageLeft;
    SwatchSelectTestResultFragment resultFragment;
    private FragmentManager fragmentManager;
    private float[] testResults;
    private TestInfo testInfo;
    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private int resultPageNumber;
    private int totalPageCount;
    private int skipToPageNumber;
    private int instructionCount;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swatch_select);

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
        pagerLayout = findViewById(R.id.pagerLayout);
        footerLayout = findViewById(R.id.layout_footer);

        fragmentManager = getSupportFragmentManager();

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        resultFragment = SwatchSelectTestResultFragment.newInstance(testInfo);
        if (testInfo.getHasEndInstruction()) {
            instructionCount = testInfo.getInstructions().size() - 1;
        } else {
            instructionCount = testInfo.getInstructions().size();
        }

        totalPageCount = instructionCount + 1;
        resultPageNumber = totalPageCount - 1;
        skipToPageNumber = resultPageNumber;

        if (testInfo.getHasEndInstruction()) {
            totalPageCount += 1;
        }

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(totalPageCount);

        imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view ->
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));

        imagePageLeft = findViewById(R.id.image_pageLeft);
        imagePageLeft.setOnClickListener(view -> pageBack());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pagerIndicator.setActiveIndex(position);

                if (position > resultPageNumber) {
                    if (!resultFragment.isValid(true)) {
                        viewPager.setCurrentItem(resultPageNumber);
                    }
                }

                if (position < 1) {
                    imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    imagePageLeft.setVisibility(View.VISIBLE);
                }

                showHideFooter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

//        startTest();
    }

    private void showHideFooter() {
        imagePageLeft.setVisibility(View.VISIBLE);
        imagePageRight.setVisibility(View.VISIBLE);
        pagerIndicator.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);
        if (viewPager.getCurrentItem() == resultPageNumber) {
            if (resultFragment.isValid(true)) {
                viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                imagePageRight.setVisibility(View.VISIBLE);
            } else {
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                imagePageRight.setVisibility(View.INVISIBLE);
            }
        } else if (viewPager.getCurrentItem() == totalPageCount - 1) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
            imagePageLeft.setVisibility(View.VISIBLE);
            if (scale <= 1.5) {
                // don't show footer page indicator for smaller screens
                (new Handler()).postDelayed(() -> footerLayout.setVisibility(View.GONE), 400);
            }
        } else {
            footerLayout.setVisibility(View.VISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
        }
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(testInfo.getName());
    }

    private void startTest() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                SwatchSelectFragment.newInstance(testResults, testInfo.getRanges()), "swatchSelect")
                .commit();
    }

    public void onSwatchSelect(float[] key) {
        testResults = key;
        testInfo.getResults().get(0).setResultValue(key[0]);
        testInfo.getResults().get(1).setResultValue(key[1]);
    }

    @SuppressWarnings("unused")
    public void onClickMatchedButton(View view) {
        if (testResults == null || testResults[0] == 0 || testResults[1] == 0) {

            Toast toast = Toast.makeText(this, R.string.select_colors_before_continue,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 300);

            View view1 = toast.getView();
            if (view1 != null) {
                view1.setPadding(20, 20, 20, 20);
                view1.setBackgroundResource(R.color.error_background);
            }

            toast.show();
        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,
                    SwatchSelectTestResultFragment.newInstance(testInfo), "resultFragment")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @SuppressWarnings("unused")
    public void onClickSubmitResult(View view) {

        SparseArray<String> results = new SparseArray<>();

        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo, results, null, null);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        private static final String ARG_SHOW_OK = "show_ok";
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;
        private boolean showOk;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction, boolean showOkButton) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            args.putBoolean(ARG_SHOW_OK, showOkButton);
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
                showOk = getArguments().getBoolean(ARG_SHOW_OK);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            View view = fragmentInstructionBinding.getRoot();

            if (showOk) {
                view.findViewById(R.id.buttonDone).setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == resultPageNumber) {
                return resultFragment;
            } else if (position == totalPageCount - 1) {
                return PlaceholderFragment.newInstance(
                        testInfo.getInstructions().get(instructionCount), true);
            } else {
                return PlaceholderFragment.newInstance(
                        testInfo.getInstructions().get(position), false);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }
    }
}
