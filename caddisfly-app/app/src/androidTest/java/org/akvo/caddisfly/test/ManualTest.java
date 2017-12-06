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
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestConstant;
import org.akvo.caddisfly.util.TestUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

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

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Before
    public void setUp() {

        loadData(mIntentsRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mIntentsRule.getActivity());
        prefs.edit().clear().apply();

        resetLanguage();
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
                mIntentsRule.getActivity().getResources(), R.drawable.place_device));

        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }

    @Test
    public void runManualTurbidityTest() {

        gotoSurveyForm();

        for (int i = 0; i < 5; i++) {
            clickExternalSourceButton(TestConstant.NEXT);
        }

        clickExternalSourceButton(0, TestConstant.GO_TO_TEST);

        SystemClock.sleep(6000);

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        onView(withText("Connect the turbidity tubes and fill the tube with the water sample until you cannot see the black circle at the yellow bottom anymore."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube with small intervals until you can see the black circle at the yellow bottom again and read out the turbidity on the side of the tube."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Fill in the obtained turbidity value in the app."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube completely, clean if necessary, disconnect tubes and store them."))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText("Connect the turbidity tubes and fill the tube with the water sample until you cannot see the black circle at the yellow bottom anymore."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Empty the tube with small intervals until you can see the black circle at the yellow bottom again and read out the turbidity on the side of the tube."))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText("Fill in the obtained turbidity value in the app."))
                .check(matches(isDisplayed()));

        pressBack();

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.editResult),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("12345"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonSubmitResult), withText("Submit Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.editResult), withText("12345"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("1234"));

        ViewInteraction textInputEditText3 = onView(
                allOf(withId(R.id.editResult), withText("1234"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText3.perform(closeSoftKeyboard());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.buttonSubmitResult), withText("Submit Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton5.perform(click());

        assertNotNull(mDevice.findObject(By.text("Turbidity: 1234.0 NTU")));

    }

    @Test
    public void runManualPhTest() {

        gotoSurveyForm();

        for (int i = 0; i < 5; i++) {
            clickExternalSourceButton(TestConstant.NEXT);
        }

        clickExternalSourceButton(1, TestConstant.GO_TO_TEST);

        SystemClock.sleep(6000);

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample))
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
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_50_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.editResult),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("12345"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonSubmitResult), withText("Submit Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.editResult), withText("12345"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("1234"));

        ViewInteraction textInputEditText3 = onView(
                allOf(withId(R.id.editResult), withText("1234"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText3.perform(closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(allOf(withId(R.id.editResult), withText("1234"),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                3),
                        0),
                isDisplayed())).perform(replaceText("12"));

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("pH: 12.0 ")));

    }

    @Test
    public void runManualEcTest() {

        gotoSurveyForm();

        for (int i = 0; i < 5; i++) {
            clickExternalSourceButton(TestConstant.NEXT);
        }

        clickExternalSourceButton(2, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

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

        onView(withText(R.string.sd_70_maintenance))
                .check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_instructions), withText("Instructions"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(withText(R.string.sd_on))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        onView(withText(R.string.sd_70_dip_sample))
                .check(matches(isDisplayed()));

        TestUtil.nextPage();

        pressBack();

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_prepare), withText("Next"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinatorLayout),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.editResult),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("12345"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.buttonSubmitResult), withText("Submit Result"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.editResult), withText("12345"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("1234"));

        ViewInteraction textInputEditText3 = onView(
                allOf(withId(R.id.editResult), withText("1234"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        textInputEditText3.perform(closeSoftKeyboard());

        onView(withText("Submit Result")).perform(click());

        onView(allOf(withId(R.id.editResult), withText("1234"),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                3),
                        0),
                isDisplayed())).perform(replaceText("21"));

        onView(withText("Submit Result")).perform(click());

        onView(withText("ms/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20"));

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

        clickExternalSourceButton(2, TestConstant.GO_TO_TEST);

        SystemClock.sleep(3000);

        onView(withText("Next")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("200"));

        onView(withText("Submit Result")).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20001"));

        onView(withText("Submit Result")).perform(click());

        onView(withId(R.id.editResult)).perform(replaceText("20000"));

        onView(withText("ms/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        onView(withText("μS/cm")).perform(click());

        onView(withText("Submit Result")).perform(click());

        assertNotNull(mDevice.findObject(By.text("Electrical Conductivity: 20000.0 μS/cm")));

    }
}
