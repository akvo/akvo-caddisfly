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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.akvo.caddisfly.util.DrawableMatcher.hasDrawable;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.prevPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.CoreMatchers.not;
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
    public void runSwatchSelectCancelLR() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Tester");

        clickExternalSourceButton(1);

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click());

        sleep(500);

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        assertNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

    }

    @Test
    public void runSwatchSelectLR() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Tester");

        clickExternalSourceButton(0);

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()));

        pressBack();

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()));

        onView(withText(R.string.pt_fill_compartments_2))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()));

        pressBack();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        nextPage();

        prevPage();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        nextPage();

        nextPage();

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_3_reagent_1")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_4_reagent_2")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_5_shake")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_observe")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_6_interface_lr")).check(matches(hasDrawable()));

        sleep(200);

        nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        sleep(500);

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.skip)).check(matches(isDisplayed()));

        onView(withText(R.string.pt_open_lid)).check(matches(isDisplayed()));

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()));

        nextPage();

        onView(withText(R.string.pt_fill_compartments_1)).check(matches(isDisplayed()));

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()));

        sleep(500);

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click());

        sleep(500);

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.55f));
        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.55f));

        sleep(200);

        nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("1 mg/l")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(200);

        nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2 mg/l")).check(matches(isDisplayed()));

        pressBack();

        sleep(200);

        onView(withText(R.string.skip)).check(doesNotExist());

        nextPage();

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        sleep(500);

        onView(withText(R.string.skip)).check(matches(isDisplayed())).perform(click());

        sleep(500);

        nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

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

        TestUtil.nextSurveyPage("Tester");

        clickExternalSourceButton(1);

        sleep(200);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        mDevice.waitForIdle();

        onView(withText(R.string.pt_open_lid))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_1_open")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        nextPage();

        onView(withText(R.string.pt_fill_compartments_1))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_2_fill")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_1))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_3_reagent_1")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_add_reagent_2))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_4_reagent_2")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_close_shake))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_5_shake")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_read_result))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_observe")).check(matches(hasDrawable()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.pt_select_result_next))
                .check(matches(isDisplayed()));

        onView(withContentDescription("pt_6_interface_lr")).check(matches(hasDrawable()));

        sleep(200);

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        nextPage();

        onView(withText(R.string.select_color_intervals))
                .check(matches(isDisplayed()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        ViewInteraction customShapeButton = onView(allOf(withId(R.id.compartments), isDisplayed()));

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.55f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.55f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.4")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("2 mg/l")).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.7f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.3f));

        sleep(200);

        nextPage();

        onView(withText("pH")).check(matches(isDisplayed()));

        onView(withText("7.2")).check(matches(isDisplayed()));

        onView(withText("Chlorine")).check(matches(isDisplayed()));

        onView(withText("5 mg/l")).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())));

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
