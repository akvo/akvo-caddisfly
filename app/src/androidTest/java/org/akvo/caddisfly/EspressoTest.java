/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly;

import android.support.test.espresso.Espresso;
import android.test.ActivityInstrumentationTestCase2;

import org.akvo.caddisfly.ui.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.object.HasToString.hasToString;
import static org.hamcrest.text.StringStartsWith.startsWith;

public class EspressoTest
        extends ActivityInstrumentationTestCase2<MainActivity> {
    public EspressoTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        getActivity();
    }

    @SuppressWarnings("EmptyMethod")
    public void testA() {
    }

    public void testAbout() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();
    }

    public void testCalibrateSwatches() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.calibrate))
                .perform(click());

        onView(withId(R.id.action_swatches))
                .perform(click());

        Espresso.pressBack();

        onView(withId(R.id.action_swatches)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.action_settings)).check(matches(isDisplayed()));
    }

    public void testChangeTestType() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText("0.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("Free Chlorine")).perform(click());

        onView(withText("10.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("Nitrite")).perform(click());

        onView(withText("3.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("pH")).perform(click());

        onView(withText("9.0")).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();
    }

    public void testLanguage() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("العربية"))).perform(click());
    }

    public void testLanguage2() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("हिंदी"))).perform(click());
    }

    public void testLanguage3() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("Français"))).perform(click());
    }

    public void testLanguage4() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("English"))).perform(click());
    }

    public void testStartCalibrate() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("pH")).perform(click());

        onView(withText("9.0")).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button1)).perform(click());

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Espresso.pressBack();

        Espresso.pressBack();
    }

    public void testStartSurvey() {
        onView(withId(R.id.surveyButton)).check(matches(isClickable()));

        onView(withId(R.id.surveyButton)).perform(click());

        //onView(withId(R.id.placeInStandText)).check(matches(isDisplayed()));

        onView(withId(R.id.endSurveyButton)).check(matches(isDisplayed()));

        onView(withId(R.id.endSurveyButton)).perform(click());
    }

//    public void tearDown() throws Exception {
//        Thread.sleep(1000);
//        goBackN();
//        super.tearDown();
//    }
//
//    private void goBackN() {
//        final int N = 3; // how many times to hit back button
//        try {
//            for (int i = 0; i < N; i++)
//                Espresso.pressBack();
//        } catch (NoActivityResumedException e) {
//            //Log.e(TAG, "Closed all activities", e);
//        }
//    }
}