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

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clearPreferences;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.getString;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.childAtPosition;
import static org.akvo.caddisfly.util.TestUtil.nextPage;
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

//            for (int i = 0; i < 5; i++) {
//                mDevice.pressBack();
//            }
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
                allOf(withId(R.id.button_phase_2), withText(R.string.submitResult),
                        isDisplayed()));
        appCompatButton2.perform(click());

        sleep(1000);

        nextPage(2);

        mDevice.waitForIdle();

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()));

        ViewInteraction customShapeButton = onView(
                allOf(withId(R.id.compartments),
                        isDisplayed()));
        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.7f, 0.1f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(1000);

        nextPage();

        String[] riskText = getString(R.string.very_high_risk_unsafe).split("/");
        String risk1 = riskText[0].trim();
        String risk2 = riskText[1].trim();

        ViewInteraction textView = onView(
                allOf(withId(R.id.textResult), withText(risk1),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText(risk1)));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textResult2), withText(">100"),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                0),
                        isDisplayed()));
        textView3.check(matches(withText(">100")));

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

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.textResult1), withText(risk2),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                1),
                        isDisplayed()));
        textView6.check(matches(withText(risk2)));

        onView(withText(R.string.result)).check(matches(isDisplayed()));

        nextPage();

        final String result1 = getString(R.string.health_risk_category);
        final String interval = getString(R.string.confidenceInterval);

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.buttonSubmit), withText("Submit Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        mIntentsRule.finishActivity();

        sleep(2000);

        assertNotNull(mDevice.findObject(By.text(result1 + ": " + "Very High Risk / Unsafe ")));
        assertNotNull(mDevice.findObject(By.text("MPN: >100 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text(interval + ": 9435.1 ")));
    }

    @Test
    public void cbtDilutionTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(1);

        mDevice.waitForIdle();

        sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_phase_2), withText(R.string.submitResult),
                        isDisplayed()));
        appCompatButton2.perform(click());

        sleep(1000);

        nextPage(2);

        mDevice.waitForIdle();

        ViewInteraction customShapeButton = onView(
                allOf(withId(R.id.compartments),
                        isDisplayed()));
        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.7f, 0.1f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(100);

        nextPage();

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()));
        onView(withText(">1000")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(3000);

        mDevice.waitForIdle();

        nextPage();

        String[] riskText = getString(R.string.very_high_risk_unsafe).split("/");
        String risk1 = riskText[0].trim();
        String risk2 = riskText[1].trim();

        onView(withText(risk1)).check(matches(isDisplayed()));
        onView(withText(risk2)).check(matches(isDisplayed()));
        onView(withText("483")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        sleep(3000);

        mDevice.waitForIdle();

        nextPage();

        riskText = getString(R.string.low_risk_possibly_safe).split("/");
        risk1 = riskText[0].trim();
        risk2 = riskText[1].trim();
        onView(withText(risk1)).check(matches(isDisplayed()));
        onView(withText(risk2)).check(matches(isDisplayed()));
        onView(withText("58")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(1000);

        mDevice.waitForIdle();

        nextPage();

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()));
        onView(withText(">1000")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));

        nextPage();

        sleep(5000);

        final String result1 = getString(R.string.recreational_health_risk_category);
        final String interval = getString(R.string.confidenceInterval);

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        mIntentsRule.finishActivity();

        sleep(1000);

        assertNotNull(mDevice.findObject(By.text(result1 + ": " + "Very Unsafe ")));
        assertNotNull(mDevice.findObject(By.text("MPN: >1000 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text(interval + ": 94351.0 ")));
    }
}
