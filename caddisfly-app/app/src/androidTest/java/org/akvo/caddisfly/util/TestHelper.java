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

package org.akvo.caddisfly.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import androidx.annotation.StringRes;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.TestConstants;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.runner.lifecycle.Stage.RESUMED;
import static org.akvo.caddisfly.util.TestUtil.clickListViewItem;
import static org.akvo.caddisfly.util.TestUtil.findButtonInScrollable;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;

public final class TestHelper {

    public static final String mCurrentLanguage = "en";
    private static final boolean TAKE_SCREENSHOTS = true;
    private static final Map<String, String> STRING_HASH_MAP_EN = new HashMap<>();
    private static final Map<String, String> STRING_HASH_MAP_ES = new HashMap<>();
    private static final Map<String, String> STRING_HASH_MAP_FR = new HashMap<>();
    private static final Map<String, String> STRING_HASH_MAP_IN = new HashMap<>();
    public static Map<String, String> currentHashMap;
    public static UiDevice mDevice;

    private static int mCounter;

    private TestHelper() {
    }

    private static void addString(String key, String... values) {
        STRING_HASH_MAP_EN.put(key, values[0]);
        if (values.length > 1) {
            STRING_HASH_MAP_ES.put(key, values[1]);
            STRING_HASH_MAP_FR.put(key, values[2]);
            STRING_HASH_MAP_IN.put(key, values[3]);
        } else {
            STRING_HASH_MAP_ES.put(key, values[0]);
            STRING_HASH_MAP_FR.put(key, values[0]);
            STRING_HASH_MAP_IN.put(key, values[0]);
        }
    }

    public static String getString(@StringRes int resourceId) {
        return getString(getCurrentActivity(), resourceId);
    }

    public static String getString(Activity activity, @StringRes int resourceId) {
        Resources currentResources = activity.getResources();
        AssetManager assets = currentResources.getAssets();
        DisplayMetrics metrics = currentResources.getDisplayMetrics();
        Configuration config = new Configuration(currentResources.getConfiguration());
        config.locale = new Locale(mCurrentLanguage);
        Resources res = new Resources(assets, metrics, config);

        return res.getString(resourceId);
    }

    public static void loadData(Activity activity, String languageCode) {

        STRING_HASH_MAP_EN.clear();
        STRING_HASH_MAP_ES.clear();
        STRING_HASH_MAP_FR.clear();
        STRING_HASH_MAP_IN.clear();

        Resources currentResources = activity.getResources();
        AssetManager assets = currentResources.getAssets();
        DisplayMetrics metrics = currentResources.getDisplayMetrics();
        Configuration config = new Configuration(currentResources.getConfiguration());
        config.locale = new Locale(languageCode);
        Resources res = new Resources(assets, metrics, config);

        addString(TestConstant.LANGUAGE, "English", "Español", "Français", "Bahasa Indonesia");
//        addString("otherLanguage", "Français", "English");
        addString(TestConstant.FLUORIDE, res.getString(R.string.fluoride));
        addString("chlorine", res.getString(R.string.freeChlorine));
        addString("survey", res.getString(R.string.survey));
        addString("sensors", res.getString(R.string.sensors));
        addString("electricalConductivity", res.getString(R.string.electricalConductivity));
        addString("unnamedDataPoint", res.getString(R.string.unnamedDataPoint));
        addString("createNewDataPoint", res.getString(R.string.addDataPoint));
        addString(TestConstant.USE_EXTERNAL_SOURCE, res.getString(R.string.useExternalSource));
        addString(TestConstant.GO_TO_TEST, res.getString(R.string.goToTest));
        addString("next", res.getString(R.string.next));

        // Restore device-specific locale
        new Resources(assets, metrics, currentResources.getConfiguration());

        switch (languageCode) {
            case "en":
                currentHashMap = STRING_HASH_MAP_EN;
                break;
            case "es":
                currentHashMap = STRING_HASH_MAP_ES;
                break;
            case "in":
                currentHashMap = STRING_HASH_MAP_IN;
                break;
            default:
                currentHashMap = STRING_HASH_MAP_FR;
                break;
        }
    }

    public static void takeScreenshot() {
        if (TAKE_SCREENSHOTS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            File path = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/Akvo Caddisfly/screenshots/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png");
            mDevice.takeScreenshot(path, 0.5f, 60);
        }
    }

    public static void takeScreenshot(String name, int page) {
        if (TAKE_SCREENSHOTS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            File path;
            path = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/Akvo Caddisfly/screenshots/" + name + "-" + mCurrentLanguage + "-" +
                    String.format("%02d", page + 1) + ".png");
            mDevice.takeScreenshot(path, 0.1f, 30);
        }
    }

    public static void goToMainScreen() {

        boolean found = false;
        while (!found) {
            try {
                onView(withId(R.id.button_info)).check(matches(isDisplayed()));
                found = true;
            } catch (NoMatchingViewException e) {
                Espresso.pressBack();
            }
        }
    }

    public static void activateTestMode() {

        if (!AppConfig.IS_TEST_MODE) {

            onView(withId(R.id.button_info)).perform(click());

            onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

            String version = CaddisflyApp.getAppVersion(false);

            onView(withText(version)).check(matches(isDisplayed()));

            enterDiagnosticMode();

            onView(withId(R.id.actionSettings)).perform(click());

            clickListViewItem("Test Mode");
        }

        goToMainScreen();
    }

    public static void clickExternalSourceButton(int index) {

        String buttonText = currentHashMap.get(TestConstant.GO_TO_TEST);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            assert buttonText != null;
            buttonText = buttonText.toUpperCase();
        }

        findButtonInScrollable(buttonText);

        List<UiObject2> buttons = mDevice.findObjects(By.text(buttonText));

        if (index < buttons.size()) {
            buttons.get(index).click();
        } else {
            UiScrollable listView = new UiScrollable(new UiSelector());
            try {
                listView.scrollToEnd(1);
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }

            List<UiObject2> buttons1 = mDevice.findObjects(By.text(buttonText));
            buttons1.get(buttons1.size() - 1).click();
        }

        // New Android OS seems to popup a button for external app
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            sleep(1000);
            mDevice.findObject(By.text("Akvo Caddisfly")).click();
            sleep(1000);
        }

        mDevice.waitForWindowUpdate("", 2000);

        sleep(4000);
    }

    public static void clickExternalSourceButton(String text) {
        try {

            String buttonText = currentHashMap.get(text);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                assert buttonText != null;
                buttonText = buttonText.toUpperCase();
            }

            findButtonInScrollable(buttonText);

            assert buttonText != null;
            mDevice.findObject(new UiSelector().text(buttonText)).click();

            // New Android OS seems to popup a button for external app
            if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                    && (text.equals(TestConstant.USE_EXTERNAL_SOURCE)
                    || text.equals(TestConstant.GO_TO_TEST))) {
                sleep(1000);
                mDevice.findObject(By.text("Akvo Caddisfly")).click();
                sleep(1000);
            }

            mDevice.waitForWindowUpdate("", 2000);

        } catch (UiObjectNotFoundException e) {
            Timber.e(e);
        }
    }

    public static void gotoSurveyForm() {

        Context context = getInstrumentation().getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(TestConstants.FLOW_SURVEY_PACKAGE_NAME);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        context.startActivity(intent);

        if (!clickListViewItem(currentHashMap.get("unnamedDataPoint"))) {

            UiObject addButton = mDevice.findObject(new UiSelector()
                    .resourceId("org.akvo.flow:id/add_data_point_fab"));

            try {
                if (addButton.exists() && addButton.isEnabled()) {
                    addButton.click();
                }
            } catch (UiObjectNotFoundException e) {
                Timber.e(e);
            }
        }

        // mDevice.findObject(By.text("Caddisfly Tests")).click();
    }

    public static void enterDiagnosticMode() {
        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.textVersion)).perform(click());
        }
    }

    public static void leaveDiagnosticMode() {
        onView(withId(R.id.fabDisableDiagnostics)).perform(click());
    }

//    public static void resetLanguage() {
//
//        goToMainScreen();
//
//        onView(withId(R.id.actionSettings)).perform(click());
//
//        onView(withText(R.string.language)).perform(click());
//
//        onData(Matchers.hasToString(Matchers.startsWith(currentHashMap.get("language")))).perform(click());
//
//        mDevice.waitForIdle();
//
//        sleep(5000);
//
//        mDevice.waitForIdle();
//
//        Espresso.pressBack();
//
//        onView(withId(R.id.actionSettings)).perform(click());
//
//        onView(withText(R.string.language)).perform(click());
//
//        onData(Matchers.hasToString(Matchers.startsWith(currentHashMap.get("language")))).perform(click());
//
//        mDevice.waitForIdle();
//
//        sleep(5000);
//
//        mDevice.waitForIdle();
//
//        goToMainScreen();
//
//    }

    public static void clearPreferences(ActivityTestRule activityTestRule) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(activityTestRule.getActivity());
        prefs.edit().clear().apply();
    }

    public static void navigateUp() {
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        appCompatImageButton.perform(click());
    }

    public static Activity getCurrentActivity() {
        final Activity[] currentActivity = {null};
        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                currentActivity[0] = (Activity) resumedActivities.iterator().next();
            }
        });
        return currentActivity[0];
    }

    public static boolean isLowMemoryDevice(String model) {
        return model.contains("ASUS_Z007");
    }
}
