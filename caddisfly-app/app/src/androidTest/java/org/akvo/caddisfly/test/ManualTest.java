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


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ManualTest {

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

    @Before
    public void stubCameraIntent() {
        Instrumentation.ActivityResult result = createImageCaptureStub();

        // Stub the Intent.
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
    }

    private Instrumentation.ActivityResult createImageCaptureStub() {
        // Put the drawable in a bundle.
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", BitmapFactory.decodeResource(
                mIntentsRule.getActivity().getResources(), R.drawable.closer));

        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }

    @Test
    public void runManualTurbidityTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(0);

        SystemClock.sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.fill_turbidity_tube)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.empty_turbidity_tube)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.fill_in_value_turbidity_tube)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.clean_turbidity_tube)).check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText(R.string.fill_turbidity_tube))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.empty_turbidity_tube))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.fill_in_value_turbidity_tube))
                .check(matches(isDisplayed()));

        pressBack();

        onView(withText(R.string.skip)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Turbidity: 1234.0 NTU")));

    }

    @Test
    public void runManualPhTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(1);

        SystemClock.sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample_1))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_fill_photo_fill_in))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_rinse))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample_1))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        onView(withText(R.string.skip)).perform(click());

        SystemClock.sleep(1000);

        onView(withText(R.string.takePhoto)).perform(click());

        SystemClock.sleep(3000);

        TestUtil.nextPage();

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 12.0 ")));

    }

    @Test
    public void runManualEcTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Manual");

        clickExternalSourceButton(2);

        SystemClock.sleep(1000);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_fill_photo_fill_in))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_rinse))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        onView(withText(R.string.skip)).perform(click());

        SystemClock.sleep(1000);

        onView(withText(R.string.takePhoto)).perform(click());

        SystemClock.sleep(3000);

        TestUtil.nextPage();

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        SystemClock.sleep(1000);

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("21"), closeSoftKeyboard());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withText("ms/cm")).perform(click());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20"));

        onView(withText(R.string.submitResult)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

        clickExternalSourceButton(2);

        SystemClock.sleep(3000);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.skip)).perform(click());

        SystemClock.sleep(1000);

        onView(withText(R.string.takePhoto)).perform(click());

        SystemClock.sleep(3000);

        TestUtil.nextPage();

        onView(withId(R.id.editResult)).perform(replaceText("200"));

        SystemClock.sleep(3000);

        onView(withText(R.string.submitResult)).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20001"));

        onView(withText(R.string.submitResult)).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20000"));

        onView(withText("ms/cm")).perform(click());

        onView(withText(R.string.submitResult)).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withText(R.string.submitResult)).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

    }
}
