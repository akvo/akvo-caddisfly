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

package org.akvo.caddisfly.util

import android.os.Build
import android.os.SystemClock
import android.view.InputDevice
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.*
import org.akvo.caddisfly.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RequiresExternalApp

fun findButtonInScrollable(name: String) {
    val listView = UiScrollable(UiSelector().className(ScrollView::class.java.name))
    listView.maxSearchSwipes = 10
    listView.waitForExists(5000)
    try {
        listView.scrollTextIntoView(name)
    } catch (ignored: Exception) {
    }
}

fun sleep(time: Int) {
    SystemClock.sleep(time.toLong())
}

internal fun clickListViewItem(name: String): Boolean {
    val listView = UiScrollable(UiSelector())
    listView.maxSearchSwipes = 4
    listView.waitForExists(3000)
    val listViewItem: UiObject
    try {
        if (listView.scrollTextIntoView(name)) {
            listViewItem = listView.getChildByText(UiSelector()
                    .className(TextView::class.java.name), "" + name + "")
            listViewItem.click()
        } else {
            return false
        }
    } catch (e: UiObjectNotFoundException) {
        return false
    }

    return true
}

/**
 * Utility functions for automated testing
 */
object TestUtil {

    val isEmulator: Boolean
        get() = ((Build.ID.contains("KOT49H") && Build.MODEL.contains("MLA-AL10"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)

    fun clickPercent(pctX: Float, pctY: Float): ViewAction {
        return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)
                    val w = view.width
                    val h = view.height

                    val x = w * pctX
                    val y = h * pctY

                    val screenX = screenPos[0] + x
                    val screenY = screenPos[1] + y

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER, InputDevice.SOURCE_TOUCHSCREEN, 0)
    }

    fun swipeLeft() {
        mDevice.waitForIdle()
        mDevice.swipe(300, 400, 50, 400, 4)
        mDevice.waitForIdle()
    }

    fun swipeRight() {
        mDevice.waitForIdle()
        mDevice.swipe(50, 400, 500, 400, 4)
        mDevice.waitForIdle()
    }

    private fun swipeDown() {
        for (i in 0..2) {
            mDevice.waitForIdle()
            mDevice.swipe(300, 400, 300, 750, 4)
        }
    }

    fun nextPage(times: Int) {
        for (i in 0 until times) {
            nextPage()
        }
    }

    fun nextPage() {
        onView(allOf(withId(R.id.image_pageRight),
                isDisplayed())).perform(click())
        sleep(300)
        mDevice.waitForIdle()
        getInstrumentation().waitForIdleSync()
    }

    fun prevPage() {
        onView(allOf(withId(R.id.image_pageLeft),
                isDisplayed())).perform(click())
        getInstrumentation().waitForIdleSync()
    }

    fun prevPage(times: Int) {
        for (i in 0 until times) {
            mDevice.pressBack()
        }
    }

    fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position))
            }
        }
    }

    fun nextSurveyPage() {
        swipeLeft()
    }

    fun findObject(tabName: String): UiObject2? {
        var tab: UiObject2? = mDevice.findObject(By.text(tabName))
        if (tab == null) {
            tab = mDevice.findObject(By.text(tabName.toUpperCase()))
        }
        return tab
    }

    fun nextSurveyPage(tabName: String) {
        var tab: UiObject2? = findObject(tabName)
        if (tab == null || !tab.isSelected) {
            swipeLeft()
            if (tab == null || !tab.isSelected) {
                for (i in 0..11) {
                    swipeRight()
                    tab = findObject(tabName)
                    if (tab != null && tab.isSelected) {
                        break
                    }
                    tab = findObject("Striptest")
                    if (tab != null && tab.isSelected) {
                        for (j in 0..19) {
                            mDevice.waitForIdle()
                            swipeLeft()
                            sleep(200)
                            tab = findObject(tabName)
                            if (tab != null && tab.isSelected) {
                                break
                            }
                        }
                        break
                    }
                }
            }
        }

        swipeDown()
        mDevice.waitForIdle()
    }

    fun checkTextInTable(@StringRes resourceId: Int) {
        val textView3 = onView(
                allOf(withText(resourceId),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow::class.java),
                                        0),
                                1),
                        isDisplayed()))
        textView3.check(matches(withText(resourceId)))
    }
}
