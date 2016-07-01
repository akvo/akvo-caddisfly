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

import android.os.Build;
import android.os.Environment;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.widget.TextView;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.util.FileUtil;
import org.hamcrest.Matchers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.akvo.caddisfly.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.TestUtil.findButtonInScrollable;
import static org.akvo.caddisfly.TestUtil.sleep;

class TestHelper {

    private static final HashMap<String, String> stringHashMapEN = new HashMap<>();
    private static final HashMap<String, String> stringHashMapFR = new HashMap<>();
    private static final HashMap<String, String> calibrationHashMap = new HashMap<>();
    private static final boolean mTakeScreenshots = false;
    public static HashMap<String, String> currentHashMap;
    public static UiDevice mDevice;
    public static String mCurrentLanguage = "en";
    private static int mCounter;

    private static void addString(String key, String englishText, String frenchText) {
        stringHashMapEN.put(key, englishText);
        stringHashMapFR.put(key, frenchText);
    }

    private static void addCalibration(String key, String colors) {
        calibrationHashMap.put(key, colors);
    }

    public static void loadData(String languageCode) {
        mCurrentLanguage = languageCode;

        stringHashMapEN.clear();
        stringHashMapFR.clear();
        calibrationHashMap.clear();

        addString("language", "English", "Français");
        addString("otherLanguage", "Français", "English");
        addString("fluoride", "Fluoride", "Fluorure");
        addString("chlorine", "Free Chlorine", "Chlore libre");
        addString("survey", "Survey", "Enquête");
        addString("electricalConductivity", "Electrical Conductivity", "Conductivité Electrique");
        addString("unnamedDataPoint", "Unnamed data point", "Donnée non nommée");
        addString("createNewDataPoint", "Add Data Point", "CRÉER UN NOUVEAU POINT");
        addString("useExternalSource", "Go to test", "Go to test");
        addString("next", "Next", "Suivant");

        addCalibration("TestValid", "0.0=255  38  186\n"
                + "0.5=255  51  129\n"
                + "1.0=255  59  89\n"
                + "1.5=255  62  55\n"
                + "2.0=255  81  34\n");

        addCalibration("TestInvalid", "0.0=255  88  177\n"
                + "0.5=255  110  15\n"
                + "1.0=255  138  137\n"
                + "1.5=253  174  74\n"
                + "2.0=253  174  76\n"
                + "2.5=236  172  81\n"
                + "3.0=254  169  61\n");

        addCalibration("OutOfSequence", "0.0=255  38  186\n"
                + "0.5=255  51  129\n"
                + "1.0=255  62  55\n"
                + "1.5=255  59  89\n"
                + "2.0=255  81  34\n");

        addCalibration("HighLevelTest", "0.0=255  38  180\n"
                + "0.5=255  51  129\n"
                + "1.0=255  53  110\n"
                + "1.5=255  55  100\n"
                + "2.0=255  59  89\n");

        addCalibration("TestInvalid2", "0.0=255  88  47\n"
                + "0.5=255  60  37\n"
                + "1.0=255  35  27\n"
                + "1.5=253  17  17\n"
                + "2.0=254  0  0\n");

        addCalibration("LowLevelTest", "0.0=255  60  37\n"
                + "0.5=255  35  27\n"
                + "1.0=253  17  17\n"
                + "1.5=254  0  0\n"
                + "2.0=224  0  0\n");

        if (languageCode.equals("en")) {
            currentHashMap = stringHashMapEN;
        } else {
            currentHashMap = stringHashMapFR;
        }
    }

    public static void takeScreenshot() {
        if (mTakeScreenshots) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                File path = new File(Environment.getExternalStorageDirectory().getPath() +
                        "/Akvo Caddisfly/screenshots/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png");
                mDevice.takeScreenshot(path, 0.5f, 60);
            }
        }
    }

    public static void goToMainScreen() {

        boolean found = false;
        while (!found) {
            try {
                onView(withId(R.id.buttonCalibrate)).check(matches(isDisplayed()));
                found = true;
            } catch (NoMatchingViewException e) {
                Espresso.pressBack();
            }
        }
    }


//    public static void clickViewInScrollView(String buttonText) {
//        try {
//
//            UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));
//            appViews.setAsVerticalList();
//            appViews.flingToEnd(3);
//            appViews.getChildByText(
//                    new UiSelector().className(android.widget.Button.class.getName()), buttonText).click();
//
//            mDevice.waitForWindowUpdate("", 2000);
//
//        } catch (UiObjectNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public static void clickExternalSourceButton(String buttonText, int index) {

        findButtonInScrollable(buttonText);

        List<UiObject2> buttons = mDevice.findObjects(By.text(currentHashMap.get(buttonText)));
        buttons.get(buttons.size() - 1 - index).click();

        //mDevice.findObject(new UiSelector().text(currentHashMap.get(buttonText))).click();

        mDevice.waitForWindowUpdate("", 2000);

        sleep(4000);
    }

//    public static void clickExternalSourceButton(String buttonText) {
//        try {
//
//            findButtonInScrollable(buttonText);
//
//            mDevice.findObject(new UiSelector().text(currentHashMap.get(buttonText))).click();
//
//            mDevice.waitForWindowUpdate("", 2000);
//
//        } catch (UiObjectNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public static void startApp() {
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

    public static void saveCalibration(String name) {
        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, "FLUOR");

        FileUtil.saveToFile(path, name, calibrationHashMap.get(name));
    }

    public static void gotoSurveyForm() {
        if (!clickListViewItem(currentHashMap.get("unnamedDataPoint"))) {

            UiObject addButton = mDevice.findObject(new UiSelector()
                    .descriptionContains(currentHashMap.get("createNewDataPoint")));

            try {
                if (addButton.exists() && addButton.isEnabled()) {
                    addButton.click();
                }
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        }

        mDevice.findObject(By.text("caddisfly test")).click();
    }

    public static void enterDiagnosticMode() {
        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }
    }

    public static void leaveDiagnosticMode() {
        goToMainScreen();
        onView(withId(R.id.fabDisableDiagnostics)).perform(click());
    }

    public static void resetLanguage() {

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(Matchers.hasToString(Matchers.startsWith(currentHashMap.get("language")))).perform(click());

        mDevice.waitForIdle();

        goToMainScreen();

        onView(withId(R.id.actionSettings)).perform(click());

        onView(withText(R.string.language)).perform(click());

        onData(Matchers.hasToString(Matchers.startsWith(currentHashMap.get("language")))).perform(click());

        mDevice.waitForIdle();

        goToMainScreen();

    }
}