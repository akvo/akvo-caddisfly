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


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.filters.LargeTest;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SwatchSelectTest {

    @Rule
    public IntentsTestRule<MainActivity> mIntentsRule = new IntentsTestRule<>(MainActivity.class);

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

        SystemClock.sleep(2000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        SystemClock.sleep(3000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        SystemClock.sleep(2000);

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.5f));

        sleep(1000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("1.0 mg/l")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(1000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2.0 mg/l")).check(matches(isDisplayed()));

        pressBack();

        sleep(3000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("Save")).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")));

        assertNotNull(mDevice.findObject(By.text("Chlorine: 2.0 mg/l")));

    }

    @Test
    public void runSwatchSelectHR() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual 2");

        clickExternalSourceButton(0);

        SystemClock.sleep(2000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        SystemClock.sleep(3000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        SystemClock.sleep(2000);

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.5f));

        sleep(1000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2.0 mg/l")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(1000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("5.0 mg/l")).check(matches(isDisplayed()));

        pressBack();

        sleep(3000);

        onView(withText("Next")).check(matches(isDisplayed())).perform(click());

        onView(withText("Save")).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 7.2 ")));

        assertNotNull(mDevice.findObject(By.text("Chlorine: 5.0 mg/l")));

    }
}
