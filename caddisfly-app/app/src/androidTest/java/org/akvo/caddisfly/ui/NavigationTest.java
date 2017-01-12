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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.widget.DatePicker;

import org.akvo.caddisfly.R;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.text.DecimalFormatSymbols;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.currentHashMap;
import static org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestHelper.resetLanguage;
import static org.akvo.caddisfly.util.TestHelper.saveCalibration;
import static org.akvo.caddisfly.util.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationTest {
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
    public void testNavigateAll() {

        saveCalibration("TestInvalid");

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        mDevice.waitForWindowUpdate("", 2000);

        goToMainScreen();

        //Main Screen
        takeScreenshot();

        onView(ViewMatchers.withId(R.id.actionSettings)).perform(click());

        //Settings Screen
        takeScreenshot();

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        //About Screen
        takeScreenshot();

        Espresso.pressBack();

        onView(withText(R.string.language)).perform(click());

        mDevice.waitForWindowUpdate("", 1000);

        //Language Dialog
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(4000);

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onData(hasToString(startsWith("TestInvalid"))).perform(click());

        sleep(2000);

        onView(withText(String.format("%s. %s", mActivityRule.getActivity()
                        .getString(R.string.calibrationIsInvalid),
                mActivityRule.getActivity().getString(R.string.tryRecalibrating)))).check(matches(isDisplayed()));

        leaveDiagnosticMode();

        sleep(4000);

        onView(withText(R.string.calibrate)).perform(click());

        //Test Types Screen
        takeScreenshot();

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        //Calibrate Swatches Screen
        takeScreenshot();

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withId(R.id.editExpiryDate)).perform(click());

        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(2025, 8, 25));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        //onView(withId(R.id.buttonStart)).perform(click());

        saveCalibration("TestValid");

        goToMainScreen();

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

        onData(hasToString(startsWith("TestValid"))).perform(click());

        sleep(2000);

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        try {
            onView(withText(R.string.incorrectCalibrationCanAffect)).check(matches(isDisplayed()));
            //Calibrate EC Warning
            takeScreenshot();

            onView(withText(R.string.cancel)).perform(click());

            onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

            onView(withText(R.string.warning)).check(matches(isDisplayed()));

            onView(withText(R.string.calibrate)).perform(click());

            //Calibrate EC
            takeScreenshot();

            onView(withId(R.id.buttonStartCalibrate)).perform(click());

            //EC not found dialog
            takeScreenshot();

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception ex) {
            String message = String.format("%s\r\n\r\n%s", mActivityRule.getActivity().getString(R.string.phoneDoesNotSupport),
                    mActivityRule.getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            //Feature not supported
            takeScreenshot();

            onView(withText(R.string.ok)).perform(click());
        }

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton(0);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        //Dilution dialog
        takeScreenshot();

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        mActivityRule.launchActivity(new Intent());

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        //clickExternalSourceButton("next");

//        clickExternalSourceButton("useExternalSource", 0);
//
//        //Calibration incomplete
//        takeScreenshot();
//
//        onView(withId(android.R.id.button2)).perform(click());

        //EC
        //clickExternalSourceButton(1);

        //Connect EC Sensor Screen
        //takeScreenshot();

        //mDevice.pressBack();

        //pH
//        clickExternalSourceButton(2);

        //onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        //onView(withText(R.string.ok)).perform(click());

        mDevice.pressBack();

        //Caffeine
        //clickExternalSourceButton(3);

        //Test type not available
        //takeScreenshot();

        //mDevice.pressBack();
        //onView(withId(android.R.id.button1)).perform(click());

    }
}
