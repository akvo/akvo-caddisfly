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
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.DatePicker;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.ui.MainActivity;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormatSymbols;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.TestHelper.changeLanguage;
import static org.akvo.caddisfly.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.TestHelper.currentHashMap;
import static org.akvo.caddisfly.TestHelper.enterDiagnosticMode;
import static org.akvo.caddisfly.TestHelper.goToMainScreen;
import static org.akvo.caddisfly.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.TestHelper.leaveDiagnosticMode;
import static org.akvo.caddisfly.TestHelper.mDevice;
import static org.akvo.caddisfly.TestHelper.saveCalibration;
import static org.akvo.caddisfly.TestHelper.takeScreenshot;
import static org.akvo.caddisfly.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.TestUtil.getText;
import static org.akvo.caddisfly.TestUtil.sleep;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnalysisTest {

    private static final int TEST_START_DELAY = 16000;
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

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null, "FLUOR", "ppm",
                CaddisflyApp.TestType.COLORIMETRIC_LIQUID, true, new String[]{}, new String[]{}, new String[]{}, true, 12));

    }

    @Test
    public void testStartASurvey() {

        saveCalibration("TestValid");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem("TestValid");

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        mDevice.waitForWindowUpdate("", 1000);

    }

    @Test
    public void testChangeTestType() {

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).check(matches(isDisplayed()));

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("chlorine"))).perform(click());

        onView(withText("0" + dfs.getDecimalSeparator() + "50 ppm")).check(matches(isDisplayed()));

    }

    @Test
    public void testRestartAppDuringAnalysis() {

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withId(R.id.editExpiryDate)).perform(click());

        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(2025, 8, 25));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        mDevice.pressHome();

        try {
            mDevice.pressRecentApps();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        sleep(2000);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.waitForWindowUpdate("", 1000);

        clickListViewItem("Automated Tests");

    }

    @Test
    public void testStartNoDilutionTest() {

        saveCalibration("TestValid");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(mActivityRule.getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        clickListViewItem(mActivityRule.getActivity().getString(R.string.noBackdropDetection));

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem("TestValid");

        leaveDiagnosticMode();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(1000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) *
                ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        //Result dialog
        takeScreenshot();

        onView(withId(R.id.buttonOk)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    @Test
    public void testStartHighLevelTest() {

        saveCalibration("HighLevelTest");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(mActivityRule.getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem("HighLevelTest");

        sleep(1000);

        leaveDiagnosticMode();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(1000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) *
                ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(String.format(mActivityRule.getActivity().getString(R.string.tryWithDilutedSample), 2)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        //Test Start Screen
        takeScreenshot();

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) *
                ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(String.format(mActivityRule.getActivity().getString(R.string.tryWithDilutedSample), 5)))
                .check(matches(isCompletelyDisplayed()));

        //High levels found dialog
        takeScreenshot();

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        //Test Progress Screen
        takeScreenshot();

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) *
                ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        double result = Double.valueOf(getText(withId(R.id.textResult)));
        assertTrue("Result is wrong", result > 9);

        onView(withId(R.id.buttonOk)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();
    }
}