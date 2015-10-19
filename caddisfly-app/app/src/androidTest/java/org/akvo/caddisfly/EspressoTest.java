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

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
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

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidConfig;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.ui.TypeListActivity;
import org.akvo.caddisfly.util.FileUtil;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
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

    private static final boolean mTakeScreenshots = false;
    private static String mCurrentLanguage = "en";
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

        addString("language", "English", "Français");
        addString("otherLanguage", "Français", "English");
        addString("fluoride", "Fluoride", "Fluorure");
        addString("chlorine", "Free Chlorine", "Chlore libre");
        addString("survey", "Survey", "Survey");
        addString("electricalConductivity", "Electrical Conductivity", "Conductivité Electrique");
        addString("unnamedDataPoint", "Unnamed data point", "Donnée non nommée");
        addString("createNewDataPoint", "Add Data Point", "CRÉER UN NOUVEAU POINT");
        addString("useExternalSource", "Use External Source", "Utiliser source externe");
        addString("next", "Next", "Suivant");

        mCurrentLanguage = "en";
        changeLanguage(mCurrentLanguage);

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null, "FLUOR", "ppm",
                CaddisflyApp.TestType.COLORIMETRIC_LIQUID, true, new String[]{}, new String[]{}, true));

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
        prefs.edit().clear().apply();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        getActivity();

        resetLanguage();

        goToMainScreen();

    }

    private void addString(String key, String englishText, String frenchText) {
        stringHashMapEN.put(key, englishText);
        stringHashMapFR.put(key, frenchText);
    }

    private void changeLanguage(String languageCode) {
        mCurrentLanguage = languageCode;
        if (languageCode.equals("en")) {
            currentHashMap = stringHashMapEN;
        } else {
            currentHashMap = stringHashMapFR;
        }
    }

    @SuppressWarnings("EmptyMethod")
    public void test000() {

    }

    public void test001_Language() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        leaveDiagnosticMode();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }

    public void test002_Screenshots() {

        saveInvalidCalibration();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Akvo Caddisfly/screenshots";

        File folder = new File(path);
        if (!folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        mDevice.waitForWindowUpdate("", 2000);

        goToMainScreen();

        sleep(4000);

        //Main Screen
        takeScreenshot();

        onView(withId(R.id.actionSettings)).perform(click());

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

//        if (!NetworkUtil.checkInternetConnection(getActivity(), false)) {
//            onView(withText(R.string.updateCheck)).perform(click());
//
//            onView(withText(R.string.noInternetConnection)).check(matches(isDisplayed()));
//            onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));
//            mDevice.waitForWindowUpdate("", 1000);
//
//            //Enable Internet Dialog
//            takeScreenshot();
//
//            Espresso.pressBack();
//
//        }

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        sleep(4000);

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onView(withText("TestInvalid")).perform(click());

        sleep(2000);

        onView(withText(String.format("%s. %s", getActivity().getString(R.string.calibrationIsInvalid),
                getActivity().getString(R.string.tryRecalibrating)))).check(matches(isDisplayed()));

        leaveDiagnosticMode();

        sleep(4000);

        onView(withText(R.string.calibrate)).perform(click());

        //Test Types Screen
        takeScreenshot();

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        //Calibrate Swatches Screen
        takeScreenshot();

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        saveCalibration();

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

        onView(withText("TestValid")).perform(click());

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
            String message = String.format("%s\r\n\r\n%s", getActivity().getString(R.string.phoneDoesNotSupport),
                    getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            //Feature not supported
            takeScreenshot();

            onView(withText(R.string.ok)).perform(click());
        }

        goToMainScreen();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        //Dilution dialog
        takeScreenshot();

        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();
        mDevice.pressBack();

        startApp();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("next");

        clickExternalSourceButton("useExternalSource");

        //Calibration incomplete
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        clickExternalSourceButton("next");

        //EC
        clickExternalSourceButton("useExternalSource");

        //Connect EC Sensor Screen
        takeScreenshot();

        mDevice.pressBack();

        clickExternalSourceButton("next");

        //Temperature
        clickExternalSourceButton("useExternalSource");

        mDevice.pressBack();

        clickExternalSourceButton("next");

        //pH
        clickExternalSourceButton("useExternalSource");

        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        onView(withText(R.string.ok)).perform(click());

        clickExternalSourceButton("next");

        //Caffeine
        clickExternalSourceButton("useExternalSource");

        //Test type not available
        takeScreenshot();

        onView(withId(android.R.id.button1)).perform(click());

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

        TextView button = (TextView) getCurrentActivity().findViewById(R.id.buttonCalibrate);
        while (button == null) {
            Espresso.pressBack();
            mDevice.waitForWindowUpdate("", 2000);
            button = (TextView) getCurrentActivity().findViewById(R.id.buttonCalibrate);
        }
    }

    public void testEC() {

        goToMainScreen();

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.electricalConductivity)).perform(click());

        onView(withText(R.string.electricalConductivity)).check(matches(isDisplayed()));

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

        mDevice.waitForWindowUpdate("", 2000);

        onView(withContentDescription(getActivity()
                .getString(R.string.deviceConnectSensor))).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(R.string.electricalConductivity)).perform(click());

        try {
            onView(withText(R.string.warning)).check(matches(isDisplayed()));

            onView(withText(R.string.calibrate)).perform(click());

            onView(withId(R.id.buttonStartCalibrate)).perform(click());

            onView(withText(R.string.sensorNotFound)).check(matches(isDisplayed()));

            onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception ex) {
            String message = String.format("%s\r\n\r\n%s", getActivity().getString(R.string.phoneDoesNotSupport),
                    getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withText(R.string.ok)).perform(click());
        }
    }

    public void testSwatches() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.actionSwatches)).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionSwatches)).check(matches(isDisplayed()));

        Espresso.pressBack();
    }

    public void testIncompleteCalibration() {

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("next");

        clickExternalSourceButton("useExternalSource");

        mDevice.waitForWindowUpdate("", 2000);

        onView(withText(R.string.cannotStartTest)).check(matches(isDisplayed()));

        String message = getActivity().getString(R.string.errorCalibrationIncomplete,
                currentHashMap.get("chlorine"));
        message = String.format("%s\r\n\r\n%s", message,
                getActivity().getString(R.string.doYouWantToCalibrate));

        onView(withText(message)).check(matches(isDisplayed()));

        onView(withText(R.string.cancel)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

    }

    public void testChangeTestType() {
        //onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("chlorine"))).perform(click());

        onView(withText("0" + dfs.getDecimalSeparator() + "50 ppm")).perform(click());

    }

    public void testStartASurvey() {

        saveCalibration();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onView(withText("TestValid")).perform(click());

        sleep(2000);

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

    private void openSurveyInFlow() {
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
            String appName = "Akvo FLOW";
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

        gotoSurveyForm();
    }

    private void gotoSurveyForm() {
        //clickListViewItem("Automated Tests");
        //onView(withText("Automated Tests")).perform(click());
        if (!clickListViewItem(currentHashMap.get("unnamedDataPoint"))) {
            //openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

            // click on 'Add Note' button
            UiObject addButton = mDevice.findObject(new UiSelector()
                    .descriptionContains(currentHashMap.get("createNewDataPoint")));

            try {
                if (addButton.exists() && addButton.isEnabled()) {
                    addButton.click();
                }
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }

            // onView(withContentDescription(currentHashMap.get("createNewDataPoint"))).perform(click());
            //clickListViewItem(currentHashMap.get("createNewDataPoint"));
        }
        //clickListViewItem("All Tests");
    }

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
            onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception ex) {
            String message = String.format("%s\r\n\r\n%s", getActivity().getString(R.string.phoneDoesNotSupport),
                    getActivity().getString(R.string.pleaseContactSupport));

            onView(withText(message)).check(matches(isDisplayed()));

            onView(withText(R.string.ok)).perform(click());
        }

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

    }

//    public void testCheckUpdate() {
//
//        onView(withId(R.id.actionSettings)).perform(click());
//
//        onView(withText(R.string.updateCheck)).check(matches(isDisplayed()));
//
//        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));
//
//        if (!NetworkUtil.checkInternetConnection(getActivity(), false)) {
//
//            onView(withText(R.string.updateCheck)).perform(click());
//
//            onView(withText(R.string.noInternetConnection)).check(matches(isDisplayed()));
//            onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));
//
//            Espresso.pressBack();
//
//        }
//
//        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));
//
//        Espresso.pressBack();
//
//    }

    public void testDiagnosticMode() {

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }

        goToMainScreen();

        onView(withId(R.id.fabDisableDiagnostics)).check(matches(isDisplayed()));

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.actionSwatches)).perform(click());

    }

    public void testLanguageFrench() {
        languageTest("fr");
    }

    public void testLanguageEnglish() {
        languageTest("en");
    }

    private void languageTest(String language) {
        onView(withId(R.id.actionSettings)).perform(click());

        changeLanguage(language);

        try {
            onView(withText(currentHashMap.get("language"))).perform(click());
            onData(hasToString(startsWith(currentHashMap.get("otherLanguage")))).perform(click());
            onView(withId(R.id.actionSettings)).perform(click());
        } catch (Exception ignored) {
        }

        onView(withText(R.string.language)).perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());

        onView(withText(currentHashMap.get("survey"))).perform(click());

        mDevice.pressBack();

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        mDevice.pressBack();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();

        onView(withText(currentHashMap.get("language"))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());

        onView(withText(currentHashMap.get("language"))).perform(click());
        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }

    private void resetLanguage() {
        onView(withId(R.id.actionSettings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }

    private void enterDiagnosticMode() {

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }

        sleep(1000);
    }

    private void leaveDiagnosticMode() {

        goToMainScreen();

        onView(withId(R.id.fabDisableDiagnostics)).perform(click());

    }

    public void testStartCalibrate() {

        startCalibrate(2, 4);
    }

    private void startCalibrate(double value, int index) {

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onData(is(instanceOf(Swatch.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(index).onChildView(withId(R.id.textSwatch))
                .check(matches(allOf(isDisplayed(), withText("?"))));

        onView(withText(String.format("%.2f ppm", value))).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        sleep(16000 + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        //onView(withId(R.id.okButton)).perform(click());

        onData(is(instanceOf(Swatch.class)))
                .inAdapterView(withId(android.R.id.list))
                .atPosition(index).onChildView(withId(R.id.textSwatch))
                .check(matches(allOf(isDisplayed(), not(withBackgroundColor(Color.rgb(10, 10, 10))), withText(isEmpty()))));


        Espresso.pressBack();

        Espresso.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    public void disableTestStartNoDilutionTest() {

        saveLowLevelCalibration();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onView(withText("LowLevelTest")).perform(click());

        sleep(2000);

        leaveDiagnosticMode();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(2000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(withId(R.id.buttonStart)).perform(click());

        sleep(16000 + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

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

    public void disableTestStartHighLevelTest() {

        saveHighLevelCalibration();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        String version = CaddisflyApp.getAppVersion(getActivity());

        onView(withText(version)).check(matches(isDisplayed()));

        enterDiagnosticMode();

        goToMainScreen();

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        onView(withId(R.id.menuLoad)).perform(click());

        sleep(2000);

        onView(withText("HighLevelTest")).perform(click());

        sleep(2000);

        leaveDiagnosticMode();

        onView(withId(R.id.buttonSurvey)).perform(click());

        gotoSurveyForm();

        clickExternalSourceButton("useExternalSource");

        sleep(2000);

        onView(withId(R.id.buttonNoDilution)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonNoDilution)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution2), withText(R.string.noDilution)))
                .check(matches(isCompletelyDisplayed()));

        sleep(16000 + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) *
                ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(String.format(getActivity().getString(R.string.tryWithDilutedSample), 2)))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonDilution1)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution1)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        //Test Start Screen
        takeScreenshot();

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution2), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 2)))).check(matches(isCompletelyDisplayed()));

        sleep(16000 + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        onView(withText(String.format(getActivity().getString(R.string.tryWithDilutedSample), 5)))
                .check(matches(isCompletelyDisplayed()));

        //High levels found dialog
        takeScreenshot();

        onView(withId(R.id.buttonOk)).perform(click());

        clickExternalSourceButton("useExternalSource");

        onView(withId(R.id.buttonDilution2)).check(matches(isDisplayed()));

        onView(withId(R.id.buttonDilution2)).perform(click());

        onView(allOf(withId(R.id.textDilution), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonStart)).perform(click());

        onView(allOf(withId(R.id.textDilution2), withText(String.format(getActivity()
                .getString(R.string.timesDilution), 5)))).check(matches(isCompletelyDisplayed()));

        //Test Progress Screen
        takeScreenshot();

        sleep(16000 + (ColorimetryLiquidConfig.DELAY_BETWEEN_SAMPLING + 5000) * ColorimetryLiquidConfig.SAMPLING_COUNT_DEFAULT);

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("10" + dfs.getDecimalSeparator() + "00")).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.buttonOk)).perform(click());

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

        mDevice.pressBack();

//        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
//        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

    }

    private void clickExternalSourceButton(String buttonText) {
        try {

            mDevice.findObject(new UiSelector().text(currentHashMap.get(buttonText))).click();

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void testRestartAppDuringAnalysis() {

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        sleep(500);

        onView(withId(R.id.buttonStart)).perform(click());

        mDevice.pressHome();

        try {
            mDevice.pressRecentApps();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        sleep(3000);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.click(mDevice.getDisplayWidth() / 2, (mDevice.getDisplayHeight() / 2) + 300);

        mDevice.waitForWindowUpdate("", 2000);

        clickListViewItem("Automated Tests");

    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testZErrors() {

        mCounter += 4;

//        getActivity().runOnUiThread(new Runnable() {
//            public void run() {
//                try {
//                    Method method = MainActivity.class.getDeclaredMethod("alertCameraFlashNotAvailable");
//                    method.setAccessible(true);
//                    method.invoke(getActivity());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    assertEquals(e.getMessage(), 0, 1);
//                }
//            }
//        });
//
//        //No flash
//        takeScreenshot();
//
//        onView(withId(android.R.id.button1)).perform(click());

        startApp();

        getActivity();

        onView(withText(R.string.calibrate)).perform(click());

        final Activity typeListActivity = getCurrentActivity();
        typeListActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Method method = TypeListActivity.class.getDeclaredMethod("alertFeatureNotSupported");
                    method.setAccessible(true);
                    method.invoke(typeListActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                    assertEquals(e.getMessage(), 0, 1);
                }
            }
        });

        //Error loading config
        takeScreenshot();

        onView(withId(android.R.id.button2)).perform(click());

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        final Activity activity = getCurrentActivity();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Method method = ColorimetryLiquidActivity.class.getDeclaredMethod("alertCouldNotLoadConfig");
                    method.setAccessible(true);
                    method.invoke(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                    assertEquals(e.getMessage(), 0, 1);
                }
            }
        });

        //Error loading config
        takeScreenshot();

        onView(withId(android.R.id.button1)).perform(click());

        goToMainScreen();

    }

    private void saveCalibration() {

        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode());

        FileUtil.saveToFile(path,
                "TestValid", "0.0=255  88  177\n"
                        + "0.5=255  110  15\n"
                        + "1.0=255  139  137\n"
                        + "1.5=253  174  74\n"
                        + "2.0=244  180  86\n"
                        + "2.5=236  172  81\n"
                        + "3.0=254  169  61\n");
    }

    private void saveInvalidCalibration() {
        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode());

        FileUtil.saveToFile(path,
                "TestInvalid", "0.0=255  88  177\n"
                        + "0.5=255  110  15\n"
                        + "1.0=255  138  137\n"
                        + "1.5=253  174  74\n"
                        + "2.0=253  174  76\n"
                        + "2.5=236  172  81\n"
                        + "3.0=254  169  61\n");
    }

    private void saveHighLevelCalibration() {
        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode());

        FileUtil.saveToFile(path,
                "HighLevelTest", "0.0=255  88  47\n"
                        + "0.5=255  60  37\n"
                        + "1.0=255  35  27\n"
                        + "1.5=253  17  17\n"
                        + "2.0=254  0  0\n");
    }

    private void saveLowLevelCalibration() {
        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                CaddisflyApp.getApp().getCurrentTestInfo().getCode());

        FileUtil.saveToFile(path,
                "LowLevelTest", "0.0=255  60  37\n"
                        + "0.5=255  35  27\n"
                        + "1.0=253  17  17\n"
                        + "1.5=254  0  0\n"
                        + "2.0=224  0  0\n");
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

    private void takeScreenshot() {
        if (mTakeScreenshots) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sleep(500);

                File path = new File(Environment.getExternalStorageDirectory().getPath() +
                        "/Akvo Caddisfly/screenshots/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png");
                mDevice.takeScreenshot(path, 0.5f, 60);
            }
        }
    }

    private boolean clickListViewItem(String name) {
        UiScrollable listView = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(10);
        listView.waitForExists(5000);
        UiObject listViewItem;
        try {
            if (listView.scrollTextIntoView(name)) {
                listViewItem = listView.getChildByText(new UiSelector()
                        .className(android.widget.TextView.class.getName()), "" + name + "");
                listViewItem.click();
            } else {
                return false;
            }
        } catch (UiObjectNotFoundException e) {
            return false;
        }

        System.out.println("\"" + name + "\" ListView item was clicked.");
        return true;
    }
}