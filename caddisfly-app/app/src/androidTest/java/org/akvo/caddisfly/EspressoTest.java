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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;

import com.squareup.spoon.Spoon;

import org.akvo.caddisfly.ui.MainActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.DecimalFormatSymbols;
import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.object.HasToString.hasToString;

public class EspressoTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    static final String mCurrentLanguage = "en";
    static boolean mTakeScreenshots = true;
    static int mCounter = 0;

    HashMap<String, String> stringHashMapEN = new HashMap<>();
    HashMap<String, String> stringHashMapFR = new HashMap<>();
    HashMap<String, String> currentHashMap;

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

    public static Matcher<String> isEmpty() {
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

        getActivity();
    }

    @SuppressWarnings("EmptyMethod")
    public void test000() {
    }

    public void test001_Language() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }

    public void test002_About() {

        final Button button = (Button) getActivity().findViewById(R.id.surveyButton);
        assertNotNull(button);

        takeScreenshot();

        onView(withId(R.id.action_settings))
                .perform(click());

        takeScreenshot();

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        takeScreenshot();

        Espresso.pressBack();

    }

    public void testCalibrateSwatches() {
        calibrateSwatches(false);
    }

    private void calibrateSwatches(boolean devMode) {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.calibrate))
                .perform(click());

        if (devMode) {
            onView(withId(R.id.action_swatches))
                    .perform(click());

            Espresso.pressBack();

            onView(withId(R.id.action_swatches)).check(matches(isDisplayed()));
        }

        Espresso.pressBack();

        onView(withText(R.string.calibrate)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.action_settings)).check(matches(isDisplayed()));
    }

    public void testChangeTestType() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrate)).perform(click());

        takeScreenshot();

        onView(withText(currentHashMap.get("fluoride"))).perform(click());

        takeScreenshot();

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        onView(withText("0" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());

        takeScreenshot();

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(currentHashMap.get("chlorine"))).perform(click());

        onView(withText("0" + dfs.getDecimalSeparator() + "50 ppm")).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

//        onView(withText("Nitrite")).perform(click());
//
//        onView(withText("3.00 ppm")).perform(click());
//
//        Espresso.pressBack();
//
//        Espresso.pressBack();
//
//        onView(withText("pH")).perform(click());
//
//        onView(withText("9.00 pH")).perform(click());
//
//        onView(withId(R.id.startButton)).perform(click());

        //onView(withText(R.string.selectDilution)).check(matches(isDisplayed()));

        //onView(withId(android.R.id.button2)).perform(click());

//        Espresso.pressBack();
//
//        Espresso.pressBack();

        Espresso.pressBack();
    }

    public void testStartASurvey() {

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

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.hundredPercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.fiftyPercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.twentyFivePercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.hundredPercentButton)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withId(R.id.placeInStandText)).check(matches(isDisplayed()));

        try {
            Espresso.pressBack();
        } catch (NoActivityResumedException ignored) {
        }
    }

    public void testCalibrateSensor() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrateSummary)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrate)).perform(click());

        onView(withText(currentHashMap.get("electricalConductivity"))).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withText(R.string.notConnected)).check(matches(isDisplayed()));
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

        onView(withText(R.string.updateCheck)).perform(click());

        onView(withText(R.string.dataConnection)).check(matches(isDisplayed()));
        onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));

        //onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

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

//    public void testLanguage() {
//
//        onView(withId(R.id.action_settings))
//                .perform(click());
//
//        onView(withText(R.string.language))
//                .check(matches(not(isDisplayed())))
//                .perform(scrollTo())
//                .check(matches(isDisplayed()))
//                .perform(scrollTo());
//
//        onView(withText(R.string.language))
//                .perform(click());
//
//        onData(hasToString(startsWith("العربية"))).perform(click());
//    }
//
//    public void testLanguage2() {
//        onView(withId(R.id.action_settings))
//                .perform(click());
//
//        onView(withText(R.string.language))
//                .check(matches(not(isDisplayed())))
//                .perform(scrollTo())
//                .check(matches(isDisplayed()))
//                .perform(scrollTo());
//
//        onView(withText(R.string.language))
//                .perform(click());
//
//        onData(hasToString(startsWith("हिंदी"))).perform(click());
//    }
//
//    public void testLanguage3() {
//        onView(withId(R.id.action_settings))
//                .perform(click());
//
//        onView(withText(R.string.language))
//                .check(matches(not(isDisplayed())))
//                .perform(scrollTo())
//                .check(matches(isDisplayed()))
//                .perform(scrollTo());
//
//        onView(withText(R.string.language))
//                .perform(click());
//
//        onData(hasToString(startsWith("ಕನ್ನಡ"))).perform(click());
//    }

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

    public void testLanguageReset() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith(currentHashMap.get("language")))).perform(click());
    }


    public void testLeaveDiagnosticMode() {
        onView(withId(R.id.disableDiagnosticButton)).perform(click());

        onView(withId(R.id.disableDiagnosticButton)).check(matches(not(isDisplayed())));

    }

//    public void testStartCalibrate() {
//        startCalibrate(false);
//    }

//    public void startCalibrate(boolean devMode) {
//
//        onView(withId(R.id.action_settings)).perform(click());
//
//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(this.getInstrumentation().getTargetContext());
//        prefs.edit().clear().apply();
//
//        onView(withText(R.string.calibrate)).perform(click());
//
//        onView(withText(currentHashMap.get("fluoride"))).perform(click());
//
//        onData(is(instanceOf(ResultRange.class)))
//                .inAdapterView(withId(android.R.id.list))
//                .atPosition(4).onChildView(withId(R.id.button))
//                .check(matches(allOf(isDisplayed(), withText("?"))));
//
//        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
//        onView(withText("2" + dfs.getDecimalSeparator() + "00 ppm")).perform(click());
//
//        onView(withId(R.id.startButton)).perform(click());
//
//        try {
//            Thread.sleep(70000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        if (devMode) {
//            onView(withId(R.id.okButton)).perform(click());
//        }
//
//        onData(is(instanceOf(ResultRange.class)))
//                .inAdapterView(withId(android.R.id.list))
//                .atPosition(4).onChildView(withId(R.id.button))
//                .check(matches(allOf(isDisplayed(), not(withBackgroundColor(Color.rgb(10, 10, 10))), withText(isEmpty()))));
//
//
//        Espresso.pressBack();
//
//        Espresso.pressBack();
//
////        onView(withId(android.R.id.list)).check(matches(withChildCount(is(greaterThan(0)))));
////        onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));
//
//    }
//

    private Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    java.util.Collection activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
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
            //int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
            //if (SDK_VERSION >= 17) {
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            File path = new File(Environment.getExternalStorageDirectory().getPath() +
//                    "/org.akvo.caddisfly/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png");

            Spoon.screenshot(getCurrentActivity(), "screen-" + mCounter++ + "-" + mCurrentLanguage);
            //}
        }
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