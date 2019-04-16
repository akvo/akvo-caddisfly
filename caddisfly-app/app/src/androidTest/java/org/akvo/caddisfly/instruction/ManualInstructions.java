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

import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.sleep;

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
            id = id.substring(id.lastIndexOf("-") + 1);

            int pages = navigateToTest2("Manual", i, id);

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
            id = id.substring(id.lastIndexOf("-") + 1);

            int pages = navigateToTest("Manual 2", i, id);

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

            onView(withText(testInfo.getName())).check(matches(isDisplayed()));

            mDevice.pressBack();

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }
        Log.d("Caddisfly", jsArrayString.toString());
    }

    @SuppressWarnings("SameParameterValue")
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
        return pages + 1;
    }

    @SuppressWarnings("SameParameterValue")
    private int navigateToTest2(String tabName, int index, String id) {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(tabName);

        clickExternalSourceButton(index);

        mDevice.waitForIdle();

        sleep(1000);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        int pages = 0;
        for (int i = 0; i < 17; i++) {
            pages++;

            try {
                takeScreenshot(id, i);

                onView(withId(R.id.image_pageRight)).perform(click());

            } catch (Exception e) {
                sleep(600);
                break;
            }
        }
        return pages + 1;
    }

    @Test
    public void instructionTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(1);

        sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on)).check(matches(isDisplayed()));

        onView(withText(R.string.sd_50_dip_sample_1)).check(matches(isDisplayed()));

    }
}
