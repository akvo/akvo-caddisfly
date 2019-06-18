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

package org.akvo.caddisfly.test;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class SwatchSelectTest {

    @Rule
    public ActivityTestRule<MainActivity> mIntentsRule = new ActivityTestRule<>(MainActivity.class);

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

        loadData(mIntentsRule.getActivity(), mCurrentLanguage);

        clearPreferences(mIntentsRule);
    }

    @Test
    public void runSwatchSelectLR() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual 2");

        clickExternalSourceButton(1);

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()));

        sleep(200);

        nextPage();

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.55f));
        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.55f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("1 mg/l")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2 mg/l")).check(matches(isDisplayed()));

        pressBack();

        sleep(200);

        nextPage();

        sleep(200);

        onView(withText(R.string.submitResult)).perform(click());

        mIntentsRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")));

        assertNotNull(mDevice.findObject(By.text("Chlorine: 2.0 mg/l")));

        mDevice.pressBack();

        mDevice.pressBack();
    }

    @Test
    public void runSwatchSelectHR() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual 2");

        clickExternalSourceButton(0);

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()));

        sleep(200);

        nextPage();

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.55f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.55f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2 mg/l")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("5 mg/l")).check(matches(isDisplayed()));

        pressBack();

        sleep(200);

        nextPage();

        sleep(200);

        onView(withText(R.string.submitResult)).perform(click());

        mIntentsRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")));

        assertNotNull(mDevice.findObject(By.text("Chlorine: 5.0 mg/l")));

        mDevice.pressBack();

        mDevice.pressBack();
    }
}
