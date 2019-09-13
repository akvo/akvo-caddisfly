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

package org.akvo.caddisfly.test

import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class SensorTest : BaseTest() {

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
    override fun setUp() {
        super.setUp()
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
    }

    @Test
    @RequiresDevice
    fun eCTest() {

        gotoSurveyForm()

        TestUtil.nextSurveyPage("Sensor")

        clickExternalSourceButton(1)

        SystemClock.sleep(6000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Soil - Moisture"))).check(matches(isDisplayed()))

        onView(withText("Next")).perform(click())

        onView(allOf<View>(withId(R.id.textTitle), withText("Soil - Moisture"))).check(matches(isDisplayed()))

        if (TestUtil.isEmulator || AppConfig.SENSOR_NOT_CONNECTED) {
            return
        }

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()))

        var message = "The expected sensor was not found.\n\n" + "Connect the Soil - Electrical Conductivity sensor."

        onView(withText(message)).check(matches(isDisplayed()))

        onView(withId(android.R.id.button2)).perform(click())

        val relativeLayout6 = onView(
                allOf<View>(childAtPosition(
                        withId(R.id.list_types),
                        1),
                        isDisplayed()))
        relativeLayout6.perform(click())

        SystemClock.sleep(7000)

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()))

        message = "The expected sensor was not found.\n\n" + "Connect the Soil - Moisture sensor."

        onView(withText(message)).check(matches(isDisplayed()))

        onView(withId(android.R.id.button2)).perform(click())

        val relativeLayout2 = onView(
                allOf<View>(childAtPosition(
                        withId(R.id.list_types),
                        2),
                        isDisplayed()))
        relativeLayout2.perform(click())

        SystemClock.sleep(7000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val appCompatImageButton2 = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText(R.string.selectTest))).check(matches(isDisplayed()))

        val relativeLayout1 = onView(
                allOf<View>(childAtPosition(
                        withId(R.id.list_types),
                        0),
                        isDisplayed()))

        relativeLayout1.perform(click())

        SystemClock.sleep(7000)

        onView(withText(R.string.incorrectDevice)).check(matches(isDisplayed()))

        message = "The expected sensor was not found.\n\n" + "Connect the Soil - Electrical Conductivity sensor."

        onView(withText(message)).check(matches(isDisplayed()))

        onView(withId(android.R.id.button2)).perform(click())

    }

    @Test
    @RequiresDevice
    fun testSensorFromSurvey() {

        gotoSurveyForm()

        TestUtil.nextSurveyPage("Sensor")

        clickExternalSourceButton(2)

        SystemClock.sleep(1000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()))

        onView(withText("Next")).perform(click())

        onView(allOf<View>(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()))

        if (TestUtil.isEmulator || AppConfig.SENSOR_NOT_CONNECTED) {

            mDevice.pressBack()

            return
        }

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor!"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()))

        onView(withId(R.id.buttonSubmitResult)).perform(click()).check(matches(isDisplayed()))

    }

    @Test
    @RequiresDevice
    fun surveyQuestions() {

        goToMainScreen()

        gotoSurveyForm()

        TestUtil.nextSurveyPage("Sensor")

        clickExternalSourceButton(2)

        onView(withText("Next")).perform(click())

        SystemClock.sleep(12000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()))

        if (TestUtil.isEmulator || AppConfig.SENSOR_NOT_CONNECTED) {
            return
        }

        onView(allOf<View>(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()))

        onView(withId(R.id.buttonSubmitResult)).perform(click())

        clickExternalSourceButton(0)

        SystemClock.sleep(12000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textTitle), withText("Soil - Electrical Conductivity"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit), withText("μS/cm"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit2), withText("°Celsius"))).check(matches(isDisplayed()))

        onView(withId(R.id.buttonSubmitResult)).perform(click())

        clickExternalSourceButton(1)

        SystemClock.sleep(12000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textTitle), withText("Soil - Moisture"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textSubtitle), withText("Sensor connected"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textUnit), withText("% VWC"))).check(matches(isDisplayed()))

        onView(withId(R.id.textUnit2)).check(matches(not<View>(isDisplayed())))

        onView(withId(R.id.buttonSubmitResult)).perform(click())

    }

    @Test
    @RequiresDevice
    fun testEC() {

        gotoSurveyForm()

        TestUtil.nextSurveyPage("Sensor")

        clickExternalSourceButton(2)

        onView(withText("Next")).perform(click())

        SystemClock.sleep(12000)

        onView(allOf<View>(withId(R.id.textToolbarTitle), withText("Sensor"))).check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.textTitle), withText("Water - Electrical Conductivity"))).check(matches(isDisplayed()))

        mDevice.waitForWindowUpdate("", 2000)

        onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()))

        mDevice.waitForWindowUpdate("", 2000)

        onView(withContentDescription(R.string.deviceConnectSensor)).check(matches(isDisplayed()))

        Espresso.pressBack()

    }
}