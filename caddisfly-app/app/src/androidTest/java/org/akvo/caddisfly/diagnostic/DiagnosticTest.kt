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

package org.akvo.caddisfly.diagnostic

import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.navigateUp
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mActivityRule)
    }

    @Test
    fun app_DiagnosticMode() {

        onView(withId(R.id.button_info)).perform(click())

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click())

        enterDiagnosticMode()

        onView(withId(R.id.actionSettings)).perform(click())

        pressBack()

        onView(withId(R.id.fabDisableDiagnostics)).check(matches(isDisplayed()))

        sleep(500)

        goToMainScreen()

        sleep(3000)

        onView(withText(R.string.next)).perform(click())

        sleep(1500)

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        onView(withId(R.id.button_info)).perform(click())

        leaveDiagnosticMode()

        sleep(1000)

        navigateUp()

        sleep(3000)

        onView(withText(R.string.next)).perform(click())

        getInstrumentation().waitForIdleSync()

        sleep(3000)

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        onView(withId(R.id.button_info)).perform(click())

        sleep(500)

        enterDiagnosticMode()

        onView(withId(R.id.actionSettings)).perform(click())

        @Suppress("ConstantConditionIf")
        if (BuildConfig.TEST_LANGUAGE == "en") {
            onView(withText("English")).perform(click())
            onView(withText("Français")).perform(click())
        } else {
            onView(withText("Français")).perform(click())
            onView(withText("Annuler")).perform(click())
        }

        Espresso.pressBackUnconditionally()

        sleep(1000)

        mActivityRule.finishActivity()

        getInstrumentation().waitForIdleSync()

        mActivityRule.launchActivity(Intent())

        getInstrumentation().waitForIdleSync()

        sleep(1000)

        onView(withId(R.id.button_info)).perform(click())

        sleep(500)

        pressBack()

        onView(withId(R.id.button_info)).perform(click())

        onView(withText("L'information légale")).check(matches(isDisplayed())).perform(click())

        navigateUp()

        pressBack()

        sleep(2000)

        getInstrumentation().waitForIdleSync()

        onView(withText("Suivant")).perform(click())

        sleep(1500)

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        onView(withId(R.id.button_info)).perform(click())

        onView(withId(R.id.actionSettings)).perform(click())

        onView(withText("Français")).perform(click())

        onView(withText("Español")).perform(click())

        Espresso.pressBackUnconditionally()

        sleep(1000)

        mActivityRule.finishActivity()

        getInstrumentation().waitForIdleSync()

        mActivityRule.launchActivity(Intent())

        getInstrumentation().waitForIdleSync()

        sleep(1000)

        onView(withId(R.id.button_info)).perform(click())

        onView(withText("Información legal")).check(matches(isDisplayed())).perform(click())

        navigateUp()

        pressBack()

        sleep(2000)

        getInstrumentation().waitForIdleSync()

        onView(withText("Siguiente")).perform(click())

        sleep(1500)

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        onView(withId(R.id.button_info)).perform(click())

        onView(withId(R.id.actionSettings)).perform(click())

        onView(withText("Español")).perform(click())

        onView(withText("English")).perform(click())

        Espresso.pressBackUnconditionally()

        sleep(1000)

        mActivityRule.finishActivity()

        getInstrumentation().waitForIdleSync()

        mActivityRule.launchActivity(Intent())

        getInstrumentation().waitForIdleSync()

        sleep(1000)

        onView(withId(R.id.button_info)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.actionSettings)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.disable_diagnostics)).perform(click())

        clearPreferences(mActivityRule)

        getInstrumentation().waitForIdleSync()

        mActivityRule.finishActivity()

        mDevice.pressBack()

        mDevice.pressBack()

    }
}