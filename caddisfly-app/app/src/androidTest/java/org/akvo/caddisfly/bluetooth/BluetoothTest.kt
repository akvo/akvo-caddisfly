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

package org.akvo.caddisfly.bluetooth


import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.navigateUp
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class BluetoothTest : BaseTest() {

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
    var mRuntimePermissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION)!!

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
    }

    @Test
    @RequiresDevice
    fun testResultData() {

        activateTestMode()

        mDevice.waitForWindowUpdate("", 2000)

        gotoSurveyForm()

        sleep(1000)

        TestUtil.nextSurveyPage("MD610")

        sleep(1500)

        clickExternalSourceButton(1)

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.BLUETOOTH)
        for (i in 0 until TestConstants.MD610_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.BLUETOOTH)

            var id = testList[i].uuid

            id = id.substring(id.lastIndexOf("-") + 1)

            if (id.equals("e14626afa5b0", ignoreCase = true)) {
                onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

                sleep(1000)

                onView(withText(R.string.test_selected)).perform(click())

                onView(withText(R.string.skip)).perform(click())

                sleep(10000)

                onView(withText(R.string.result)).check(matches(isDisplayed()))

                onView(withText(">500")).check(matches(isDisplayed()))

                onView(withText("mg/l")).check(matches(isDisplayed()))

                onView(withId(R.id.button_submit_result)).check(matches(isDisplayed()))

                onView(withId(R.id.button_submit_result)).perform(click())

                break
            }
        }
        mActivityTestRule.finishActivity()

        sleep(3000)

        assertNotNull(mDevice.findObject(By.text("2. Calcium Hardness")))

        assertNotNull(mDevice.findObject(By.text("Calcium Hardness: >500 mg/l")))
    }

    @Test
    @RequiresDevice
    fun bluetoothTest() {

        activateTestMode()

        mDevice.waitForWindowUpdate("", 2000)

        gotoSurveyForm()

        TestUtil.nextSurveyPage("MD610")

        clickExternalSourceButton(0)

        onView(withText(R.string.next)).perform(click())

        @Suppress("ConstantConditionIf")
        if (!AppConfig.SKIP_BLUETOOTH_SCAN) {
            if (TestUtil.isEmulator) {
                onView(withText("Bluetooth not supported."))
                        .inRoot(withDecorView(not<View>(`is`<View>(mActivityTestRule.activity.window
                                .decorView)))).check(matches(isDisplayed()))
                return
            }

            try {
                Thread.sleep(7000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            val appCompatButton3 = onView(
                    allOf<View>(withId(android.R.id.button1), withText(R.string.retry),
                            childAtPosition(
                                    allOf<View>(withClassName(`is`("com.android.internal.widget.ButtonBarLayout")),
                                            childAtPosition(
                                                    withClassName(`is`("android.widget.LinearLayout")),
                                                    3)),
                                    3),
                            isDisplayed()))
            appCompatButton3.perform(click())

            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            onView(allOf<View>(withId(R.id.button_connect), withText("Connect"))).perform(click())

            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }

        onView(withText(R.string.test_selected)).perform(click())

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero_a))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        var phrase = getString(R.string.add_reagent_1_liquid_exact_ml)
        phrase = phrase.replace("%reagent1", "2 ml SPADNS (467481)")
        onView(withText(phrase)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.wipe_place_align_vial_wait_2_a))
                .check(matches(isDisplayed()))

        TestUtil.swipeRight()

        onView(withText("Close the vial tightly with the cap and swirl several times to mix the contents"))
                .check(matches(isDisplayed()))

        TestUtil.goBack(4)

        navigateUp()

        onView(withText(R.string.next)).perform(click())

        onView(withText("Select Test")).check(matches(isDisplayed()))

        onView(withText("Press and hold (*shift*) + (*1*)(*7*)(*0*) to select Fluoride test and then press (*enter*)"))
                .check(matches(isDisplayed()))

        onView(withText(R.string.test_selected)).perform(click())

        onView(withText(R.string.fill_vial_24_with_10ml_sample_zero_a))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.remove_vial))
                .check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).perform(click())

        onView(withText(R.string.skip)).perform(click())

        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val textView7 = onView(
                allOf<View>(withId(R.id.textName1), withText("Fluoride"),
                        childAtPosition(
                                allOf<View>(withId(R.id.layoutResult1),
                                        childAtPosition(
                                                withId(R.id.layoutResult),
                                                0)),
                                0),
                        isDisplayed()))
        textView7.check(matches(withText("Fluoride")))

        onView(withText(">2")).check(matches(isDisplayed()))

        onView(withText("mg/l")).check(matches(isDisplayed()))

        onView(withId(R.id.button_submit_result)).perform(click())

        mActivityTestRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("1. Fluoride")))

        assertNotNull(mDevice.findObject(By.text("Fluoride: >2 mg/l")))

    }
}
