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
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.hamcrest.Matcher;

import java.util.Collection;

import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

/**
 * Utility functions for automated testing
 */
public final class TestUtil {

    private TestUtil() {
    }

    public static String getText(final Matcher<View> matcher) {
        final String[] stringHolder = {null};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView) view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }

//    private static Matcher<View> withBackgroundColor(final int color) {
//        Checks.checkNotNull(color);
//        return new BoundedMatcher<View, Button>(Button.class) {
//            @Override
//            public boolean matchesSafely(Button button) {
//                int buttonColor = ((ColorDrawable) button.getBackground()).getColor();
//                return Color.red(color) == Color.red(buttonColor) &&
//                        Color.green(color) == Color.green(buttonColor) &&
//                        Color.blue(color) == Color.blue(buttonColor);
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("with background color: " + color);
//            }
//        };
//    }
//
//    private static Matcher<String> isEmpty() {
//        return new TypeSafeMatcher<String>() {
//            @Override
//            public boolean matchesSafely(String target) {
//                return target.length() == 0;
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("is empty");
//            }
//        };
//    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Timber.e(e);
        }
    }

    public static Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    activity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });
        return activity[0];
    }

    @SuppressWarnings("SameParameterValue")
    public static void findButtonInScrollable(String name) {
        UiScrollable listView = new UiScrollable(new UiSelector().className(ScrollView.class.getName()));
        listView.setMaxSearchSwipes(10);
        listView.waitForExists(5000);
        try {
            listView.scrollTextIntoView(name);
        } catch (Exception ignored) {
        }
    }

    public static boolean clickListViewItem(String name) {
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
}
