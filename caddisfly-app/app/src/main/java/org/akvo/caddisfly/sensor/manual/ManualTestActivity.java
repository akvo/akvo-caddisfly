package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.InstructionHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.PageIndex;
import org.akvo.caddisfly.model.PageType;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.widget.ButtonType;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsHorizontal;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsVertical;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;
import static org.akvo.caddisfly.util.ApiUtil.setKeyboardVisibilityListener;

public class ManualTestActivity extends BaseActivity
        implements MeasurementInputFragment.OnSubmitResultListener,
        ResultPhotoFragment.OnPhotoTakenListener, OnKeyboardVisibilityListener {

    private ImageView imagePageRight;
    private ImageView imagePageLeft;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private PageIndex pageIndex = new PageIndex();

    private TestInfo testInfo;
    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SparseArray<ResultPhotoFragment> resultPhotoFragment = new SparseArray<>();
    private SparseArray<MeasurementInputFragment> inputFragment = new SparseArray<>();
    private int totalPageCount;
    //    private float scale;
    private ArrayList<Instruction> instructionList = new ArrayList<>();
    private PlaceholderFragment submitFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_steps);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        scale = getResources().getDisplayMetrics().density;

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        resultLayout = findViewById(R.id.resultLayout);
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

        InstructionHelper.setupInstructions(testInfo.getInstructions(),
                instructionList, pageIndex, false);

        totalPageCount = instructionList.size();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);

        pagerIndicator.showDots(true);
        pagerIndicator.setPageCount(totalPageCount);

        imagePageRight = findViewById(R.id.image_pageRight);
        imagePageRight.setOnClickListener(view -> nextPage());

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
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (pageIndex.getType(viewPager.getCurrentItem()) == PageType.INPUT
                            && !inputFragment.get(viewPager.getCurrentItem()).isValid(false)) {
                        inputFragment.get(viewPager.getCurrentItem()).showSoftKeyboard();
                    } else {
                        if (inputFragment.get(pageIndex.getInputPageIndex(0)) != null) {
                            inputFragment.get(pageIndex.getInputPageIndex(0)).hideSoftKeyboard();
                        }
                        if (inputFragment.get(pageIndex.getInputPageIndex(1)) != null) {
                            inputFragment.get(pageIndex.getInputPageIndex(1)).hideSoftKeyboard();
                        }
                    }
                }
            }
        });

        submitFragment = PlaceholderFragment.newInstance(
                instructionList.get(totalPageCount - 1), ButtonType.SUBMIT);

        setKeyboardVisibilityListener(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        showHideFooter();
    }

    private void nextPage() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
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
            if (fragment instanceof ResultPhotoFragment) {
                resultPhotoFragment.put(((BaseFragment) fragment).getFragmentId(),
                        (ResultPhotoFragment) fragment);
            }
            if (fragment instanceof MeasurementInputFragment) {
                inputFragment.put(((BaseFragment) fragment).getFragmentId(),
                        (MeasurementInputFragment) fragment);
            }
        }

        super.onRestoreInstanceState(inState);
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

        if (viewPager.getCurrentItem() > pageIndex.getSkipToIndex() + 1) {
            showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex2() - 1;
        } else {
            showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex() - 1;
        }

        switch (pageIndex.getType(viewPager.getCurrentItem())) {
            case PHOTO:
                if (resultPhotoFragment.get(viewPager.getCurrentItem()) != null) {
                    if (resultPhotoFragment.get(viewPager.getCurrentItem()).isValid()) {
                        viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        imagePageRight.setVisibility(View.VISIBLE);
                    } else {
                        viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                        imagePageRight.setVisibility(View.INVISIBLE);
                    }
                }
                break;

            case INPUT:
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                imagePageRight.setVisibility(View.INVISIBLE);
                break;

            case RESULT:
                setTitle(R.string.result);
                imagePageRight.setVisibility(View.INVISIBLE);
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                break;

            case DEFAULT:
                if (viewPager.getCurrentItem() > 0 &&
                        instructionList.get(viewPager.getCurrentItem() - 1).testStage > 0) {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.right);
                    imagePageLeft.setVisibility(View.INVISIBLE);
                } else if (instructionList.get(viewPager.getCurrentItem()).testStage > 0) {
                    viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                    imagePageRight.setVisibility(View.INVISIBLE);
                    showSkipMenu = false;
                } else {
                    footerLayout.setVisibility(View.VISIBLE);
                    viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                }
                break;
        }

        // Last page
        if (viewPager.getCurrentItem() == totalPageCount - 1) {
            imagePageRight.setVisibility(View.INVISIBLE);
            submitFragment.setResult(testInfo);
        }

        // First page
        if (viewPager.getCurrentItem() == 0) {
            imagePageLeft.setVisibility(View.INVISIBLE);
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean visible) {
        if (visible) {
            footerLayout.setVisibility(View.GONE);
        } else {
            footerLayout.setVisibility(View.VISIBLE);
        }
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

    private void sendResults() {

        SparseArray<String> results = new SparseArray<>();
        Intent resultIntent = new Intent();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = "";
        String imageFileName = "";

        if (resultPhotoFragment.get(pageIndex.getPhotoPageIndex(0)) != null) {

            imageFileName = resultPhotoFragment.get(pageIndex.getPhotoPageIndex(0)).getImageFileName();
            resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

            if (resultPhotoFragment.get(pageIndex.getPhotoPageIndex(1)) != null) {
                String result1ImagePath = photoPath.getAbsolutePath() + File.separator +
                        resultPhotoFragment.get(pageIndex.getPhotoPageIndex(1)).getImageFileName();
                Bitmap bitmap1 = BitmapFactory.decodeFile(result1ImagePath);
                Bitmap bitmap2 = BitmapFactory.decodeFile(resultImagePath);
                Bitmap resultBitmap;
                if (bitmap1 != null && bitmap2 != null) {

                    if (Math.abs(bitmap1.getWidth() - bitmap2.getWidth()) > 50) {
                        bitmap2 = BitmapUtils.RotateBitmap(bitmap2, 90);
                    }

                    if (bitmap1.getWidth() > bitmap1.getHeight()) {
                        resultBitmap = concatTwoBitmapsHorizontal(bitmap1, bitmap2);
                    } else {
                        resultBitmap = concatTwoBitmapsVertical(bitmap1, bitmap2);
                    }

                    //noinspection ResultOfMethodCallIgnored
                    new File(result1ImagePath).delete();
                    //noinspection ResultOfMethodCallIgnored
                    new File(resultImagePath).delete();
                    imageFileName = UUID.randomUUID().toString() + ".jpg";
                    resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;
                    ImageUtil.saveImage(resultBitmap, resultImagePath);
                }
            }
        }

        results.put(1, inputFragment.get(pageIndex.getInputPageIndex(0)).getResult());
        if (inputFragment.size() > 1) {
            results.put(2, inputFragment.get(pageIndex.getInputPageIndex(1)).getResult());
        }

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
    public void onSubmitResult(Integer id, String result) {
        if (inputFragment.get(pageIndex.getInputPageIndex(id - 1)).isValid(false)) {
            testInfo.getResults().get(id - 1).setResultValue(Float.parseFloat(result));
            submitFragment.setResult(testInfo);
        }
        nextPage();
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

    public void onSkipClick(MenuItem item) {

        if (viewPager.getCurrentItem() > pageIndex.getSkipToIndex() + 1) {
            viewPager.setCurrentItem(pageIndex.getSkipToIndex2());
        } else {
            viewPager.setCurrentItem(pageIndex.getSkipToIndex());
        }

        if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
            Bundle bundle = new Bundle();
            bundle.putString("InstructionsSkipped", testInfo.getName() +
                    " (" + testInfo.getBrand() + ")");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo.getUuid());
            mFirebaseAnalytics.logEvent("instruction_skipped", bundle);
        }
    }

    @Override
    public void onPhotoTaken(String photoPath) {
        nextPage();
    }

    public void onSubmitClick(View view) {
        sendResults();
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
        private ButtonType showButton;
        private LinearLayout resultLayout;
        private ViewGroup viewRoot;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @return The instance
         */
        static PlaceholderFragment newInstance(Instruction instruction, ButtonType showButton) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, instruction);
            args.putSerializable(ARG_SHOW_OK, showButton);
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
                showButton = (ButtonType) getArguments().getSerializable(ARG_SHOW_OK);
                fragmentInstructionBinding.setInstruction(instruction);
            }

            View view = fragmentInstructionBinding.getRoot();

            if (showButton == ButtonType.SUBMIT) {
                view.findViewById(R.id.buttonSubmit).setVisibility(View.VISIBLE);
            }

            resultLayout = view.findViewById(R.id.layout_results);

            return view;
        }

        void setResult(TestInfo testInfo) {
            if (testInfo != null && testInfo.getResults().size() > 1 && getActivity() != null) {

                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                resultLayout.removeAllViews();

                SparseArray<String> results = new SparseArray<>();

                for (Result result : testInfo.getResults()) {
                    results.put(result.getId(), String.valueOf(result.getResultValue()));

                    String valueString = createValueUnitString(result.getResultValue(), result.getUnit(),
                            getString(R.string.no_result));

                    LinearLayout itemResult;
                    itemResult = (LinearLayout) inflater.inflate(R.layout.item_result,
                            viewRoot, false);
                    TextView textTitle = itemResult.findViewById(R.id.text_title);
                    textTitle.setText(result.getName());

                    TextView textResult = itemResult.findViewById(R.id.text_result);
                    textResult.setText(valueString);
                    resultLayout.addView(itemResult);
                }
                resultLayout.setVisibility(View.VISIBLE);
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
            if (pageIndex.getType(position) == PageType.PHOTO) {
                if (resultPhotoFragment.get(position) == null) {
                    resultPhotoFragment.put(position, ResultPhotoFragment.newInstance(
                            testInfo.getResults().get(resultPhotoFragment.size()).getName(),
                            instructionList.get(position), position));
                }
                return resultPhotoFragment.get(position);
            } else if (pageIndex.getType(position) == PageType.INPUT) {
                if (inputFragment.get(position) == null) {
                    inputFragment.put(position, MeasurementInputFragment.newInstance(
                            testInfo, inputFragment.size(), instructionList.get(position), position));
                }
                return inputFragment.get(position);
            } else if (position == totalPageCount - 1) {
                if (submitFragment == null) {
                    submitFragment = PlaceholderFragment.newInstance(
                            instructionList.get(position), ButtonType.SUBMIT);
                }
                return submitFragment;
            } else {
                return PlaceholderFragment.newInstance(
                        instructionList.get(position), ButtonType.NONE);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }
    }
}
