package org.akvo.caddisfly.sensor.manual;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.InstructionHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.PageIndex;
import org.akvo.caddisfly.model.PageType;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;

public class SwatchSelectTestActivity extends BaseActivity
        implements SwatchSelectFragment.OnSwatchSelectListener {

    ImageView imagePageRight;
    ImageView imagePageLeft;
    private SparseArray<SwatchSelectFragment> inputFragment = new SparseArray<>();
    PlaceholderFragment submitFragment;
    PageIndex pageIndex = new PageIndex();

    private float[] testResults;
    private TestInfo testInfo;
    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int totalPageCount;
    private float scale;
    private ArrayList<Instruction> instructionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swatch_select);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        scale = getResources().getDisplayMetrics().density;

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
        footerLayout = findViewById(R.id.layout_footer);

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        InstructionHelper.setupInstructions(testInfo.getInstructions(),
                instructionList, pageIndex, false);

        totalPageCount = instructionList.size();

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
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
                showHideFooter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void showHideFooter() {
        if (imagePageLeft == null) {
            return;
        }
        viewPager.setAllowedSwipeDirection(SwipeDirection.all);
        imagePageLeft.setVisibility(View.VISIBLE);
        imagePageRight.setVisibility(View.VISIBLE);
        pagerIndicator.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);

        setTitle(testInfo.getName());

        showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex() - 1;

        switch (pageIndex.getType(viewPager.getCurrentItem())) {
            case INPUT:
                setTitle(R.string.select_color_intervals);
                if (inputFragment.get(pageIndex.getInputPageIndex(0)).isValid()) {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                    imagePageRight.setVisibility(View.VISIBLE);
                } else {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                    imagePageRight.setVisibility(View.INVISIBLE);
                }
                break;

            case RESULT:
                setTitle(R.string.result);
                break;

            case DEFAULT:
                footerLayout.setVisibility(View.VISIBLE);
                viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                break;
        }

        // Last page
        if (viewPager.getCurrentItem() == totalPageCount - 1) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
            imagePageLeft.setVisibility(View.VISIBLE);
            submitFragment.setResult(testInfo);
            if (scale <= 1.5) {
                // don't show footer page indicator for smaller screens
                (new Handler()).postDelayed(() -> footerLayout.setVisibility(View.GONE), 400);
            }
        }

        // First page
        if (viewPager.getCurrentItem() == 0) {
            imagePageLeft.setVisibility(View.INVISIBLE);
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (resultLayout.getVisibility() == View.VISIBLE) {
            viewPager.setCurrentItem(instructionList.size() + 1);
        } else if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pageBack();
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
        viewPager.setCurrentItem(pageIndex.getSkipToIndex());

        if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
            Bundle bundle = new Bundle();
            bundle.putString("InstructionsSkipped", testInfo.getName() +
                    " (" + testInfo.getBrand() + ")");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo.getUuid());
            mFirebaseAnalytics.logEvent("instruction_skipped", bundle);
        }
    }

    public void onSwatchSelect(float[] key) {
        showHideFooter();
        if (inputFragment.get(pageIndex.getInputPageIndex(0)).isValid()) {
            testResults = key;
            testInfo.getResults().get(0).setResultValue(key[0]);
            testInfo.getResults().get(1).setResultValue(key[1]);
            submitFragment.setResult(testInfo);
        }
    }

    public void onSubmitClick(View view) {
        sendResults();
    }

    private void sendResults() {
        if (inputFragment.get(pageIndex.getInputPageIndex(0)).isValid()) {
            SparseArray<String> results = new SparseArray<>();

            results.put(1, String.valueOf(testInfo.getResults().get(0).getResultValue()));
            results.put(2, String.valueOf(testInfo.getResults().get(1).getResultValue()));

            JSONObject resultJsonObj = TestConfigHelper.getJsonResult(this, testInfo,
                    results, null, null);

            Intent intent = new Intent();
            intent.putExtra(SensorConstants.RESPONSE, resultJsonObj.toString());
            setResult(RESULT_OK, intent);
        }
        finish();
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
                view.findViewById(R.id.buttonSubmit).setVisibility(View.VISIBLE);
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
            if (pageIndex.getType(position) == PageType.INPUT) {
                if (inputFragment.get(position) == null) {
                    inputFragment.put(position,
                            SwatchSelectFragment.newInstance(testResults, testInfo.getRanges()));
                }
                return inputFragment.get(position);
            } else if (position == totalPageCount - 1) {
                if (submitFragment == null) {
                    submitFragment = PlaceholderFragment.newInstance(
                            instructionList.get(position), true);
                }
                return submitFragment;
            } else {
                return PlaceholderFragment.newInstance(
                        instructionList.get(position), false);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }
    }
}
