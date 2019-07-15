package org.akvo.caddisfly.sensor.striptest.ui;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

import java.util.Objects;

import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;

public class StripTestActivity extends BaseActivity {

    private static final int REQUEST_QUALITY_TEST = 1;
    private static final int REQUEST_TEST = 2;

    ImageView imagePageRight;
    ImageView imagePageLeft;
    ResultFragment resultFragment;

    private TestInfo testInfo;
    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int resultPageNumber;
    private int totalPageCount;
    private int skipToPageNumber;
    private int currentStage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swatch_select);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

        instructionCount = testInfo.getInstructions().size();

        totalPageCount = instructionCount + 1;
        resultPageNumber = totalPageCount - 1;
        skipToPageNumber = resultPageNumber - 1;

        if (savedInstanceState == null) {
            createFragments();
        }

        for (int i = 0; i < instructionCount; i++) {
            if (testInfo.getInstructions().get(i).testStage > 0) {
                skipToPageNumber = i;
                break;
            }
        }

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(totalPageCount - 1);

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

                showHideFooter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (savedInstanceState == null) {
            Intent intent = new Intent(getBaseContext(), StripMeasureActivity.class);
            intent.putExtra(ConstantKey.TEST_INFO, testInfo);
            intent.putExtra(ConstantKey.TEST_STAGE, currentStage);
            startActivityForResult(intent, REQUEST_QUALITY_TEST);
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

    private void createFragments() {
        int resultId = 1;

        if (resultFragment == null) {
            resultFragment = ResultFragment.newInstance(testInfo, resultId);
            resultFragment.setFragmentId(resultPageNumber);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TEST) {
            if (resultCode == RESULT_OK) {
                if (viewPager.getCurrentItem() != resultPageNumber - 1) {
                    currentStage++;
                    skipToPageNumber = resultPageNumber - 1;
                }
                resultFragment.setDecodeData(StriptestHandler.getDecodeData());
                nextPage();
            }
        } else if (requestCode == REQUEST_QUALITY_TEST) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    private void nextPage() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    private void showHideFooter() {
        showSkipMenu = false;
        imagePageLeft.setVisibility(View.VISIBLE);
        imagePageRight.setVisibility(View.VISIBLE);
        pagerIndicator.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);

        if (viewPager.getCurrentItem() < resultPageNumber - 2) {
            showSkipMenu = true;
        }

        if (viewPager.getCurrentItem() == skipToPageNumber - 1) {
            showSkipMenu = false;
        }

        if (viewPager.getCurrentItem() == resultPageNumber) {
            setTitle(R.string.result);
            viewPager.setAllowedSwipeDirection(SwipeDirection.none);
            footerLayout.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        } else if (viewPager.getCurrentItem() > 0 &&
                testInfo.getInstructions().get(viewPager.getCurrentItem() - 1).testStage > 0) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.right);
            imagePageLeft.setVisibility(View.INVISIBLE);
        } else if (testInfo.getInstructions().get(viewPager.getCurrentItem()).testStage > 0) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
            showSkipMenu = false;
        } else if (viewPager.getCurrentItem() == resultPageNumber - 1) {
            imagePageRight.setVisibility(View.INVISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
        } else {
            footerLayout.setVisibility(View.VISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
            if (viewPager.getCurrentItem() == 0) {
                imagePageLeft.setVisibility(View.INVISIBLE);
            }
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != resultPageNumber) {
            if (viewPager.getCurrentItem() == 0) {
                super.onBackPressed();
            } else {
                pageBack();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showSkipMenu) {
            getMenuInflater().inflate(R.menu.menu_instructions, menu);
        }
        return true;
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

    private void showWaitingView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        showSkipMenu = false;
        invalidateOptionsMenu();
    }

    public void onSendResults(View view) {
        sendResults();
    }

    private void sendResults() {
        SparseArray<String> results = new SparseArray<>();

        results.put(1, String.valueOf(testInfo.getResults().get(0).getResultValue()));
        results.put(2, String.valueOf(testInfo.getResults().get(1).getResultValue()));

        JSONObject resultJsonObj = TestConfigHelper.getJsonResult(this, testInfo,
                results, null, null);

        Intent intent = new Intent();
        intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onStartTest(View view) {
        Intent intent = new Intent(getBaseContext(), StripMeasureActivity.class);
        intent.putExtra(ConstantKey.START_MEASURE, true);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        intent.putExtra(ConstantKey.TEST_STAGE, currentStage);
        startActivityForResult(intent, REQUEST_TEST);
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
        private LinearLayout layout;
        private ViewGroup viewRoot;

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

            viewRoot = container;

            if (getArguments() != null) {
                instruction = getArguments().getParcelable(ARG_SECTION_NUMBER);
                showOk = getArguments().getBoolean(ARG_SHOW_OK);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            View view = fragmentInstructionBinding.getRoot();

            if (showOk) {
                view.findViewById(R.id.buttonStart).setVisibility(View.VISIBLE);
            }

            layout = view.findViewById(R.id.layout_results);

            return view;
        }

        public void setResult(TestInfo testInfo) {
            if (testInfo != null) {

                LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity())
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                layout.removeAllViews();

                SparseArray<String> results = new SparseArray<>();

                results.put(1, String.valueOf(testInfo.getResults().get(0).getResultValue()));
                results.put(2, String.valueOf(testInfo.getResults().get(1).getResultValue()));

                for (Result result : testInfo.getResults()) {
                    String valueString = createValueUnitString(result.getResultValue(), result.getUnit(),
                            getString(R.string.no_result));

                    LinearLayout itemResult;
                    itemResult = (LinearLayout) inflater.inflate(R.layout.item_result,
                            viewRoot, false);
                    TextView textTitle = itemResult.findViewById(R.id.text_title);
                    textTitle.setText(result.getName());

                    TextView textResult = itemResult.findViewById(R.id.text_result);
                    textResult.setText(valueString);
                    layout.addView(itemResult);
                }

                layout.setVisibility(View.VISIBLE);
            }
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
            if (position < testInfo.getInstructions().size() &&
                    testInfo.getInstructions().get(position).testStage > 0) {
                return PlaceholderFragment.newInstance(testInfo.getInstructions().get(position),
                        true);
            } else if (position == totalPageCount - 2) {
                return PlaceholderFragment.newInstance(testInfo.getInstructions().get(position),
                        true);
            } else if (position == totalPageCount - 1) {
                return resultFragment;
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
