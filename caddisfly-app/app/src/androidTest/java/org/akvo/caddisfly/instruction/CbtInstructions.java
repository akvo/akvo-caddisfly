/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.instruction;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.ui.TestActivity;
import org.akvo.caddisfly.util.TestHelper;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CbtInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();
    private final StringBuilder listString = new StringBuilder();

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    // third parameter is set to false which means the activity is not started automatically
    public ActivityTestRule<TestActivity> mTestActivityRule =
            new ActivityTestRule<>(TestActivity.class, false, false);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    private static void CheckTextInTable(@StringRes int resourceId) {
        ViewInteraction textView3 = onView(
                allOf(withText(resourceId),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText(resourceId)));
    }

    @SuppressWarnings("SameParameterValue")
    private static void CheckTextInTable(@StringRes int resId1, @StringRes int resId2) {

        String text = getString(resId1) + " " + getString((resId2));
        ViewInteraction textView3 = onView(
                allOf(withText(text),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText(text)));
    }

//    private static void CheckTextInTable(String text) {
//        ViewInteraction textView3 = onView(
//                allOf(withText(text),
//                        childAtPosition(
//                                childAtPosition(
//                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
//                                        0),
//                                1),
//                        isDisplayed()));
//        textView3.check(matches(withText(text)));
//    }

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        clearPreferences(mActivityTestRule);
    }

    @Test
    public void cbtInstructions() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(0);

        ViewInteraction textView = onView(
                allOf(withText("www.aquagenx.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("www.aquagenx.com")));

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed()));

        onView(withText("Water - E.coli")).check(matches(isDisplayed()));

        onView(withText(R.string.submitResult)).check(matches(isDisplayed()));

        onView(withText(R.string.prepare_sample)).perform(click());

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        CheckTextInTable(R.string.prepare_area_put_on_gloves);

        CheckTextInTable(R.string.open_growth_medium);

        onView(withContentDescription("1")).check(matches(hasDrawable()));

        ViewInteraction imageView = onView(
                allOf(withContentDescription("1"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.ScrollView.class),
                                        0),
                                6),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        onView(allOf(withId(R.id.pager_indicator),
                childAtPosition(
                        allOf(withId(R.id.layout_footer),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.RelativeLayout.class),
                                        1)),
                        0),
                isDisplayed()));

        nextPage();

        onView(withContentDescription("2")).check(matches(hasDrawable()));

        CheckTextInTable(R.string.dissolve_medium_in_sample_a);

        CheckTextInTable(R.string.dissolve_medium_in_sample_b);

        nextPage();

        onView(withContentDescription("4")).check(matches(hasDrawable()));

        CheckTextInTable(R.string.label_compartment_bag);

        nextPage(3);

        CheckTextInTable(R.string.let_incubate);

        onView(withText(R.string.read_instructions)).perform(click());

        onView(withText(R.string.below25Degrees)).check(matches(isDisplayed()));

        onView(withText(R.string.incubate_in_portable)).check(matches(isDisplayed()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton3.perform(scrollTo(), click());

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        mDevice.waitForIdle();

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonClose), withText("Close"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        mDevice.waitForIdle();

        clickExternalSourceButton(0);

        onView(withId(R.id.button_phase_2)).perform(click());

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()));

        nextPage();

        onView(withContentDescription("8")).check(matches(hasDrawable()));

        CheckTextInTable(R.string.change_colors_to_match, R.string.click_compartments_to_change);

        CheckTextInTable(R.string.note_blue_green_specks);

        nextPage();

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(isDisplayed()));

        nextPage();

        onView(withText("Low Risk")).check(matches(isDisplayed()));

        onView(withText("Safe")).check(matches(isDisplayed()));

        mDevice.pressBack();

        sleep(500);

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.next)).perform(click());

        CheckTextInTable(R.string.dispose_contents_bag);

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        ViewInteraction submitButton = onView(
                allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        submitButton.perform(click());
    }

    @Test
    @RequiresDevice
    public void testInstructionsCbt() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        List<TestInfo> testList = testConfigRepository.getTests(TestType.CBT);
        for (int i = 0; i < TestConstants.CBT_TESTS_COUNT; i++) {

            assertEquals(testList.get(i).getSubtype(), TestType.CBT);
            String uuid = testList.get(i).getUuid();
            String id = uuid.substring(uuid.lastIndexOf("-") + 1);

//            if (("bf80d7197176, ac22c9afa0ab").contains(id))
//
            {
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(AppConfig.EXTERNAL_APP_ACTION);
                Bundle data = new Bundle();
                data.putString(SensorConstants.RESOURCE_ID, uuid);
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage);
                intent.putExtras(data);

                mTestActivityRule.launchActivity(intent);

                int pages = navigateToCbtTest(id);

                sleep(2000);

                mTestActivityRule.launchActivity(intent);

                navigateToCbtTest2(id, pages);

                sleep(1000);

//                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
//                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
//                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");
//                TestHelper.getCurrentActivity().finish();
//                mTestActivityRule.finishActivity();
            }
        }

        Log.d("Caddisfly", jsArrayString.toString());
        Log.d("Caddisfly", listString.toString());
    }

    private int navigateToCbtTest(String id) {

        sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, -1);

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed())).perform(click());

        sleep(1000);

        mDevice.waitForIdle();

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                sleep(1000);

                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {

                ViewInteraction appCompatTextView = onView(
                        allOf(withText("Read Instructions"),
                                childAtPosition(
                                        childAtPosition(
                                                withClassName(is("android.widget.TableLayout")),
                                                7),
                                        2)));
                appCompatTextView.perform(scrollTo(), click());

                sleep(1000);

                takeScreenshot(id, i + 1);

                sleep(200);

                ViewInteraction appCompatButton4 = onView(
                        allOf(withId(android.R.id.button1), withText("OK"),
                                childAtPosition(
                                        childAtPosition(
                                                withId(R.id.buttonPanel),
                                                0),
                                        3)));
                appCompatButton4.perform(scrollTo(), click());

                sleep(1000);

                mDevice.waitForIdle();

                ViewInteraction appCompatButton5 = onView(
                        allOf(withId(R.id.buttonClose), withText("Close"),
                                childAtPosition(
                                        childAtPosition(
                                                withId(R.id.viewPager),
                                                1),
                                        3),
                                isDisplayed()));
                appCompatButton5.perform(click());

                sleep(300);
                break;
            }
        }
        return pages + 1;
    }

    @SuppressWarnings("UnusedReturnValue")
    private int navigateToCbtTest2(String id, int startIndex) {

        sleep(1000);

        mDevice.waitForIdle();

        takeScreenshot(id, startIndex);

        onView(withText(R.string.submitResult)).check(matches(isDisplayed())).perform(click());

        sleep(1000);

        mDevice.waitForIdle();

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                sleep(1000);


                if (i == 2) {
                    ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments),
                            isDisplayed()));

                    customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));
                    customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f));
                    customShapeButton.perform(TestUtil.clickPercent(0.7f, 0.1f));
                } else if (i == 5) {
                    ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments),
                            isDisplayed()));
                    customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));
                }
                takeScreenshot(id, startIndex + i + 1);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {

                ViewInteraction appCompatButton3 = onView(
                        allOf(withId(R.id.buttonSubmit), withText("Submit Result"),
                                childAtPosition(
                                        childAtPosition(
                                                withId(R.id.viewPager),
                                                1),
                                        2),
                                isDisplayed()));
                appCompatButton3.perform(click());

                sleep(300);
                break;
            }
        }
        return pages + 1;
    }

//    @Test
//    @RequiresDevice
//    public void testInstructionsAll() {
//
//        final TestListViewModel viewModel =
//                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);
//
//        List<TestInfo> testList = viewModel.getTests(TestType.CBT);
//
//        for (int i = 0; i < TestConstants.CBT_TESTS_COUNT; i++) {
//            TestInfo testInfo = testList.get(i);
//
//            String id = testInfo.getUuid();
//            id = id.substring(id.lastIndexOf("-") + 1);
//
//            int pages = navigateToTest(i, id);
//
//            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));
//
//            onView(withText(testInfo.getName())).check(matches(isDisplayed()));
//
//            mDevice.pressBack();
//
//            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
//        }
//
//        mActivityTestRule.finishActivity();
//
//        Log.d("Caddisfly", jsArrayString.toString());
//        Log.d("Caddisfly", listString.toString());
//
//    }

//    private int navigateToTest(int index, String id) {
//
//        gotoSurveyForm();
//
//        TestUtil.nextSurveyPage("Coliforms");
//
//        clickExternalSourceButton(index);
//
//        mDevice.waitForIdle();
//
//        sleep(1000);
//
//        takeScreenshot(id, -1);
//
//        mDevice.waitForIdle();
//
//        onView(withText(R.string.prepare_sample)).perform(click());
//
//        int pages = 0;
//        for (int i = 0; i < 17; i++) {
//            try {
//                takeScreenshot(id, pages);
//
//                pages++;
//
//                try {
//                    onView(withId(R.id.button_phase_2)).perform(click());
//                    sleep(600);
//                    takeScreenshot(id, pages);
//                    pages++;
//                    sleep(600);
//                    mDevice.pressBack();
//                } catch (Exception ignore) {
//                }
//
//                onView(withId(R.id.image_pageRight)).perform(click());
//
//            } catch (Exception e) {
//                sleep(600);
//                Random random = new Random(Calendar.getInstance().getTimeInMillis());
//                if (random.nextBoolean()) {
//                    Espresso.pressBack();
//                } else {
//                    mDevice.pressBack();
//                }
//                break;
//            }
//        }
//        return pages;
//    }
}
