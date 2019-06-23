package org.akvo.caddisfly.sensor.striptest.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.manual.MeasurementInputFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;

import java.lang.ref.WeakReference;

public class StripTestActivity extends BaseActivity
        implements MeasurementInputFragment.OnSubmitResultListener {

    private static final int REQUEST_TEST = 1;

    SparseArray<String> results = new SparseArray<>();
    ImageView imagePageRight;
    ImageView imagePageLeft;
    SectionsPagerAdapter mSectionsPagerAdapter;

    private WeakReference<StripMeasureActivity> mActivity;
    private TestInfo testInfo;

    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ResultFragment resultFragment;
    private int resultPageNumber;
    private int totalPageCount;
    private int skipToPageNumber;
    private float scale;
    private int startTest1PageNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        scale = getResources().getDisplayMetrics().density;

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
        pagerLayout = findViewById(R.id.pagerLayout);
        footerLayout = findViewById(R.id.layout_footer);

        if (savedInstanceState != null) {
            testInfo = savedInstanceState.getParcelable(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        int instructionCount;
        if (testInfo.getHasEndInstruction()) {
            instructionCount = testInfo.getInstructions().size();
        } else {
            instructionCount = testInfo.getInstructions().size() + 1;
        }

        totalPageCount = instructionCount;
        resultPageNumber = totalPageCount - 1;
        skipToPageNumber = resultPageNumber - 1;

        for (int i = 0; i < testInfo.getInstructions().size(); i++) {
            for (String text :
                    testInfo.getInstructions().get(i).section) {
                if (text.contains("<start>")) {
                    startTest1PageNumber = i;
                    totalPageCount = startTest1PageNumber + 2;
                    resultPageNumber = totalPageCount - 1;
                    skipToPageNumber = resultPageNumber - 1;
                    break;
                }
            }
        }

        if (testInfo.getHasEndInstruction()) {
            totalPageCount += 1;
        }

        if (savedInstanceState == null) {
            createFragments();
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(totalPageCount);

        imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view ->
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));

        imagePageLeft = findViewById(R.id.image_pageLeft);
        imagePageLeft.setVisibility(View.INVISIBLE);
        imagePageLeft.setOnClickListener(view -> pageBack());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pagerIndicator.setActiveIndex(position);

                if (position < 1) {
                    imagePageLeft.setVisibility(View.INVISIBLE);
                } else {
                    imagePageLeft.setVisibility(View.VISIBLE);
                }

                if (position == resultPageNumber) {
                    showWaitingView();
                }

                showHideFooter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
    }

    private void createFragments() {
        if (resultFragment == null) {
            resultFragment = ResultFragment.newInstance(testInfo);
            resultFragment.setFragmentId(resultPageNumber);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        for (int i = 0; i < getSupportFragmentManager().getFragments().size(); i++) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(i);
            if (fragment instanceof BaseFragment) {
                if (((BaseFragment) fragment).getFragmentId() == resultPageNumber) {
                    resultFragment = (ResultFragment) fragment;
                }
            }
        }

        createFragments();

        super.onRestoreInstanceState(inState);
    }

    private void showHideFooter() {
        showSkipMenu = false;
        imagePageLeft.setVisibility(View.VISIBLE);
        imagePageRight.setVisibility(View.VISIBLE);
        pagerIndicator.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);
        if (viewPager.getCurrentItem() == resultPageNumber - 1) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
        } else if (viewPager.getCurrentItem() == resultPageNumber) {
            footerLayout.setVisibility(View.GONE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.none);
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
            if (viewPager.getCurrentItem() < resultPageNumber - 1) {
                showSkipMenu = true;
            }
            if (viewPager.getCurrentItem() == 0) {
                imagePageLeft.setVisibility(View.INVISIBLE);
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (testInfo != null && testInfo.getUuid() != null) {
            setTitle(testInfo.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showSkipMenu) {
            getMenuInflater().inflate(R.menu.menu_instructions, menu);
        }
        return true;
    }

    private void submitResult() {
        sendResults();
    }

    private void sendResults() {

        Intent resultIntent = new Intent();

//        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);
//
//        results.put(1, String.valueOf(testInfo.getResults().get(0).getResultValue()));
//        results.put(2, String.valueOf(testInfo.getResults().get(1).getResultValue()));
//
//        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo,
//                results, null, imageFileName);
//        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
//        if (!imageFileName.isEmpty()) {
//            resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);
//        }

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onSubmitResult(String result) {
        submitResult();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (viewPager.getCurrentItem() == 0) {
                onBackPressed();
            } else {
                viewPager.setCurrentItem(0);
                showHideFooter();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (resultLayout.getVisibility() == View.VISIBLE) {
            viewPager.setCurrentItem(testInfo.getInstructions().size() + 1);
            showWaitingView();
        } else if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pageBack();
        }
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    private void showWaitingView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        showSkipMenu = false;
        invalidateOptionsMenu();
    }

    public void onSkipClick(MenuItem item) {
        viewPager.setCurrentItem(skipToPageNumber);
        showWaitingView();

        if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
            Bundle bundle = new Bundle();
            bundle.putString("InstructionsSkipped", testInfo.getName() +
                    " (" + testInfo.getBrand() + ")");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo.getUuid());
            mFirebaseAnalytics.logEvent("instruction_skipped", bundle);
        }
    }

    public void onSendResults(View view) {
        sendResults();
    }

    @Override
    public void onPause() {
        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }

        super.onPause();
    }

    public void onStartTest(View view) {
        Intent intent;
        intent = new Intent(this, StripMeasureActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEST && resultCode == Activity.RESULT_OK) {
            resultFragment.setDecodeData(StriptestHandler.getDecodeData());
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            setTitle(R.string.result);
        }
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
        private static final String ARG_SHOW_START = "show_start";
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;
        private boolean showOk;
        private boolean showStart;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction,
                                               boolean showOkButton, boolean showStartButton) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            args.putBoolean(ARG_SHOW_OK, showOkButton);
            args.putBoolean(ARG_SHOW_START, showStartButton);
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
                showStart = getArguments().getBoolean(ARG_SHOW_START);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            View view = fragmentInstructionBinding.getRoot();

            if (showOk) {
                view.findViewById(R.id.buttonDone).setVisibility(View.VISIBLE);
            }
            if (showStart) {
                view.findViewById(R.id.buttonStart).setVisibility(View.VISIBLE);
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
            } else if (position == resultPageNumber - 1 || position == startTest1PageNumber) {
                return PlaceholderFragment.newInstance(
                        testInfo.getInstructions().get(position), false, true);
            } else {
                return PlaceholderFragment.newInstance(
                        testInfo.getInstructions().get(position), false, false);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }
    }
}
