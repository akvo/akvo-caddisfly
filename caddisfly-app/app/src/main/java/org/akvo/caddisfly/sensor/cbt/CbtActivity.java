package org.akvo.caddisfly.sensor.cbt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.PageIndex;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.manual.ResultPhotoFragment;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.StringUtil;
import org.akvo.caddisfly.widget.ButtonType;
import org.akvo.caddisfly.widget.CustomViewPager;
import org.akvo.caddisfly.widget.PageIndicatorView;
import org.akvo.caddisfly.widget.SwipeDirection;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static org.akvo.caddisfly.sensor.striptest.utils.ResultUtils.createValueUnitString;

public class CbtActivity extends BaseActivity
        implements CompartmentBagFragment.OnCompartmentBagSelectListener,
        ResultPhotoFragment.OnPhotoTakenListener {

    CbtResultFragment resultFragment;
    ResultPhotoFragment resultPhotoFragment;
    CompartmentBagFragment inputFragment;

    ImageView imagePageRight;
    ImageView imagePageLeft;
    PageIndex pageIndex = new PageIndex();
    private String imageFileName = "";
    private String currentPhotoPath;
    private String cbtResult = "00000";
    private TestInfo testInfo;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ArrayList<Instruction> instructionList = new ArrayList<>();
    private int totalPageCount;
    private RelativeLayout footerLayout;
    private PageIndicatorView pagerIndicator;
    private boolean showSkipMenu = true;
    private CustomViewPager viewPager;
    private int testPhase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbt);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
            InstructionHelper.setupInstructions(testInfo.getInstructions2(), instructionList, pageIndex);
        } else {
            InstructionHelper.setupInstructions(testInfo.getInstructions(), instructionList, pageIndex);
        }

        totalPageCount = instructionList.size();

        if (savedInstanceState == null) {
            createFragments();
        }

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
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

        showHideFooter();
    }

    private void createFragments() {
        if (resultFragment == null) {
            resultFragment = CbtResultFragment.newInstance();
            resultFragment.setFragmentId(pageIndex.getResultIndex());
        }
    }

    private void pageBack() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
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

        setTitle(testInfo.getName());

        showSkipMenu = viewPager.getCurrentItem() < pageIndex.getSkipToIndex() - 2;

        if (viewPager.getCurrentItem() == totalPageCount - 1) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
        } else if (viewPager.getCurrentItem() == pageIndex.getInputIndex()) {
            setTitle(R.string.setCompartmentColors);
        } else if (viewPager.getCurrentItem() == pageIndex.getResultIndex()) {
            setTitle(R.string.result);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        } else if (viewPager.getCurrentItem() > 0 &&
                instructionList.get(viewPager.getCurrentItem() - 1).testStage > 0) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.right);
            imagePageLeft.setVisibility(View.INVISIBLE);
        } else if (instructionList.get(viewPager.getCurrentItem()).testStage > 0) {
            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
            imagePageRight.setVisibility(View.INVISIBLE);
            showSkipMenu = false;
        } else if (viewPager.getCurrentItem() == pageIndex.getResultIndex()) {
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ConstantKey.CURRENT_PHOTO_PATH, currentPhotoPath);
        outState.putString(ConstantKey.CURRENT_IMAGE_FILE_NAME, imageFileName);
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo);
        super.onSaveInstanceState(outState);
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

        if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
            Bundle bundle = new Bundle();
            bundle.putString("InstructionsSkipped", testInfo.getName() +
                    " (" + testInfo.getBrand() + ")");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Navigation");
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "si_" + testInfo.getUuid());
            mFirebaseAnalytics.logEvent("instruction_skipped", bundle);
        }
    }

    public void onCompartmentBagSelect(String key) {
        cbtResult = key;
        resultFragment.setResult(cbtResult, testInfo.getSampleQuantity());
    }

    public void onNextClick(View view) {
        nextPage();
    }

    public void onCloseClick(View view) {
        setResult(Activity.RESULT_FIRST_USER, new Intent());
        finish();
    }

    public void onSubmitClick(View view) {
        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(cbtResult, testInfo.getSampleQuantity());

        results.put(1, StringUtil.getStringResourceByName(this,
                mpnValue.getRiskCategory(), "en").toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, String.valueOf(mpnValue.getConfidence()));

        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo,
                results, null, imageFileName);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    @Override
    public void onPhotoTaken(String photoPath) {
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
        private LinearLayout layout;
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
            if (pageIndex.getResultIndex() == position) {
                return resultFragment;
            } else if (pageIndex.getInputIndex() == position) {
                if (inputFragment == null) {
                    inputFragment = CompartmentBagFragment.newInstance(cbtResult);
                    inputFragment.setFragmentId(position);
                }
                return inputFragment;
            } else if (pageIndex.getPhotoIndex() == position) {

                if (resultPhotoFragment == null) {
                    resultPhotoFragment = ResultPhotoFragment.newInstance(
                            "",
                            getString(R.string.take_photo_of_incubated),
                            instructionList.get(position).getIndex());
                    resultPhotoFragment.setFragmentId(position);
                }
                return resultPhotoFragment;
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
    }
}
