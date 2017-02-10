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

package org.akvo.caddisfly.sensors;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.MainActivity;
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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ECTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

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

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null,
                TestType.COLORIMETRIC_LIQUID, new String[]{}, new String[]{}, new String[]{}, null, null));

        resetLanguage();
    }

    @Test
    public void eCTest() {

        ViewInteraction appCompatButton2 = onView(
                allOf(ViewMatchers.withId(R.id.buttonSensors), withText("Sensors"),
                        withParent(withId(R.id.mainLayout)),
                        isDisplayed()));
        appCompatButton2.perform(click());

        onView(allOf(withId(R.id.textToolbarTitle), withText(R.string.selectTest))).check(matches(isDisplayed()));

        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.list_types),
                        0),
                        isDisplayed()));

        relativeLayout.perform(click());

        SystemClock.sleep(7000);

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()));

        String message = "The expected sensor was not found.\n\n" +
                "Connect the Soil - Electrical Conductivity sensor.";

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

        ViewInteraction relativeLayout6 = onView(
                allOf(childAtPosition(
                        withId(R.id.list_types),
                        1),
                        isDisplayed()));
        relativeLayout6.perform(click());

        SystemClock.sleep(7000);

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()));

        message = "The expected sensor was not found.\n\n" +
                "Connect the Soil - Moisture sensor.";

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

        ViewInteraction relativeLayout2 = onView(
                allOf(childAtPosition(
                        withId(R.id.list_types),
                        2),
                        isDisplayed()));
        relativeLayout2.perform(click());

        SystemClock.sleep(7000);

        onView(allOf(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(ViewMatchers.withId(R.id.buttonSensors), withText("Sensors"),
                        withParent(withId(R.id.mainLayout)),
                        isDisplayed()));
        appCompatButton.perform(click());

        onView(allOf(withId(R.id.textToolbarTitle), withText(R.string.selectTest))).check(matches(isDisplayed()));

        ViewInteraction relativeLayout1 = onView(
                allOf(childAtPosition(
                        withId(R.id.list_types),
                        0),
                        isDisplayed()));

        relativeLayout1.perform(click());

        SystemClock.sleep(7000);

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()));

        message = "The expected sensor was not found.\n\n" +
                "Connect the Soil - Electrical Conductivity sensor.";

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

    }

    @Test
    public void testSensorFromSurvey() {


        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("next");
        clickExternalSourceButton("next");
        clickExternalSourceButton("next");

        clickExternalSourceButton("useExternalSource");
        clickExternalSourceButton("useExternalSource");

        SystemClock.sleep(6000);

        onView(allOf(withId(R.id.textTitle), withText("Water - Electrical Conductivity")));

        onView(allOf(withId(R.id.textToolbarTitle), withText("Sensor!")));

        onView(allOf(withId(R.id.textSubtitle), withText("Sensor connected")));

        onView(allOf(withId(R.id.textUnit), withText("μS/cm")));

        onView(allOf(withId(R.id.textUnit2), withText("°Celsius")));

        onView(withId(R.id.buttonAcceptResult)).perform(click());

    }
}

