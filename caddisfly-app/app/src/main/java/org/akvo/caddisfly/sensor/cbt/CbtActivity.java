package org.akvo.caddisfly.sensor.cbt;

import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsHorizontal;
import static org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils.concatTwoBitmapsVertical;
import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.databinding.FragmentInstructionBinding;
import org.akvo.caddisfly.helper.InstructionHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.PageIndex;
import org.akvo.caddisfly.model.PageType;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.manual.ResultPhotoFragment;
import org.akvo.caddisfly.sensor.striptest.utils.BitmapUtils;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.StringUtil;
import org.akvo.caddisfly.widget.ButtonType;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class CbtActivity extends BaseActivity
        implements CompartmentBagFragment.OnCompartmentBagSelectListener,
        ResultPhotoFragment.OnPhotoTakenListener {

    private CbtResultFragment resultFragment;

    private final SparseArray<ResultPhotoFragment> resultPhotoFragment = new SparseArray<>();
    private final SparseArray<CompartmentBagFragment> inputFragment = new SparseArray<>();

    private final ArrayList<Integer> inputIndexes = new ArrayList<>();
    private final SparseArray<String> cbtResultKeys = new SparseArray<>();

    private ImageView imagePageRight;
    private ImageView imagePageLeft;
    private final PageIndex pageIndex = new PageIndex();
    private String imageFileName = "";
    private String currentPhotoPath;
    private TestInfo testInfo;
    private FirebaseAnalytics mFirebaseAnalytics;
    private final ArrayList<Instruction> instructionList = new ArrayList<>();
    private int totalPageCount;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private CustomViewPager viewPager;
    private int testPhase;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_steps);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        scale = getResources().getDisplayMetrics().density;

        viewPager = findViewById(R.id.viewPager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        footerLayout = findViewById(R.id.layout_footer);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(ConstantKey.CURRENT_PHOTO_PATH);
            imageFileName = savedInstanceState.getString(ConstantKey.CURRENT_IMAGE_FILE_NAME);
            testInfo = savedInstanceState.getParcelable(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        testPhase = getIntent().getIntExtra(ConstantKey.TEST_PHASE, 0);
        if (testPhase == 2) {
            InstructionHelper.setupInstructions(testInfo.getInstructions2(), instructionList, pageIndex, false);
        } else {
            InstructionHelper.setupInstructions(testInfo.getInstructions(), instructionList, pageIndex, false);
        }

        totalPageCount = instructionList.size();

        if (savedInstanceState == null) {
            createFragments();
        }

        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

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

                if (resultPhotoFragment.get(position - 1) != null &&
                        !resultPhotoFragment.get(position - 1).isValid()) {
                    pageBack();
                    return;
                }

                pagerIndicator.setActiveIndex(position);
                showHideFooter();
                if (pageIndex.getType(position) == PageType.PHOTO && position > 2) {
                    if (cbtResultKeys.size() > 0) {
                        if (cbtResultKeys.get(inputIndexes.get(0)).equals("11111")) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 3, true);
                        }
                    }
                }
                if (pageIndex.getType(position) == PageType.INPUT && position > 3) {
                    if (cbtResultKeys.size() > 0) {
                        if (cbtResultKeys.get(inputIndexes.get(0)).equals("11111")) {
                            viewPager.setCurrentItem(pageIndex.getInputPageIndex(0), true);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        showHideFooter();
    }

    private void createFragments() {
        if (resultFragment == null) {
            resultFragment = CbtResultFragment.newInstance(testInfo.getResults().size());
            resultFragment.setFragmentId(pageIndex.getResultIndex());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ConstantKey.CURRENT_PHOTO_PATH, currentPhotoPath);
        outState.putString(ConstantKey.CURRENT_IMAGE_FILE_NAME, imageFileName);
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(@NotNull Bundle inState) {
        for (int i = 0; i < getSupportFragmentManager().getFragments().size(); i++) {
            Fragment fragment = getSupportFragmentManager().getFragments().get(i);
            if (fragment instanceof ResultPhotoFragment) {
                resultPhotoFragment.put(((BaseFragment) fragment).getFragmentId(),
                        (ResultPhotoFragment) fragment);
            }
            if (fragment instanceof CompartmentBagFragment) {
                inputFragment.put(((BaseFragment) fragment).getFragmentId(),
                        (CompartmentBagFragment) fragment);
            }
        }

        createFragments();

        super.onRestoreInstanceState(inState);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        showHideFooter();
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    private void nextPage() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
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

        showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex() - 2;

        if (viewPager.getCurrentItem() > pageIndex.getSkipToIndex()) {
            showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex2() - 2;
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
                setTitle(R.string.setCompartmentColors);
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
            imagePageRight.setVisibility(View.INVISIBLE);
            if (testPhase == 2) {
                if (scale <= 1.5) {
                    // don't show footer page indicator for smaller screens
                    (new Handler()).postDelayed(() -> footerLayout.setVisibility(View.GONE), 400);
                }
            }
        }

        // First page
        if (viewPager.getCurrentItem() == 0) {
            imagePageLeft.setVisibility(View.INVISIBLE);
        }

        invalidateOptionsMenu();
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

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            pageBack();
        }
    }

    /**
     * Show CBT incubation times instructions in a dialog.
     *
     * @param view the view
     */
    public void onClickIncubationTimes(@SuppressWarnings("unused") View view) {
        DialogFragment newFragment = new CbtActivity.IncubationTimesDialogFragment();
        newFragment.show(getSupportFragmentManager(), "incubationTimes");
    }

    public void onSkipClick(MenuItem item) {
        viewPager.setCurrentItem(pageIndex.getSkipToIndex());

        if (AppPreferences.analyticsEnabled()) {
            Bundle bundle = new Bundle();
            bundle.putString("InstructionsSkipped", testInfo.getName() +
                    " (" + testInfo.getBrand() + ")");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo.getUuid());
            mFirebaseAnalytics.logEvent("instruction_skipped", bundle);
        }
    }

    public void onCompartmentBagSelect(String key, int fragmentId) {

        int secondFragmentId = 0;

        // Get the id of the TC input fragment
        if (inputIndexes.size() > 1) {
            secondFragmentId = inputIndexes.get(1);
        }

        cbtResultKeys.put(fragmentId, key);

        for (int i = 0; i < inputIndexes.size(); i++) {
            inputFragment.get(inputIndexes.get(0));
        }

        String secondResult;
        String newSecondResult = key.replace("1", "2");
        CompartmentBagFragment secondFragment = null;

        if (fragmentId == inputIndexes.get(0)) {

            resultFragment.setResult(cbtResultKeys.get(fragmentId), testInfo.getSampleQuantity());

            if (inputIndexes.size() > 1) {
                secondFragment = inputFragment.get(secondFragmentId);
                secondResult = secondFragment.getKey();
            } else {
                secondResult = newSecondResult;
            }

            for (int i = 0; i < secondResult.length(); i++) {
                if (secondResult.charAt(i) == '1' && newSecondResult.charAt(i) != '2') {
                    char[] chars = newSecondResult.toCharArray();
                    chars[i] = '1';
                    newSecondResult = String.valueOf(chars);
                }
            }
            if (secondFragment != null) {
                secondFragment.setKey(newSecondResult);
            }
        }

        resultFragment.setResult2(newSecondResult, testInfo.getSampleQuantity());

        // If E.coli input fragment then add or remove TC part based on input
        if (fragmentId != secondFragmentId) {
            InstructionHelper.setupInstructions(testInfo.getInstructions2(), instructionList, pageIndex, key.equals("11111"));
            totalPageCount = instructionList.size();
            pagerIndicator.setPageCount(totalPageCount);
            Objects.requireNonNull(viewPager.getAdapter()).notifyDataSetChanged();
            pagerIndicator.setVisibility(View.GONE);
            pagerIndicator.invalidate();
            pagerIndicator.setVisibility(View.VISIBLE);
        }
    }

    public void onNextClick(View view) {
        nextPage();
    }

    public void onCloseClick(View view) {
        setResult(Activity.RESULT_FIRST_USER, new Intent());
        finish();
    }

    public void onSubmitClick(View view) {
        sendResults();
    }

    private void sendResults() {

        SparseArray<String> results = new SparseArray<>();
        Intent resultIntent = new Intent();

        Bitmap resultBitmap = null;

        if (resultPhotoFragment.get(pageIndex.getPhotoPageIndex(0)) != null) {
            imageFileName = UUID.randomUUID().toString() + ".jpg";
            String resultImagePath = resultPhotoFragment.get(pageIndex.getPhotoPageIndex(0)).getImageFileName();
            Bitmap bitmap1 = BitmapFactory.decodeFile(resultImagePath);
            if (resultPhotoFragment.get(pageIndex.getPhotoPageIndex(1)) != null) {
                String result1ImagePath = resultPhotoFragment.get(pageIndex.getPhotoPageIndex(1)).getImageFileName();
                Bitmap bitmap2 = BitmapFactory.decodeFile(result1ImagePath);
                if (bitmap1 != null && bitmap2 != null) {

                    if (Math.abs(bitmap1.getWidth() - bitmap2.getWidth()) > 50) {
                        bitmap2 = BitmapUtils.RotateBitmap(bitmap2, 90);
                    }

                    if (bitmap1.getWidth() > bitmap1.getHeight()) {
                        resultBitmap = concatTwoBitmapsHorizontal(bitmap1, bitmap2);
                    } else {
                        resultBitmap = concatTwoBitmapsVertical(bitmap1, bitmap2);
                    }

                    bitmap1.recycle();
                    bitmap2.recycle();

                    //noinspection ResultOfMethodCallIgnored
                    new File(result1ImagePath).delete();
                    //noinspection ResultOfMethodCallIgnored
                    new File(resultImagePath).delete();
                }
            } else {
                resultBitmap = bitmap1;
            }
        }

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(
                resultFragment.getResult(), testInfo.getSampleQuantity());
        MpnValue mpnTcValue = TestConfigHelper.getMpnValueForKey(
                resultFragment.getResult2(), testInfo.getSampleQuantity());

        results.put(1, StringUtil.getStringResourceByName(this,
                mpnValue.getRiskCategory(), "en").toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, String.valueOf(mpnValue.getConfidence()));

        results.put(4, mpnTcValue.getMpn());
        results.put(5, String.valueOf(mpnTcValue.getConfidence()));

        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo,
                results, null, imageFileName);

        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        if (!imageFileName.isEmpty() && resultBitmap != null) {
            resultIntent.putExtra(SensorConstants.IMAGE, imageFileName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            resultBitmap.recycle();
            resultIntent.putExtra(SensorConstants.IMAGE_BITMAP, stream.toByteArray());
        }

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onPhotoTaken() {
        nextPage();
    }

    public static class IncubationTimesDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_incubation_times, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
            return builder.create();
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
        FragmentInstructionBinding fragmentInstructionBinding;
        Instruction instruction;
        private ButtonType showButton;
        private LinearLayout resultLayout;
        private ViewGroup viewRoot;

        /**
         * Returns a new instance of this fragment for the given section number.
         *
         * @param instruction The information to to display
         * @param showButton  The button to be shown
         * @return The instance
         */
        @SuppressWarnings("SameParameterValue")
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

            switch (showButton) {
                case START:
                    view.findViewById(R.id.buttonStart).setVisibility(View.VISIBLE);
                    break;
                case CLOSE:
                    view.findViewById(R.id.buttonClose).setVisibility(View.VISIBLE);
                    break;
                case SUBMIT:
                    view.findViewById(R.id.buttonSubmit).setVisibility(View.VISIBLE);
                    break;
            }

            resultLayout = view.findViewById(R.id.layout_results);

            return view;
        }

        public void setResult(TestInfo testInfo) {
            if (testInfo != null) {

                LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity())
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                resultLayout.removeAllViews();

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
            if (pageIndex.getResultIndex() == position) {
                return resultFragment;
            } else if (pageIndex.getType(position) == PageType.INPUT) {
                if (inputFragment.get(position) == null) {
                    String key = "00000";
                    boolean useBlue = false;
                    if (inputIndexes.size() > 0) {
                        int firstFragmentId = inputIndexes.get(0);
                        if (cbtResultKeys.get(firstFragmentId) == null) {
                            cbtResultKeys.put(firstFragmentId, key);
                        }
                        key = cbtResultKeys.get(firstFragmentId).replace("1", "2");
                        resultFragment.setResult2(key, testInfo.getSampleQuantity());
                    }
                    if (inputFragment.size() > 0) {
                        useBlue = true;
                    }
                    inputFragment.put(position, CompartmentBagFragment.newInstance(key, position,
                            instructionList.get(position), useBlue));
                    inputIndexes.add(position);
                }
                return inputFragment.get(position);
            } else if (pageIndex.getType(position) == PageType.PHOTO) {
                if (resultPhotoFragment.get(position) == null) {
                    resultPhotoFragment.put(position, ResultPhotoFragment.newInstance(
                            "", instructionList.get(position), position));
                }
                return resultPhotoFragment.get(position);
            } else if (position == totalPageCount - 1) {
                if (testPhase == 2) {
                    return PlaceholderFragment.newInstance(
                            instructionList.get(position), ButtonType.SUBMIT);
                }
                return PlaceholderFragment.newInstance(
                        instructionList.get(position), ButtonType.CLOSE);
            } else {
                return PlaceholderFragment.newInstance(
                        instructionList.get(position), ButtonType.NONE);
            }
        }

        @Override
        public int getCount() {
            return totalPageCount;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
