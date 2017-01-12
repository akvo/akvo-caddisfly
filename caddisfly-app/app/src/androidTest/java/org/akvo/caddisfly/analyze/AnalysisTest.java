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

package org.akvo.caddisfly.analyze;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.widget.DatePicker;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.ui.MainActivity;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DecimalFormatSymbols;
import java.util.Calendar;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
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
import static org.akvo.caddisfly.util.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.util.TestUtil.getText;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnalysisTest {

    private static final int TEST_START_DELAY = 16000;
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

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null,
                TestType.COLORIMETRIC_LIQUID, new String[]{}, new String[]{}, new String[]{}, null, null));

        resetLanguage();
    }

    @Test
    public void testStartHighLevelTest() {

        saveCalibration("HighLevelTest");

        onView(ViewMatchers.withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion();

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        clickListViewItem("HighLevelTest");

        sleep(1000);

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        clickListViewItem(mActivityRule.getActivity().getString(R.string.noBackdropDetection));

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("    "), closeSoftKeyboard());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 2);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withText(R.string.save)).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton(0);

        sleep(1000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        //onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton(0);

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        //Test Start Screen
        takeScreenshot();

        //onView(withId(R.id.buttonStart)).perform(click());

//        onView(allOf(withId(R.id.textDilution), withText(mActivityRule.getActivity()
//                .getString(R.string.testWithDilution)))).check(matches(isCompletelyDisplayed()));

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(mActivityRule.getActivity().getString(R.string.testWithDilution)))
                .check(matches(isCompletelyDisplayed()));

        //High levels found dialog
        takeScreenshot();

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton(0);

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        //onView(withId(R.id.buttonStart)).perform(click());

//        onView(allOf(withId(R.id.textDilution), withText(String.format(mActivityRule.getActivity()
//                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        //Test Progress Screen
        takeScreenshot();

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        double result = Double.valueOf(getText(withId(R.id.textResult)).replace(">", "").trim());
        assertTrue("Result is wrong", result > 9);

        onView(withId(R.id.buttonOk)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();
    }

    @Test
    public void testStartNoDilutionTest() {

        saveCalibration("TestValid");

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion();

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(1000);

        onData(hasToString(startsWith("TestValid"))).perform(click());

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        clickListViewItem(mActivityRule.getActivity().getString(R.string.noBackdropDetection));

        leaveDiagnosticMode();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.fabEditCalibration)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(clearText(), closeSoftKeyboard());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("    "), closeSoftKeyboard());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editExpiryDate)).perform(click());

        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 2);
        onView(withClassName((Matchers.equalTo(DatePicker.class.getName()))))
                .perform(PickerActions.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)));

        onView(withId(android.R.id.button1)).perform(click());

        onView(withText(R.string.save)).perform(click());

        onView(withId(R.id.editBatchCode))
                .perform(typeText("TEST 123#*@!"), closeSoftKeyboard());

        onView(withText(R.string.save)).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("1" + dfs.getDecimalSeparator() + "50 ppm")).perform(click());

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton(0);

        sleep(1000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        //onView(withId(R.id.buttonStart)).perform(click());

        sleep(TEST_START_DELAY + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000)
                * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

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

}
