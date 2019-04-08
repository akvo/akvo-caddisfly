package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.json.JSONObject;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ManualTestActivity extends BaseActivity
        implements MeasurementInputFragment.OnSubmitResultListener,
        ResultPhotoFragment.OnPhotoTakenListener {

    private TestInfo testInfo;
    private String imageFileName = "";

    private MeasurementInputFragment waitingFragment;

    private ViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String result;
    private ResultPhotoFragment resultPhotoFragment;

    private int resultPageNumber;
    private int photoPageNumber;
    private int totalPageCount;
    private int skipToPageNumber;
    private int instructionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
        pagerLayout = findViewById(R.id.pagerLayout);
        footerLayout = findViewById(R.id.layout_footer);

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        waitingFragment = MeasurementInputFragment.newInstance(testInfo);
        resultPhotoFragment = ResultPhotoFragment.newInstance();

        if (testInfo.getHasEndInstruction()) {
            instructionCount = testInfo.getInstructions().size() - 1;
        } else {
            instructionCount = testInfo.getInstructions().size();
        }

        if (testInfo.getHasImage()) {
            totalPageCount = instructionCount + 2;
            photoPageNumber = totalPageCount - 2;
            resultPageNumber = totalPageCount - 1;
            skipToPageNumber = photoPageNumber;
        } else {
            totalPageCount = instructionCount + 1;
            resultPageNumber = totalPageCount - 1;
            skipToPageNumber = resultPageNumber;
            photoPageNumber = -1;
        }

        if (testInfo.getHasEndInstruction()) {
            totalPageCount += 1;
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(totalPageCount);

        ImageView imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view ->
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));

        ImageView imagePageLeft = findViewById(R.id.image_pageLeft);
        imagePageLeft.setOnClickListener(view -> pageBack());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position >= totalPageCount - 1) {
                    if (!waitingFragment.isValid()) {
                        viewPager.setCurrentItem(resultPageNumber);
                    }
                } else if (position >= photoPageNumber) {
                    if (!resultPhotoFragment.isValid()) {
                        viewPager.setCurrentItem(photoPageNumber);
                    }
                }
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
                } else {
                    showInstructionsView();
                    onInstructionFinish(testInfo.getInstructions().size() - position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (viewPager.getCurrentItem() == resultPageNumber) {
                        waitingFragment.showSoftKeyboard();
                    } else {
                        waitingFragment.hideSoftKeyboard();
                    }
                }
            }
        });

        // startManualTest();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(testInfo.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showSkipMenu) {
            getMenuInflater().inflate(R.menu.menu_instructions, menu);
        }
        return true;
    }

    private void submitResult() {

        waitingFragment.hideSoftKeyboard();

        if (testInfo.getHasEndInstruction()) {
            if (waitingFragment.isValid()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        } else {
            sendResults();
        }
    }

    private void sendResults() {
        Intent resultIntent = new Intent();

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        results.put(1, result);

        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo,
                results, null, imageFileName);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        if (!imageFileName.isEmpty()) {
            resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);
        }

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onSubmitResult(String result) {
        this.result = result;

        if (testInfo.getHasImage() && imageFileName.isEmpty()) {
            viewPager.setCurrentItem(photoPageNumber);
            Toast.makeText(this, R.string.take_photo_meter_result,
                    Toast.LENGTH_LONG).show();
        } else {
            submitResult();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (viewPager.getCurrentItem() == 0) {
                onBackPressed();
            } else {
                showSelectTestView();
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

    private void showInstructionsView() {
        if (viewPager.getCurrentItem() < resultPageNumber) {
            footerLayout.setVisibility(View.VISIBLE);
        }
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        setTitle(testInfo.getName());
        showSkipMenu = true;
        invalidateOptionsMenu();
    }

    private void showSelectTestView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        footerLayout.setVisibility(View.GONE);
        viewPager.setCurrentItem(0);
        setTitle(testInfo.getName());
        showSkipMenu = true;
        invalidateOptionsMenu();
    }

    private void showWaitingView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        if (viewPager.getCurrentItem() == resultPageNumber) {
            footerLayout.setVisibility(View.GONE);
        }
        showSkipMenu = false;
        invalidateOptionsMenu();
    }

    public void onInstructionFinish(int page) {
        if (page > 1) {
            showSkipMenu = true;
            invalidateOptionsMenu();
        } else {
            showSkipMenu = false;
            invalidateOptionsMenu();
        }
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

    public void onSelectTestClick(View view) {
        viewPager.setCurrentItem(1);
        showInstructionsView();
    }

    @Override
    public void onPhotoTaken(String photoPath) {
        imageFileName = photoPath;
    }

    public void onSendResults(View view) {
        sendResults();
    }

//    public void onNextClick(View view) {
//        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
//    }

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
            if (position == photoPageNumber) {
                return resultPhotoFragment;
            } else if (position == resultPageNumber) {
                return waitingFragment;
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
