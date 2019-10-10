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
import androidx.test.espresso.Espresso.*
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
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.DrawableMatcher.Companion.hasDrawable
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class SensorInstructions : BaseTest() {
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

    @Before
    override fun setUp() {
        super.setUp()
        mActivityTestRule.launchActivity(Intent())
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
        mActivityTestRule.finishActivity()
    }

    @Test
    @RequiresDevice
    fun testInstructionsAllSensors() {

        mActivityTestRule.launchActivity(Intent())
        loadData(mActivityTestRule.activity, mCurrentLanguage)

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.SENSOR)

        for (i in 0 until TestConstants.SENSOR_TESTS_COUNT) {

            val testInfo = testList!![i]
            assertEquals(testInfo.subtype, TestType.SENSOR)

            var id = testInfo.uuid
            id = id.substring(id.lastIndexOf("-") + 1)

            navigateToTest(i, id)

            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()))

            onView(withText(testInfo.name)).check(matches(isDisplayed()))

            mDevice.pressBack()
        }
        mActivityTestRule.finishActivity()
    }

    private fun navigateToTest(index: Int, id: String) {

        gotoSurveyForm()

        TestUtil.nextSurveyPage("Sensor")

        clickExternalSourceButton(index)

        mDevice.waitForIdle()

        sleep(1000)

        takeScreenshot(id, -1)

        mDevice.waitForIdle()
    }

    @Test
    @RequiresDevice
    fun testInstructionsAllManualColorSelect() {

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.MANUAL_COLOR_SELECT)

        assertEquals(testList!![0].subtype, TestType.MANUAL_COLOR_SELECT)

        val uuid = testList[0].uuid
        val id = uuid.substring(uuid.lastIndexOf("-") + 1)

        val intent = Intent()
        intent.type = "text/plain"
        intent.action = AppConfig.EXTERNAL_APP_ACTION
        val data = Bundle()
        data.putString(SensorConstants.RESOURCE_ID, uuid)
        data.putString(SensorConstants.LANGUAGE, mCurrentLanguage)
        intent.putExtras(data)

        mActivityTestRule.launchActivity(intent)

        navigateToTest(id)

        for (j in 0..3) {
            pressBackUnconditionally()
        }

        mActivityTestRule.finishActivity()
    }

    @Test
    @RequiresDevice
    fun testInstructionsAllManualColorSelect2() {

        val testConfigRepository = TestConfigRepository()
        val testList = testConfigRepository.getTests(TestType.MANUAL_COLOR_SELECT)

        assertEquals(testList!![1].subtype, TestType.MANUAL_COLOR_SELECT)

        val uuid = testList[1].uuid
        val id = uuid.substring(uuid.lastIndexOf("-") + 1)

        val intent = Intent()
        intent.type = "text/plain"
        intent.action = AppConfig.EXTERNAL_APP_ACTION
        val data = Bundle()
        data.putString(SensorConstants.RESOURCE_ID, uuid)
        data.putString(SensorConstants.LANGUAGE, mCurrentLanguage)
        intent.putExtras(data)

        mActivityTestRule.launchActivity(intent)

        navigateToTest(id)

        for (j in 0..3) {
            pressBackUnconditionally()
        }

        mActivityTestRule.finishActivity()
    }

    private fun navigateToTest(id: String) {

        mDevice.waitForIdle()

        sleep(1000)

        takeScreenshot(id, -1)

        mDevice.waitForIdle()

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        var pages = 0
        for (i in 0..16) {
            try {
                takeScreenshot(id, pages)

                pages++

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {
                sleep(600)
                pressBack()
                break
            }

        }
    }
}
