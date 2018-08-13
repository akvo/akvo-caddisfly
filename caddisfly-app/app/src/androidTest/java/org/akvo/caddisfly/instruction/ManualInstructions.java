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


import android.arch.lifecycle.ViewModelProviders;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.TestConstants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ManualInstructions {

    private final StringBuilder jsArrayString = new StringBuilder();

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    @Before
    public void setUp() {
        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);
        clearPreferences(mActivityTestRule);
    }

    @Test
    @RequiresDevice
    public void testInstructionsAll() {

        final TestListViewModel viewModel =
                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);

        List<TestInfo> testList = viewModel.getTests(TestType.MANUAL);

        for (int i = 0; i < TestConstants.MANUAL_TESTS_COUNT; i++) {
            TestInfo testInfo = testList.get(i);

            String id = testInfo.getUuid();
            id = id.substring(id.lastIndexOf("-") + 1, id.length());

            int pages = navigateToTest("Manual", i, id);

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

            onView(withText(testInfo.getName())).check(matches(isDisplayed()));

            mDevice.pressBack();

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }
        Log.d("Caddisfly", jsArrayString.toString());
    }

    @Test
    @RequiresDevice
    public void testInstructionsSwatchSelect() {

        final TestListViewModel viewModel =
                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);

        List<TestInfo> testList = viewModel.getTests(TestType.MANUAL_COLOR_SELECT);

        for (int i = 0; i < TestConstants.MANUAL_SELECT_TESTS_COUNT; i++) {
            TestInfo testInfo = testList.get(i);

            String id = testInfo.getUuid();
            id = id.substring(id.lastIndexOf("-") + 1, id.length());

            int pages = navigateToTest("Manual 2", i, id);

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

            onView(withText(testInfo.getName())).check(matches(isDisplayed()));

            mDevice.pressBack();

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }
        Log.d("Caddisfly", jsArrayString.toString());
    }

    private int navigateToTest(String tabName, int index, String id) {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(tabName);

        clickExternalSourceButton(index);

        mDevice.waitForIdle();

        sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withText(getString(mActivityTestRule.getActivity(), R.string.instructions))).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                sleep(600);
                Random random = new Random(Calendar.getInstance().getTimeInMillis());
                if (random.nextBoolean()) {
                    Espresso.pressBack();
                } else {
                    mDevice.pressBack();
                }
                break;
            }
        }
        return pages;
    }

    @Test
    public void instructionTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(1);

        sleep(1000);

        //onView(allOf(withId(R.id.textTitle), withText("Lovibond SD 50 pH me..."))).check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText(R.string.sd_on)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample)).check(matches(isDisplayed()));

    }
}
