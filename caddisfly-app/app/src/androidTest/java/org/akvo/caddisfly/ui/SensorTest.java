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

package org.akvo.caddisfly.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.akvo.caddisfly.R;
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
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.currentHashMap;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SensorTest {

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

    @Before
    public void setUp() {

        loadData(mActivityRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();

        resetLanguage();
    }

    @Test
    public void testEC() {

        goToMainScreen();

        mDevice.waitForWindowUpdate("", 2000);

        onView(ViewMatchers.withText(R.string.sensors)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        onView(withText(R.string.electricalConductivity)).check(matches(isDisplayed()));

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

        mDevice.waitForWindowUpdate("", 2000);

        onView(withContentDescription(mActivityRule.getActivity()
                .getString(R.string.deviceConnectSensor))).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(R.string.electricalConductivity)).perform(click());

        try {
            onView(withText(R.string.warning)).check(matches(isDisplayed()));

            onView(withText(R.string.calibrate)).perform(click());

            onView(withId(R.id.buttonStartCalibrate)).perform(click());

            onView(withText(R.string.sensorNotFound)).check(matches(isDisplayed()));

            String message = mActivityRule.getActivity().getString(R.string.connectCorrectSensor,
                    mActivityRule.getActivity().getString(R.string.electricalConductivity));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception ex) {
            String message = String.format("%s\r\n\r\n%s",
                    mActivityRule.getActivity().getString(R.string.phoneDoesNotSupport),
                    mActivityRule.getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withText(R.string.ok)).perform(click());
        }
    }

    @Test
    public void testCalibrateSensor() {
        //onView(withId(R.id.actionSettings)).perform(click());

        //onView(withText(R.string.calibrateSummary)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());
        try {
            onView(withText(R.string.warning)).check(matches(isDisplayed()));

            onView(withText(R.string.calibrate)).perform(click());

            onView(withId(R.id.buttonStartCalibrate)).perform(click());

            onView(withText(R.string.sensorNotFound)).check(matches(isDisplayed()));

            String message = mActivityRule.getActivity().getString(R.string.connectCorrectSensor,
                    mActivityRule.getActivity().getString(R.string.electricalConductivity));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception ex) {
            String message = String.format("%s\r\n\r\n%s",
                    mActivityRule.getActivity().getString(R.string.phoneDoesNotSupport),
                    mActivityRule.getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withText(R.string.ok)).perform(click());
        }

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

    }
}