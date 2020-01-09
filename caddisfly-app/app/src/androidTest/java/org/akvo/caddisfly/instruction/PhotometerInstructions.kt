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
import org.akvo.caddisfly.common.AppConfig.INSTRUMENTED_TEST_LANGUAGE
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class PhotometerInstructions : BaseTest() {

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
    // third parameter is set to false which means the activity is not started automatically
    var mActivityRule = ActivityTestRule(TestActivity::class.java, false, false)

    @Test
    @RequiresDevice
    fun testInstructionsMd610() {

        val testConfigRepository = TestConfigRepository()

        val testList = testConfigRepository.getTests(TestType.BLUETOOTH)
        for (i in 0 until TestConstants.MD610_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.BLUETOOTH)

            val uuid = testList[i].uuid

            val id = uuid.substring(uuid.lastIndexOf("-") + 1)

            //            if (("9991fb84dd90 606b771e0ffe 6060e4dbe59d").contains(id))
            //
            run {
                val intent = Intent()
                intent.type = "text/plain"
                intent.action = AppConfig.EXTERNAL_APP_ACTION
                val data = Bundle()
                data.putString(SensorConstants.RESOURCE_ID, uuid)
                data.putString(SensorConstants.LANGUAGE, INSTRUMENTED_TEST_LANGUAGE)
                intent.putExtras(data)

                mActivityRule.launchActivity(intent)

                val pages = navigateToBluetoothTest(id)

                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],")

                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                        .append("\')\">").append(testList[i].name).append("</span></li>")

                TestHelper.currentActivity.finish()
                mActivityRule.finishActivity()
            }

        }

        Log.d("Caddisfly", jsArrayString.toString())
        Log.d("Caddisfly", listString.toString())
    }

    private fun navigateToBluetoothTest(id: String): Int {

        sleep(1000)

        mDevice.waitForIdle()

        takeScreenshot(id, -1)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        mDevice.waitForIdle()

        takeScreenshot(id, 0)

        onView(withText(R.string.test_selected)).perform(click())

        var pages = 0
        for (i in 0..16) {
            pages++

            try {
                sleep(1000)

                takeScreenshot(id, i + 1)

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {
                TestHelper.navigateUp()
                sleep(300)
                Espresso.pressBack()
                sleep(300)
                break
            }

        }
        return pages + 1
    }
}