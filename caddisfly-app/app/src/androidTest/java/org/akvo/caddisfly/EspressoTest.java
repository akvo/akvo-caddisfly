/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.NetworkUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.object.HasToString.hasToString;

@SuppressWarnings("unused")
public class EspressoTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String mCurrentLanguage = "en";
    private static final boolean mTakeScreenshots = false;
    private static int mCounter = 0;
    private final HashMap<String, String> stringHashMapEN = new HashMap<>();
    private final HashMap<String, String> stringHashMapFR = new HashMap<>();
    private HashMap<String, String> currentHashMap;
    private UiDevice mDevice;

    @SuppressWarnings("unused")
    public EspressoTest() {
        super(MainActivity.class);
    }

    private static Matcher<View> withBackgroundColor(final int color) {
        Checks.checkNotNull(color);
        return new BoundedMatcher<View, Button>(Button.class) {
            @Override
            public boolean matchesSafely(Button button) {
                int buttonColor = ((ColorDrawable) button.getBackground()).getColor();
                return Color.red(color) == Color.red(buttonColor) &&
                        Color.green(color) == Color.green(buttonColor) &&
                        Color.blue(color) == Color.blue(buttonColor);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with background color: " + color);
            }
        };
    }

    private static Matcher<String> isEmpty() {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String target) {
                return target.length() == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is empty");
            }
        };
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        stringHashMapEN.put("language", "English");
        stringHashMapEN.put("fluoride", "Fluoride");
        stringHashMapEN.put("chlorine", "Free Chlorine");
        stringHashMapEN.put("electricalConductivity", "Electrical Conductivity");

        stringHashMapFR.put("language", "Français");
        stringHashMapFR.put("fluoride", "Fluorure");
        stringHashMapFR.put("chlorine", "Chlore Libre");
        stringHashMapFR.put("electricalConductivity", "Conductivité Électrique");

        if (mCurrentLanguage.equals("en")) {
            currentHashMap = stringHashMapEN;
        } else {
            currentHashMap = stringHashMapFR;
        }

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        getActivity();

    }

    @SuppressWarnings("EmptyMethod")
    public void test000() {

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

    }

    public void test001_Language() {

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = MainApp.getVersion(getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withId(R.id.disableDiagnosticButton)).check(matches(isDisplayed()));

        leaveDiagnosticMode();

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }

    public void test002_Screenshots() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        String path = Environment.getExternalStorageDirectory().getPath() + "/org.akvo.caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        resetLanguage();

        mDevice.waitForWindowUpdate("", 2000);

        goToMainScreen();

        final Button button = (Button) getActivity().findViewById(R.id.surveyButton);
        assertNotNull(button);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Main Screen
        takeScreenshot();

        onView(withId(R.id.action_settings)).perform(click());

        //Settings Screen
        takeScreenshot();

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        //About Screen
        takeScreenshot();

        enterDiagnosticMode();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menu_load)).perform(click());

        onView(withText("Test")).perform(click());

        goToMainScreen();

        leaveDiagnosticMode();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        mDevice.waitForWindowUpdate("", 1000);

        //Language Dialog
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        if (!NetworkUtils.isOnline(getActivity())) {
            onView(withText(R.string.updateCheck)).perform(click());

            onView(withText(R.string.noInternetConnection)).check(matches(isDisplayed()));
            onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));
            mDevice.waitForWindowUpdate("", 1000);

            //Enable Internet Dialog
            takeScreenshot();

            Espresso.pressBack();

        }

        onView(withText(R.string.calibrate)).perform(click());

        //Test Types Screen
        takeScreenshot();

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        //Ranges Screen
        takeScreenshot();

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        onView(withText(R.string.incorrectCalibrationCanAffect)).check(matches(isDisplayed()));

        //Calibrate EC Warning
        takeScreenshot();

        onView(withText(R.string.cancel)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        onView(withText(R.string.warning)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).perform(click());

        //Calibrate EC
        takeScreenshot();

        onView(withId(R.id.startButton)).perform(click());

        //EC not found dialog
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        goToMainScreen();

        onView(withId(R.id.surveyButton)).check(matches(isClickable()));

        onView(withId(R.id.surveyButton)).perform(click());

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

        clickListViewItem("CREATE NEW DATA POINT");

        clickListViewItem("All Tests");

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.noDilutionButton)).check(matches(isDisplayed()));

        //Dilution dialog
        takeScreenshot();

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        startApp();

        onView(withId(R.id.surveyButton)).check(matches(isClickable()));

        onView(withId(R.id.surveyButton)).perform(click());

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

        clickListViewItem("CREATE NEW DATA POINT");

        clickListViewItem("All Tests");

        try {
            mDevice.findObject(new UiSelector().text("Next")).click();

            mDevice.waitForWindowUpdate("", 2000);

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Calibration incomplete
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        try {
            mDevice.findObject(new UiSelector().text("Next")).click();

            mDevice.waitForWindowUpdate("", 2000);

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Connect EC Sensor Screen
        takeScreenshot();

        onView(withId(R.id.backButton)).perform(click());

        try {
            mDevice.findObject(new UiSelector().text("Next")).click();

            mDevice.waitForWindowUpdate("", 2000);

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.backButton)).perform(click());

        try {
            mDevice.findObject(new UiSelector().text("Next")).click();

            mDevice.waitForWindowUpdate("", 2000);

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Test type not available
        takeScreenshot();

        onView(withId(android.R.id.button1)).perform(click());

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();


    }

    private void startApp() {
        // Start from the home screen
        mDevice.pressHome();
        mDevice.waitForWindowUpdate("", 2000);
        UiObject2 allAppsButton = mDevice.findObject(By.desc("Apps"));
        allAppsButton.click();
        mDevice.waitForWindowUpdate("", 2000);

        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));
        appViews.setAsHorizontalList();

        UiObject settingsApp = null;
        try {
            String appName = "Akvo Caddisfly";
            settingsApp = appViews.getChildByText(new UiSelector().className(TextView.class.getName()), appName);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (settingsApp != null) {
                settingsApp.clickAndWaitForNewWindow();
            }
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        mDevice.waitForWindowUpdate("", 2000);

        assertTrue("Unable to detect app", settingsApp != null);
    }

    private void goToMainScreen() {

        Button button = (Button) getCurrentActivity().findViewById(R.id.surveyButton);
        while (button == null) {
            Espresso.pressBack();
            button = (Button) getCurrentActivity().findViewById(R.id.surveyButton);
        }
    }

    public void testCalibrateSwatches() {
        calibrateSwatches();
    }

    private void calibrateSwatches() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.calibrate))
                .perform(click());

//            onView(withId(R.id.action_swatches))
//                    .perform(click());
//
//            Espresso.pressBack();
//
//            onView(withId(R.id.action_swatches)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.action_settings)).check(matches(isDisplayed()));
    }

    public void testChangeTestType() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(currentHashMap.get("chlorine"))).perform(click());

        onView(withText("0" + dfs.getDecimalSeparator() + "50 ppm")).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();
    }

    public void testStartASurvey() {

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        startApp();

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.enableUserMode)).check(matches(isDisplayed()));

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menu_load)).perform(click());

        onView(withText("Test")).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withId(R.id.surveyButton)).check(matches(isClickable()));

        onView(withId(R.id.surveyButton)).perform(click());

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

        clickListViewItem("CREATE NEW DATA POINT");

        clickListViewItem("All Tests");

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.noDilutionButton)).check(matches(isDisplayed()));

        onView(withId(R.id.percentButton1)).check(matches(isDisplayed()));

        onView(withId(R.id.percentButton2)).check(matches(isDisplayed()));

        onView(withId(R.id.noDilutionButton)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        mDevice.waitForWindowUpdate("", 1000);

        //onView(withId(R.id.placeInStandText)).check(matches(isDisplayed()));

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
    }

    public void testCalibrateSensor() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrateSummary)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        onView(withText(R.string.warning)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withText(R.string.sensorNotFound)).check(matches(isDisplayed()));
        onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        Espresso.pressBack();
    }

    public void testCheckUpdate() {

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.updateCheck)).check(matches(isDisplayed()));

        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));

        if (!NetworkUtils.isOnline(getActivity())) {

            onView(withText(R.string.updateCheck)).perform(click());

            onView(withText(R.string.noInternetConnection)).check(matches(isDisplayed()));
            onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));

            Espresso.pressBack();

        }

        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));

        Espresso.pressBack();

    }

    public void testDiagnosticMode() {

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.enableUserMode)).check(matches(isDisplayed()));

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.action_swatches)).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();

    }

    public void testLanguage4() {
        onView(withId(R.id.action_settings))
                .perform(click());

//        onView(withText(R.string.language))
//                .check(matches(not(isDisplayed())))
//                .perform(scrollTo())
//                .check(matches(isDisplayed()))
//                .perform(scrollTo());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("Français"))).perform(click());
    }

    public void testLanguage5() {
        onView(withId(R.id.action_settings))
                .perform(click());

//        onView(withText(R.string.language))
//                .check(matches(not(isDisplayed())))
//                .perform(scrollTo())
//                .check(matches(isDisplayed()))
//                .perform(scrollTo());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("English"))).perform(click());
    }

    private void resetLanguage() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }


    private void enterDiagnosticMode() {

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }
    }

    private void leaveDiagnosticMode() {
        onView(withId(R.id.disableDiagnosticButton)).perform(click());

        onView(withId(R.id.disableDiagnosticButton)).check(matches(not(isDisplayed())));

    }

    public void testStartCalibrate() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        startCalibrate(2, 4);
    }

    private void startCalibrate(double value, int index) {

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onData(is(instanceOf(ResultRange.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(index).onChildView(withId(R.id.button))
                .check(matches(allOf(isDisplayed(), withText("?"))));

        onView(withText(String.format("%.2f ppm", value))).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        try {
            Thread.sleep(10000 + (Config.INITIAL_DELAY + 5000) * Config.SAMPLING_COUNT_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //onView(withId(R.id.okButton)).perform(click());

        onData(is(instanceOf(ResultRange.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(index).onChildView(withId(R.id.button))
                .check(matches(allOf(isDisplayed(), not(withBackgroundColor(Color.rgb(10, 10, 10))), withText(isEmpty()))));


        Espresso.pressBack();

        Espresso.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    public void testStartNoDilutionTest() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        resetLanguage();

        goToMainScreen();

        startCalibrate(2.0, 4);

        goToMainScreen();

        startCalibrate(0.5, 1);

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        startApp();

        resetLanguage();

        goToMainScreen();

        onView(withText(R.string.startSurvey)).perform(click());

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

        clickListViewItem("CREATE NEW DATA POINT");

        clickListViewItem("All Tests");

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.noDilutionButton)).check(matches(isDisplayed()));

        onView(withId(R.id.noDilutionButton)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        try {
            Thread.sleep(10000 + (Config.INITIAL_DELAY + 5000) * Config.SAMPLING_COUNT_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Result dialog
        takeScreenshot();

        onView(withId(R.id.endSurveyButton)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    public void testStartHighLevelTest() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        startApp();

        resetLanguage();

        goToMainScreen();

        startCalibrate(2.0, 4);

        goToMainScreen();

        onView(withText(R.string.startSurvey)).perform(click());

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

        clickListViewItem("CREATE NEW DATA POINT");

        clickListViewItem("All Tests");

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        mDevice.waitForWindowUpdate("", 2000);

        onView(withId(R.id.noDilutionButton)).check(matches(isDisplayed()));

        onView(withId(R.id.noDilutionButton)).perform(click());

        onView(allOf(withId(R.id.dilutionTextView), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.startButton)).perform(click());

        onView(allOf(withId(R.id.dilution1TextView), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        try {
            Thread.sleep(12000 + (Config.INITIAL_DELAY + 5000) * Config.SAMPLING_COUNT_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText(String.format(getActivity().getString(R.string.tryWithDilutedSample), 2)))
                .check(matches(isCompletelyDisplayed()));

        //High levels found dialog
        takeScreenshot();

        onView(withId(R.id.endSurveyButton)).perform(click());

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        mDevice.waitForWindowUpdate("", 2000);

        onView(withId(R.id.percentButton1)).check(matches(isDisplayed()));

        onView(withId(R.id.percentButton1)).perform(click());

        onView(allOf(withId(R.id.dilutionTextView), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        //Test Start Screen
        takeScreenshot();

        onView(withId(R.id.startButton)).perform(click());

        //Test Progress Screen
        takeScreenshot();

        onView(allOf(withId(R.id.dilution1TextView), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        try {
            Thread.sleep(12000 + (Config.INITIAL_DELAY + 5000) * Config.SAMPLING_COUNT_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText(String.format(getActivity().getString(R.string.tryWithDilutedSample), 5)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.endSurveyButton)).perform(click());

        try {

            mDevice.findObject(new UiSelector().text("Use External Source")).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        mDevice.waitForWindowUpdate("", 2000);

        onView(withId(R.id.percentButton2)).check(matches(isDisplayed()));

        onView(withId(R.id.percentButton2)).perform(click());

        onView(allOf(withId(R.id.dilutionTextView), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.startButton)).perform(click());

        onView(allOf(withId(R.id.dilution1TextView), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        try {
            Thread.sleep(12000 + (Config.INITIAL_DELAY + 5000) * Config.SAMPLING_COUNT_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("10.00")).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.endSurveyButton)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    private Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    java.util.Collection activities = ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED);
                    activity[0] = (Activity) Iterables.getOnlyElement(activities);
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return activity[0];
    }

//    private void takeSpoonScreenshot() {
//        if (mTakeScreenshots) {
//            Spoon.screenshot(getCurrentActivity(), "screen-" + mCounter++ + "-" + mCurrentLanguage);
//        }
//    }

    private void takeScreenshot() {
        if (mTakeScreenshots) {
            int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
            if (SDK_VERSION >= 17) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                File path = new File(Environment.getExternalStorageDirectory().getPath() +
                        "/org.akvo.caddisfly/screenshots/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png");
                mDevice.takeScreenshot(path, 0.5f, 60);
            }
        }
    }


    private void clickListViewItem(String name) {
        UiScrollable listView = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(100);
        listView.waitForExists(5000);
        UiObject listViewItem;
        try {
            listView.scrollTextIntoView(name);
            listViewItem = listView.getChildByText(new UiSelector()
                    .className(android.widget.TextView.class.getName()), "" + name + "");
            listViewItem.click();
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("\"" + name + "\" ListView item was clicked.");
    }
}