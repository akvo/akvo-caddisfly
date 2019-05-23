package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsHorizontal;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsVertical;

public class ManualTestActivity extends BaseActivity
        implements MeasurementInputFragment.OnSubmitResultListener,
        ResultPhotoFragment.OnPhotoTakenListener, OnKeyboardVisibilityListener {

    SparseArray<String> results = new SparseArray<>();
    ImageView imagePageRight;
    ImageView imagePageLeft;
    private TestInfo testInfo;
    private CustomViewPager viewPager;
    private FrameLayout resultLayout;
    private FrameLayout pagerLayout;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ResultPhotoFragment resultPhotoFragment;
    private ResultPhotoFragment result1PhotoFragment;
    private MeasurementInputFragment result1Fragment;
    private MeasurementInputFragment resultFragment;
    private int photo1PageNumber = -1;
    private int result1PageNumber = -1;
    private int photoPageNumber;
    private int resultPageNumber;
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

        int resultId = 1;

        for (int i = 0; i < testInfo.getInstructions().size(); i++) {
            if (testInfo.getInstructions().get(i).section.get(0).contains("<photo1>")) {
                photo1PageNumber = i;
                result1PageNumber = i + 1;
                result1Fragment = MeasurementInputFragment.newInstance(testInfo, resultId);
                result1PhotoFragment = ResultPhotoFragment.newInstance(
                        testInfo.getResults().get(resultId - 1).getName());
                resultId++;
            }
        }

        resultFragment = MeasurementInputFragment.newInstance(testInfo, resultId);
        resultPhotoFragment = ResultPhotoFragment.newInstance(
                testInfo.getResults().get(resultId - 1).getName());

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
                } else if (photoPageNumber != -1 && position > photoPageNumber) {
                    if (!resultPhotoFragment.isValid()) {
                        viewPager.setCurrentItem(photoPageNumber);
                    }
                } else if (result1PageNumber != -1 && position > result1PageNumber) {
                    if (!result1Fragment.isValid(true)) {
                        viewPager.setCurrentItem(result1PageNumber);
                    }
                } else if (photo1PageNumber != -1 && position > photo1PageNumber) {
                    if (!result1PhotoFragment.isValid()) {
                        viewPager.setCurrentItem(photo1PageNumber);
                    }
                }

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

                showHideFooter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (viewPager.getCurrentItem() == resultPageNumber &&
                            !resultFragment.isValid(false)) {
                        resultFragment.showSoftKeyboard();
                    } else if (viewPager.getCurrentItem() == result1PageNumber &&
                            !result1Fragment.isValid(false)) {
                        if (result1Fragment != null) {
                            result1Fragment.showSoftKeyboard();
                        }
                    } else {
                        resultFragment.hideSoftKeyboard();
                        if (result1Fragment != null) {
                            result1Fragment.hideSoftKeyboard();
                        }
                    }
                }
            }
        });

        setKeyboardVisibilityListener(this);
    }

    private void showHideFooter() {
        imagePageLeft.setVisibility(View.VISIBLE);
        imagePageRight.setVisibility(View.VISIBLE);
        pagerIndicator.setVisibility(View.VISIBLE);
        footerLayout.setVisibility(View.VISIBLE);

        if (viewPager.getCurrentItem() == photo1PageNumber) {
            if (result1PhotoFragment.isValid()) {
                viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                imagePageRight.setVisibility(View.VISIBLE);
            } else {
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                imagePageRight.setVisibility(View.INVISIBLE);
            }
        } else if (viewPager.getCurrentItem() == result1PageNumber) {
            if (result1Fragment.isValid(true)) {
                viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                imagePageRight.setVisibility(View.VISIBLE);
            } else {
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                imagePageRight.setVisibility(View.INVISIBLE);
            }
        } else if (viewPager.getCurrentItem() == photoPageNumber) {
            if (resultPhotoFragment.isValid()) {
                viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                imagePageRight.setVisibility(View.VISIBLE);
            } else {
                viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                imagePageRight.setVisibility(View.INVISIBLE);
            }
        } else if (viewPager.getCurrentItem() == resultPageNumber) {
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
        } else {
            footerLayout.setVisibility(View.VISIBLE);
            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            footerLayout.setVisibility(View.GONE);
        } else {
            footerLayout.setVisibility(View.VISIBLE);
        }
    }

    //https://stackoverflow.com/questions/4312319/how-to-capture-the-virtual-keyboard-show-hide-event-in-android
    private void setKeyboardVisibilityListener(final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();
            private boolean alreadyOpen;

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    return;
                }
                alreadyOpen = isShown;
                onKeyboardVisibilityListener.onVisibilityChanged(isShown);
            }
        });
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

        if (result1PageNumber == viewPager.getCurrentItem() && result1Fragment.isValid(true)) {
            result1Fragment.hideSoftKeyboard();
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        } else if (testInfo.getHasEndInstruction()) {
            if (resultPageNumber == viewPager.getCurrentItem() && resultFragment.isValid(true)) {
                resultFragment.hideSoftKeyboard();
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        } else {
            sendResults();
        }
    }

    private void sendResults() {

        if (photo1PageNumber != -1 && !result1PhotoFragment.isValid()) {
            viewPager.setCurrentItem(photo1PageNumber);
            return;
        } else if (result1PageNumber != -1 && !result1Fragment.isValid(true)) {
            viewPager.setCurrentItem(result1PageNumber);
            return;
        } else if (photoPageNumber != -1 && !resultPhotoFragment.isValid()) {
            viewPager.setCurrentItem(photoPageNumber);
            return;
        } else if (!resultFragment.isValid(true)) {
            viewPager.setCurrentItem(resultPageNumber);
            return;
        }

        Intent resultIntent = new Intent();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator +
                resultPhotoFragment.getImageFileName();

        String imageFileName = resultPhotoFragment.getImageFileName();

        if (result1PhotoFragment != null) {
            String result1ImagePath = photoPath.getAbsolutePath() + File.separator +
                    result1PhotoFragment.getImageFileName();
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

        if (result1PageNumber != -1) {
            results.put(1, result1Fragment.getResult());
            results.put(2, resultFragment.getResult());
        } else {
            results.put(1, resultFragment.getResult());
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
    public void onSubmitResult(String result) {
        submitResult();
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
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        setTitle(testInfo.getName());
        showSkipMenu = true;
        invalidateOptionsMenu();
    }

    private void showSelectTestView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        viewPager.setCurrentItem(0);
        setTitle(testInfo.getName());
        showSkipMenu = true;
        invalidateOptionsMenu();
    }

    private void showWaitingView() {
        pagerLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
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
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
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
            if (position == photo1PageNumber) {
                return result1PhotoFragment;
            } else if (position == result1PageNumber) {
                return result1Fragment;
            } else if (position == photoPageNumber) {
                return resultPhotoFragment;
            } else if (position == resultPageNumber) {
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
