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

package org.akvo.caddisfly.internal


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.sleep
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtInternal {

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE")


    private var scale: Float = 0.toFloat()

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    // third parameter is set to false which means the activity is not started automatically
    var mTestActivityRule = ActivityTestRule(TestActivity::class.java, false, false)

    @Before
    fun setUp() {
        scale = mActivityTestRule.activity.resources.displayMetrics.density
        mActivityTestRule.finishActivity()
    }

    @Test
    fun testCbtWithoutSurvey() {

        val testConfigRepository = TestConfigRepository()

        val testList = testConfigRepository.getTests(TestType.CBT)
        for (i in 0 until TestConstants.CBT_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.CBT)

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
                data.putString(SensorConstants.LANGUAGE, "en")
                intent.putExtras(data)

                mTestActivityRule.launchActivity(intent)

                navigateToCbtTest(id)

                navigateToCbtTest2(id)

            }
        }
    }

    private fun navigateToCbtTest(id: String): Int {

        sleep(1000)

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        var pages = 0
        for (i in 0..16) {
            pages++

            try {
                sleep(1000)

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

    private fun navigateToCbtTest2(id: String) {

        sleep(1000)

        onView(withText(R.string.submitResult)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        for (i in 0..16) {

            try {
                sleep(1000)

                if (i == 2) {
                    val customShapeButton = onView(Matchers.allOf<View>(withId(R.id.compartments),
                            isDisplayed()))

                    customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f))
                } else if (i == 5) {
                    val customShapeButton = onView(Matchers.allOf<View>(withId(R.id.compartments),
                            isDisplayed()))
                    customShapeButton.perform(TestUtil.clickPercent(0.3f, 0.5f))
                }

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {

                TestHelper.clickSubmitButton()

                sleep(300)
                break
            }
        }
    }
}
