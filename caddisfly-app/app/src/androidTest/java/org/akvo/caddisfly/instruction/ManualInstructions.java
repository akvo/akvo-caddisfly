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

import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

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

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

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
    public void testInstructionsSwatchSelect() {

        goToMainScreen();

        final TestListViewModel viewModel =
                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);

        List<TestInfo> testList = viewModel.getTests(TestType.MANUAL_COLOR_SELECT);

        for (int i = 0; i < TestConstants.MANUAL_SELECT_TESTS_COUNT; i++) {
            TestInfo testInfo = testList.get(i);

            String id = testInfo.getUuid();
            id = id.substring(id.lastIndexOf("-") + 1);

            int pages = navigateToTest("Manual 2", i, id, testInfo.getName());

            sleep(500);

            ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

            customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

            customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

            takeScreenshot(id, pages);

            nextPage();

            takeScreenshot(id, ++pages);

            ViewInteraction submitButton = onView(
                    allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.viewPager),
                                            1),
                                    2),
                            isDisplayed()));
            submitButton.perform(click());

            mDevice.pressBack();

            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
        }

        mActivityTestRule.finishActivity();

        mDevice.pressBack();

        mDevice.pressBack();

        Log.d("CaddisflyTests", jsArrayString.toString());
    }

    @SuppressWarnings("SameParameterValue")
    private int navigateToTest(String tabName, int index, String id, String testName) {

        gotoSurveyForm();

        TestUtil.nextSurveyPage(tabName);

        clickExternalSourceButton(index);

        mDevice.waitForIdle();

        sleep(200);

        takeScreenshot(id, -1);

        mDevice.waitForIdle();

        onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        int pages = 0;
        for (int i = 0; i < 17; i++) {

            try {

                nextPage();

                sleep(200);

                pressBack();

                sleep(200);

                takeScreenshot(id, i);

                sleep(100);

                nextPage();

                sleep(200);

                pages++;

            } catch (Exception e) {
                sleep(200);
                break;
            }
        }
        return pages;
    }

    @Test
    public void instructionTest() {

        goToMainScreen();

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(1);

        sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on)).check(matches(isDisplayed()));

        onView(withText(R.string.sd_50_dip_sample_1)).check(matches(isDisplayed()));

        mActivityTestRule.finishActivity();
    }
}
