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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.DatePicker;

import org.akvo.caddisfly.ui.MainActivity;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.TestHelper.changeLanguage;
import static org.akvo.caddisfly.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.TestHelper.currentHashMap;
import static org.akvo.caddisfly.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.TestHelper.mDevice;
import static org.akvo.caddisfly.TestHelper.saveCalibration;
import static org.akvo.caddisfly.TestUtil.sleep;
import static org.akvo.caddisfly.TestHelper.startApp;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CalibrationTest {

    //    public void testStartCalibrate() {
//        startCalibrate(2, 4);
//    }
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {

        mDevice = UiDevice.getInstance(getInstrumentation());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        changeLanguage("en");

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityRule.getActivity());
        prefs.edit().clear().apply();

        mActivityRule.launchActivity(mActivityRule.getActivity().getIntent());

    }

    @Test
    public void testOutOfSequence() {

        saveCalibration("OutOfSequence");

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(4000);

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        clickListViewItem("OutOfSequence");

        sleep(2000);

        onView(withText(String.format("%s. %s", mActivityRule.getActivity().getString(R.string.calibrationIsInvalid),
                mActivityRule.getActivity().getString(R.string.tryRecalibrating)))).check(matches(isDisplayed()));

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        clickListViewItem("TestValid");

        sleep(2000);

        onView(withText(String.format("%s. %s", mActivityRule.getActivity().getString(R.string.calibrationIsInvalid),
                mActivityRule.getActivity().getString(R.string.tryRecalibrating)))).check(matches(not(isDisplayed())));

        sleep(2000);

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

    }

    @Test
    public void testIncompleteCalibration() {

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("next");

        clickExternalSourceButton("useExternalSource");

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        String message = mActivityRule.getActivity().getString(R.string.errorCalibrationIncomplete,
                currentHashMap.get("chlorine"));
        message = String.format("%s\r\n\r\n%s", message,
                mActivityRule.getActivity().getString(R.string.doYouWantToCalibrate));

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withText(R.string.cancel)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

    }

    @Test
    public void testExpiryDate() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(4000);

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        clickListViewItem("TestValid");

        sleep(2000);

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, -1);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withText(R.string.save)).perform(click());

        onView(withText((String.format("%s. %s", mActivityRule.getActivity().getString(R.string.expired),
                mActivityRule.getActivity().getString(R.string.calibrateWithNewReagent))))).check(matches(isDisplayed()));

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withText(R.string.cancel)).perform(click());

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(2000);

        String message = String.format("%s\r\n\r\n%s",
                mActivityRule.getActivity().getString(R.string.errorCalibrationExpired),
                mActivityRule.getActivity().getString(R.string.orderFreshBatch));

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withText(R.string.ok)).perform(click());

        startApp();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(2025, 8, 25));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withText((String.format("%s. %s", mActivityRule.getActivity().getString(R.string.expired),
                mActivityRule.getActivity().getString(R.string.calibrateWithNewReagent))))).check(matches(not(isDisplayed())));

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(2000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

    }
}