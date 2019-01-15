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
import android.os.Build;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;

import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.hamcrest.Matchers.allOf;

/**
 * Utility functions for automated testing
 */
public final class TestUtil {

    private TestUtil() {
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Timber.e(e);
        }
    }

    public static Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                activity[0] = (Activity) resumedActivities.iterator().next();
            }
        });
        return activity[0];
    }

    @SuppressWarnings("SameParameterValue")
    static void findButtonInScrollable(String name) {
        UiScrollable listView = new UiScrollable(new UiSelector().className(ScrollView.class.getName()));
        listView.setMaxSearchSwipes(10);
        listView.waitForExists(5000);
        try {
            listView.scrollTextIntoView(name);
        } catch (Exception ignored) {
        }
    }

    static boolean clickListViewItem(String name) {
        UiScrollable listView = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(4);
        listView.waitForExists(3000);
        UiObject listViewItem;
        try {
            if (listView.scrollTextIntoView(name)) {
                listViewItem = listView.getChildByText(new UiSelector()
                        .className(TextView.class.getName()), "" + name + "");
                listViewItem.click();
            } else {
                return false;
            }
        } catch (UiObjectNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.HOST.startsWith("SWDG2909")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static ViewAction clickPercent(final float pctX, final float pctY) {
        return new GeneralClickAction(
                Tap.SINGLE,
                view -> {

                    final int[] screenPos = new int[2];
                    view.getLocationOnScreen(screenPos);
                    int w = view.getWidth();
                    int h = view.getHeight();

                    float x = w * pctX;
                    float y = h * pctY;

                    final float screenX = screenPos[0] + x;
                    final float screenY = screenPos[1] + y;

                    return new float[]{screenX, screenY};
                },
                Press.FINGER);
    }

    private static void swipeLeft() {
        mDevice.waitForIdle();
        mDevice.swipe(500, 400, 50, 400, 4);
        mDevice.waitForIdle();
    }

    public static void swipeRight() {
        mDevice.waitForIdle();
        mDevice.swipe(50, 400, 500, 400, 4);
        mDevice.waitForIdle();
    }

    private static void swipeDown() {
        for (int i = 0; i < 3; i++) {
            mDevice.waitForIdle();
            mDevice.swipe(300, 400, 300, 750, 4);
        }
    }

    public static void swipeUp() {
        for (int i = 0; i < 3; i++) {
            mDevice.waitForIdle();
            mDevice.swipe(200, 750, 200, 600, 4);
        }
    }

    public static void goBack(int times) {
        for (int i = 0; i < times; i++) {
            mDevice.pressBack();
        }
    }

    public static void goBack() {
        mDevice.waitForIdle();
        goBack(1);
    }

    public static void nextPage(int times) {
        for (int i = 0; i < times; i++) {
            nextPage();
        }
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static void nextPage() {
        onView(allOf(withId(R.id.image_pageRight),
                isDisplayed())).perform(click());
        mDevice.waitForIdle();
    }

    public static void nextSurveyPage() {
        swipeLeft();
    }

    public static void nextSurveyPage(String tabName) {

        UiObject2 tab = mDevice.findObject(By.text(tabName));
        if (tab == null || !tab.isSelected()) {

            for (int i = 0; i < 12; i++) {
                swipeRight();
                tab = mDevice.findObject(By.text(tabName));
                if (tab != null && tab.isSelected()) {
                    break;
                }
                tab = mDevice.findObject(By.text("Fluoride"));
                if (tab != null && tab.isSelected()) {
                    for (int j = 0; j < 20; j++) {
                        mDevice.waitForIdle();
                        swipeLeft();
                        sleep(300);
                        tab = mDevice.findObject(By.text(tabName));
                        if (tab != null && tab.isSelected()) {
                            break;
                        }
                    }
                    break;
                }
            }
        }

        swipeDown();
        mDevice.waitForIdle();
    }
}
