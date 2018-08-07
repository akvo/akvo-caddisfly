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
import android.provider.MediaStore;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
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
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CbtTest {

//    @Rule
//    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE, CAMERA);

//    @Rule
//    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

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

        stubCameraIntent();
    }

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
    public void cbtTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(0);

        mDevice.waitForIdle();

        sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        isDisplayed()));
        appCompatButton2.perform(click());

        sleep(3000);

        mDevice.waitForIdle();

        ViewInteraction customShapeButton = onView(
                allOf(withId(R.id.compartments),
                        isDisplayed()));
        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.7f, 0.1f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(1000);

        onView(allOf(withId(R.id.buttonNext), withText("Next"),
                isDisplayed())).perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.textResult), withText("Very High Risk"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Very High Risk")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textResult2), withText("> 100"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                0),
                        isDisplayed()));
        textView3.check(matches(withText("> 100")));

        ViewInteraction textView2 = onView(
                allOf(withText("MPN/100ml"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("MPN/100ml")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textResult3), withText("9435.1"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult2),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                2)),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("9435.1")));

        ViewInteraction textView5 = onView(
                allOf(withText("Upper 95% Confidence Interval"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult2),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView5.check(matches(withText("Upper 95% Confidence Interval")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.textResult1), withText("Unsafe"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                1),
                        isDisplayed()));
        textView6.check(matches(withText("Unsafe")));

        ViewInteraction button = onView(
                allOf(withId(R.id.buttonAcceptResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonAcceptResult), withText("Accept Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        assertNotNull(mDevice.findObject(By.text("Health Risk Category (Based on MPN and Confidence Interval): Very High Risk / Unsafe ")));
        assertNotNull(mDevice.findObject(By.text("MPN: > 100 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text("Upper 95% Confidence Interval: 9435.1 ")));
    }

    @Test
    public void cbtDilutionTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(1);

        mDevice.waitForIdle();

        sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        isDisplayed()));
        appCompatButton2.perform(click());

        sleep(3000);

        mDevice.waitForIdle();

        ViewInteraction customShapeButton = onView(
                allOf(withId(R.id.compartments),
                        isDisplayed()));
        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.7f, 0.1f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(1000);

        onView(allOf(withId(R.id.buttonNext), withText("Next"),
                isDisplayed())).perform(click());

        onView(withText("Very High Risk")).check(matches(isDisplayed()));
        onView(withText("Non-compliant")).check(matches(isDisplayed()));
        onView(withText("> 1000")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));
        onView(withText("94351.0")).check(matches(isDisplayed()));
        onView(withText("Upper 95% Confidence Interval")).check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonAcceptResult), withText("Accept Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        assertNotNull(mDevice.findObject(By.text("Agricultural Waters Single Sample Water Quality (Based on MPN and Confidence Interval): Very High Risk / Non-compliant ")));
        assertNotNull(mDevice.findObject(By.text("MPN: > 1000 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text("Upper 95% Confidence Interval: 94351.0 ")));
    }
}
