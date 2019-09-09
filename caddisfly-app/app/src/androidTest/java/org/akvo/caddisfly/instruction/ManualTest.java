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


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.CoreMatchers.not;
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
//        bundle.putParcelable("data", BitmapFactory.decodeResource(
//                mIntentsRule.getActivity().getResources(), R.drawable.closer));

        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }

    @Test
    public void runManualTurbidityTest() {

        goToMainScreen();

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Meter");

        clickExternalSourceButton(2);

        sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.fill_turbidity_tube)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())));

        onView(withText(R.string.skip)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.empty_tube_see_black_circle)).check(matches(isDisplayed()));

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.clean_turbidity_tube)).check(matches(isDisplayed()));

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        onView(withText(R.string.fill_turbidity_tube))
                .check(matches(isDisplayed()));

        onView(withText(R.string.skip)).check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.empty_tube_see_black_circle))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.skip)).check(doesNotExist());

        onView(withText(R.string.enter_measured_value)).check(matches(isDisplayed()));

        pressBack();

        sleep(500);

        pressBack();

        onView(withText(R.string.skip)).perform(click());

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        sleep(500);

        onView(withText(R.string.skip)).check(doesNotExist());

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

        mIntentsRule.finishActivity();

        sleep(1000);

        assertNotNull(mDevice.findObject(By.text("Turbidity: 1234.0 NTU")));

        mDevice.pressBack();

        mDevice.pressBack();

    }

    @Test
    public void runManualPhTest() {

        goToMainScreen();

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Meter");

        clickExternalSourceButton(0);

        sleep(1000);

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_50_dip_sample_1))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample_2))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        sleep(500);

        onView(withText(R.string.takePhoto)).check(matches(isDisplayed())).perform(click());

        sleep(1500);

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_rinse))
                .check(matches(isDisplayed()));

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

        onView(withText(R.string.sd_50_dip_sample_2))
                .check(matches(isDisplayed()));

        onView(withText(R.string.skip)).check(doesNotExist());

        TestUtil.nextPage();

        pressBack();

        sleep(500);

        onView(withText(R.string.skip)).check(doesNotExist());

        TestUtil.nextPage();

        sleep(1500);

        onView(withText(R.string.takePhoto)).perform(click());

        sleep(1500);

        onView(withText(R.string.next)).perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        appCompatButton5.perform(click());

        mIntentsRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text("pH: 12.0 ")));

        mDevice.pressBack();

        mDevice.pressBack();

    }

    @Test
    public void runManualEcTest() {

        goToMainScreen();

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Meter");

        clickExternalSourceButton(1);

        sleep(1000);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_70_dip_sample_1))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_70_dip_sample_2))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        sleep(500);

        onView(withText(R.string.takePhoto)).check(matches(isDisplayed())).perform(click());

        sleep(1500);

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard());

        sleep(1000);

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("1234"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("21"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withText("ms/cm")).perform(click());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20"));

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.sd_turn_off))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_70_rinse))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        onView(withText(R.string.sd_70_dip_sample_1))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        onView(withText(R.string.skip)).check(doesNotExist());

        nextPage();

        sleep(500);

        onView(withText(R.string.takePhoto)).perform(click());

        sleep(1500);

        onView(withText(R.string.next)).perform(click());

        sleep(500);

        ViewInteraction submitButton = onView(
                allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        submitButton.perform(click());

        mIntentsRule.finishActivity();

        sleep(1000);

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

        clickExternalSourceButton(1);

        sleep(500);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.skip)).check(doesNotExist());

        nextPage();

        sleep(500);

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        onView(allOf(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click());

        sleep(1000);

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));
    }

    @Test
    public void runManualEcTest2() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Meter");

        clickExternalSourceButton(1);

        sleep(500);

        onView(withText(R.string.next)).perform(click());

        onView(withText(R.string.skip)).check(doesNotExist());

        nextPage();

        sleep(1000);

        onView(withText(R.string.takePhoto)).perform(click());

        sleep(1500);

        onView(withId(R.id.editResult)).perform(replaceText("200"));

        sleep(500);

        onView(withText(R.string.next)).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("20001"), closeSoftKeyboard());

        onView(withText(R.string.next)).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("1999"));

        onView(withText("ms/cm")).perform(click());

        onView(withText(R.string.next)).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withText(R.string.next)).perform(click());

        ViewInteraction submitButton = onView(
                allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        submitButton.perform(click());

        mIntentsRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 1999.0 μS/cm")));

        mDevice.pressBack();

        mDevice.pressBack();
    }
}
