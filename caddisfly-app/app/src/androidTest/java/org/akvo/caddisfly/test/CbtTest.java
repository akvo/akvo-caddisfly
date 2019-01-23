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
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;

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
import static android.support.test.espresso.Espresso.pressBack;
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
import static org.akvo.caddisfly.util.TestHelper.getString;
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
                allOf(withId(R.id.button_prepare), withText(R.string.next),
                        isDisplayed()));
        appCompatButton2.perform(click());

        sleep(3000);

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

        onView(allOf(withId(R.id.buttonNext), withText(R.string.next),
                isDisplayed())).perform(click());

        String[] riskText = getString(mIntentsRule.getActivity(), R.string.very_high_risk_unsafe).split("/");
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
                allOf(withText(R.string.confidenceInterval),
                        childAtPosition(
                                allOf(withId(R.id.layoutResult2),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView5.check(matches(withText(R.string.confidenceInterval)));

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

        ViewInteraction button = onView(
                allOf(withId(R.id.buttonAcceptResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        onView(withText(R.string.result)).check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonAcceptResult), withText(R.string.acceptResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        assertNotNull(mDevice.findObject(By.text(getString(mIntentsRule.getActivity(), R.string.health_risk_category) + ": "
                + getString(mIntentsRule.getActivity(), R.string.very_high_risk_unsafe) + " ")));
        assertNotNull(mDevice.findObject(By.text("MPN: >100 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text(getString(mIntentsRule.getActivity(), R.string.confidenceInterval) + ": 9435.1 ")));
    }

    @Test
    public void cbtDilutionTest() {

        gotoSurveyForm();

        TestUtil.nextSurveyPage("Coliforms");

        clickExternalSourceButton(1);

        mDevice.waitForIdle();

        sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_prepare), withText(R.string.next),
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

        sleep(100);

        onView(allOf(withId(R.id.buttonNext), withText(R.string.next),
                isDisplayed())).perform(click());

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()));
        onView(withText(">1000")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));
        onView(withText("94351.0")).check(matches(isDisplayed()));
        onView(withText(R.string.confidenceInterval)).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(3000);

        mDevice.waitForIdle();

        onView(allOf(withId(R.id.buttonNext), withText(R.string.next),
                isDisplayed())).perform(click());

        String[] riskText = getString(mIntentsRule.getActivity(), R.string.very_high_risk_unsafe).split("/");
        String risk1 = riskText[0].trim();
        String risk2 = riskText[1].trim();

        onView(withText(risk1)).check(matches(isDisplayed()));
        onView(withText(risk2)).check(matches(isDisplayed()));
        onView(withText("483")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));
        onView(withText("3519.1")).check(matches(isDisplayed()));
        onView(withText(R.string.confidenceInterval)).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        sleep(3000);

        mDevice.waitForIdle();

        onView(allOf(withId(R.id.buttonNext), withText(R.string.next),
                isDisplayed())).perform(click());

        riskText = getString(mIntentsRule.getActivity(), R.string.low_risk_possibly_safe).split("/");
        risk1 = riskText[0].trim();
        risk2 = riskText[1].trim();
        onView(withText(risk1)).check(matches(isDisplayed()));
        onView(withText(risk2)).check(matches(isDisplayed()));
        onView(withText("58")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));
        onView(withText("168.7")).check(matches(isDisplayed()));
        onView(withText(R.string.confidenceInterval)).check(matches(isDisplayed()));

        pressBack();

        customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f));

        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f));

        sleep(3000);

        mDevice.waitForIdle();

        onView(allOf(withId(R.id.buttonNext), withText(R.string.next),
                isDisplayed())).perform(click());

        onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()));
        onView(withText(">1000")).check(matches(isDisplayed()));
        onView(withText("MPN/100ml")).check(matches(isDisplayed()));
        onView(withText("94351.0")).check(matches(isDisplayed()));
        onView(withText(R.string.confidenceInterval)).check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonAcceptResult), withText(R.string.acceptResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.fragment_container),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        assertNotNull(mDevice.findObject(By.text(getString(mIntentsRule.getActivity(), R.string.recreational_health_risk_category) + ": "
                + getString(mIntentsRule.getActivity(), R.string.very_unsafe) + " ")));
        assertNotNull(mDevice.findObject(By.text("MPN: >1000 MPN/100ml")));
        assertNotNull(mDevice.findObject(By.text(getString(mIntentsRule.getActivity(), R.string.confidenceInterval) + ": 94351.0 ")));
    }
}
