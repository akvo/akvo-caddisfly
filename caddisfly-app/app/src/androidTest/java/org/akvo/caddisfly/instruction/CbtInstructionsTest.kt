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


import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.isDeviceInitialized
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresExternalApp
@RunWith(AndroidJUnit4::class)
class CbtInstructionsTest : BaseTest() {

    private val jsArrayString = StringBuilder()
    private val listString = StringBuilder()
    private var scale: Float = 0.toFloat()

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (!isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    // third parameter is set to false which means the activity is not started automatically
    var mTestActivityRule = ActivityTestRule(TestActivity::class.java, false, false)

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE")

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mActivityTestRule)
        scale = mActivityTestRule.activity.resources.displayMetrics.density
        mActivityTestRule.finishActivity()
    }

    @Test
    fun cbt_Survey_All() {

        mDevice.pressBack()

        val testConfigRepository = TestConfigRepository()
        var buttonIndex = 0
        var tabName = "Coliforms"

        val testList = testConfigRepository.getTests(TestType.CBT)
        for (i in 0 until TestConstants.CBT_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.CBT)
            val uuid = testList[i].uuid
            val id = uuid.substring(uuid.lastIndexOf("-") + 1)

            //            if (("bf80d7197176, ac22c9afa0ab").contains(id))
            //
            run {
                when {
                    "ed4db0fd3386".contains(id) -> {
                        buttonIndex = 0
                        tabName = "Coliforms"
                    }
                    "4bfd645c26cf".contains(id) -> {
                        buttonIndex = 1
                        tabName = "Coliforms"
                    }
                    "bf80d7197176".contains(id) -> {
                        buttonIndex = 0
                        tabName = "Coliforms2"
                    }
                    "ac22c9afa0ab".contains(id) -> {
                        buttonIndex = 1
                        tabName = "Coliforms2"
                    }
                }

                gotoSurveyForm()

                nextSurveyPage(tabName)

                clickExternalSourceButton(buttonIndex)

                mDevice.waitForIdle()

                sleep(1000)

                val pages = navigateToCbtTest(id)

                sleep(2000)

                gotoSurveyForm()

                nextSurveyPage(tabName)

                clickExternalSourceButton(buttonIndex)

                mDevice.waitForIdle()

                sleep(1000)

                navigateToCbtTest2(id, pages)

                sleep(1000)
            }
        }

        Log.d("Caddisfly", jsArrayString.toString())
        Log.d("Caddisfly", listString.toString())
    }

    private fun navigateToCbtTest(id: String): Int {

        sleep(1000)

        mDevice.waitForIdle()

        takeScreenshot(id, -1)

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        mDevice.waitForIdle()

        var pages = 0
        for (i in 0..16) {
            pages++

            try {
                sleep(1000)

                takeScreenshot(id, i)

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {

                val appCompatTextView = onView(
                        allOf<View>(withText(R.string.read_instructions),
                                childAtPosition(
                                        childAtPosition(
                                                withClassName(`is`("android.widget.TableLayout")),
                                                7),
                                        2)))
                appCompatTextView.perform(scrollTo(), click())

                sleep(1000)

                takeScreenshot(id, i + 1)

                sleep(200)

                val appCompatButton4 = onView(
                        allOf<View>(withId(android.R.id.button1), withText("OK"),
                                childAtPosition(
                                        childAtPosition(
                                                withId(R.id.buttonPanel),
                                                0),
                                        3)))
                appCompatButton4.perform(scrollTo(), click())

                sleep(1000)

                mDevice.waitForIdle()

                TestHelper.clickCloseButton()

                sleep(300)
                break
            }

        }
        return pages + 1
    }

    private fun navigateToCbtTest2(id: String, startIndex: Int): Int {

        sleep(1000)

        mDevice.waitForIdle()

        takeScreenshot(id, startIndex)

        onView(withText(R.string.submitResult)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        mDevice.waitForIdle()

        var pages = 0
        for (i in 0..16) {
            pages++

            try {
                sleep(1000)

                if (i == 2) {
                    val customShapeButton = onView(allOf<View>(withId(R.id.compartments),
                            isDisplayed()))

                    customShapeButton.perform(clickPercent(0.1f, 0.5f))
                    customShapeButton.perform(clickPercent(0.5f, 0.5f))
                    customShapeButton.perform(clickPercent(0.9f, 0.1f))
                } else if (i == 5) {
                    val customShapeButton = onView(allOf<View>(withId(R.id.compartments),
                            isDisplayed()))
                    customShapeButton.perform(clickPercent(0.3f, 0.5f))
                }
                takeScreenshot(id, startIndex + i + 1)

                onView(withId(R.id.image_pageRight)).perform(click())

            } catch (e: Exception) {

                TestHelper.clickSubmitButton()

                sleep(1000)

                takeScreenshot(id, startIndex + i + 2)

                break
            }

        }
        return pages + 1
    }
}
