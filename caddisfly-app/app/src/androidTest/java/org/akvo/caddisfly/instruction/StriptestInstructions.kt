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

package org.akvo.caddisfly.instruction


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.currentActivity
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.isPatchAvailable
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.max

@LargeTest
@RunWith(AndroidJUnit4::class)
class StriptestInstructions {

    private val jsArrayString = StringBuilder()
    private val listString = StringBuilder()

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
    var mActivityTestRule = ActivityTestRule(TestActivity::class.java, false, false)

    @Rule
    @JvmField
    var mMainActivityTestRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Before
    fun setUp() {
        mMainActivityTestRule.launchActivity(Intent())
        loadData(mMainActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mMainActivityTestRule)

        activateTestMode()

        mMainActivityTestRule.finishActivity()
    }

    @Test
    fun instructionsTest() {

        gotoSurveyForm()

        nextSurveyPage("Striptest")

        sleep(500)

        clickExternalSourceButton(2)

        mDevice.waitForIdle()

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(7000)

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.collect_water_sample)).check(matches(isDisplayed()))

        onView(withText("Water - pH")).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.dip_strip_in_water_2_seconds)).check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withId(R.id.viewPager)).perform(swipeLeft())

        mDevice.waitForIdle()

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_strip_clr)).check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withText(R.string.collect_water_sample))
                .check(matches(isDisplayed()))

        onView(withText(R.string.skip)).perform(click())

        sleep(1500)

        TestHelper.clickStartButton()

        sleep(7000)

        if (isPatchAvailable()) {
            onView(withText("4.8")).check(matches(isDisplayed()))
            pressBack()
            onView(withText("pH")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()

        sleep(1000)

        if (isPatchAvailable()) {
            assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")))
        } else {
            assertNotNull(mDevice.findObject(By.text("pH: null ")))
        }
    }

    @Test
    fun testInstructionsAllStripTests() {

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.STRIP_TEST)

        var resultWaitDelay: Int

        for (i in 0 until TestConstants.STRIP_TESTS_COUNT) {

            val testInfo = testList!![i]

            assertEquals(testInfo.subtype, TestType.STRIP_TEST)

            val uuid = testInfo.uuid
            val id = uuid.substring(uuid.lastIndexOf("-") + 1)

            if (("aa4a4e3100c9").contains(id)) {
                //            if (testInfo.getName().contains("Soil"))
                //                if (("ac33b44f9992 71e4c7cd2280 ac3b4d9c9599 fe26af2621a7 4c5cbcf6b1c1").contains(id))
                //
                run {

                    if (("6843158b47b4 6ed8142b6b07, 420551851acd 1b7db640037c d555f04db952 aa4a4e3100c9 " + "411a4093f6b6 321bbbd9876b 798b81d2b019 32d9b8f4aecf").contains(id)) {

                        resultWaitDelay = 0
                        for (result in testInfo.results) {
                            if (result.timeDelay > resultWaitDelay) {
                                resultWaitDelay = result.timeDelay!!
                            }
                        }

                        resultWaitDelay = max(5000, (resultWaitDelay + 5) * 1000)
                    } else {
                        resultWaitDelay = 5000
                    }

                    Log.e("Caddisfly Log", "id: $id")

                    val intent = Intent()
                    intent.type = "text/plain"
                    intent.action = AppConfig.EXTERNAL_APP_ACTION
                    val data = Bundle()
                    data.putString(SensorConstants.RESOURCE_ID, uuid)
                    data.putString(SensorConstants.LANGUAGE, mCurrentLanguage)
                    intent.putExtras(data)

                    mDevice.waitForIdle()

                    mActivityTestRule.finishActivity()

                    mActivityTestRule.launchActivity(intent)

                    navigateToTest(id)

                    var pages = navigateInstructions(id, 1)

                    TestHelper.clickStartButton()

                    if ("411a4093f6b6".contains(id)) {

                        sleep(2000)

                        takeScreenshot(id, pages)

                        sleep(resultWaitDelay)

                        pages = navigateInstructions(id, ++pages)

                        sleep(1000)

                        TestHelper.clickStartButton()
                    }

                    sleep(1000)

                    takeScreenshot(id, pages)

                    sleep(resultWaitDelay)

                    takeScreenshot(id, ++pages)

                    jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],")

                    listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                            .append("\')\">").append(testList[i].name).append("</span></li>")

                    currentActivity.finish()
                    mActivityTestRule.finishActivity()
                }
            }
        }


        Log.d("Caddisfly", jsArrayString.toString())
        Log.d("Caddisfly", listString.toString())

    }

    private fun navigateToTest(id: String) {

        mDevice.waitForIdle()

        sleep(1000)

        takeScreenshot(id, -1)

        mDevice.waitForIdle()

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(2000)

        takeScreenshot(id, 0)
    }

    private fun navigateInstructions(id: String, startIndex: Int): Int {
        var pages = startIndex
        sleep(3000)

        for (i in 0..16) {
            try {
                takeScreenshot(id, pages)

                pages++

                if ("ac33b44f9992, 32d9b8f4aecf".contains(id)) {
                    try {

                        if (pages == 5) {

                            @Suppress("ConstantConditionIf")
                            if (mCurrentLanguage == "en") {
                                mDevice.click(350, 390)
                            } else {
                                mDevice.click(450, 600)
                            }
                            sleep(3000)
                            takeScreenshot(id, pages)
                            pages++
                            sleep(600)
                            mDevice.pressBack()
                        }
                    } catch (ignore: Exception) {
                    }
                }

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {
                sleep(600)
                break
            }

        }
        return pages
    }
}
