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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConstants.EXTERNAL_APP_ACTION
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.getString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.hamcrest.core.IsInstanceOf
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import java.util.regex.Pattern

@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }

        @JvmStatic
        @BeforeClass
        fun initialize() {
            BuildConfig.TEST_RUNNING.set(true)
        }
    }

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA")

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
        TestHelper.loadData(mActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        scale = mActivityTestRule.activity.resources.displayMetrics.density
        TestHelper.clearPreferences(mActivityTestRule)
        mActivityTestRule.finishActivity()
    }

    @Test
    fun cbt_Test() {
        runCbtTest(Constants.CBT_ID, 0)
    }

    @Test
    fun cbt_DilutionTest() {
        runCbtTest(Constants.CBT_ID_2, 1)
    }

    @Test
    fun cbt_TC_Test() {
        runCbtTotalColiformTest(Constants.CBT_ID_3, 0)
    }

    @Test
    fun cbt_TC_DilutionTest() {
        runCbtTotalColiformTest(Constants.CBT_ID_4, 1)
    }

    private fun runCbtTest(id: String, buttonIndex: Int) {

        val intent = Intent()
        if (skipOpeningExternalApp()) {
            intent.type = "text/plain"
            intent.action = EXTERNAL_APP_ACTION
            val data = Bundle()
            data.putString(SensorConstants.RESOURCE_ID, id)
            data.putString(SensorConstants.LANGUAGE, BuildConfig.TEST_LANGUAGE)
            intent.putExtras(data)
            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.gotoSurveyForm()
            TestUtil.nextSurveyPage("Coliforms")
            TestHelper.clickExternalSourceButton(buttonIndex)
        }

        val textView = onView(
                Matchers.allOf(withText("www.aquagenx.com"),
                        TestUtil.childAtPosition(
                                TestUtil.childAtPosition(
                                        IsInstanceOf.instanceOf(LinearLayout::class.java),
                                        0),
                                1),
                        isDisplayed()))
        textView.check(matches(withText("www.aquagenx.com")))

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed()))

        onView(withText("Water - E.coli")).check(matches(isDisplayed()))

        onView(withText(R.string.submitResult)).check(matches(isDisplayed()))

        onView(withText(R.string.prepare_sample)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        TestUtil.checkTextInTable(R.string.prepare_area_put_on_gloves)

        if (id == Constants.CBT_ID_2) {
            TestUtil.nextPage()
        }

        TestUtil.checkTextInTable(R.string.open_growth_medium)

        onView(withContentDescription("1")).check(matches(DrawableMatcher.hasDrawable()))

        val imageView = onView(
                Matchers.allOf(withContentDescription("1"),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        onView(Matchers.allOf(withId(R.id.pager_indicator),
                TestUtil.childAtPosition(
                        Matchers.allOf(withId(R.id.layout_footer),
                                TestUtil.childAtPosition(
                                        IsInstanceOf.instanceOf(RelativeLayout::class.java),
                                        1)),
                        0),
                isDisplayed()))

        TestUtil.nextPage()

        onView(withContentDescription("2")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.dissolve_medium_in_sample_a)

        TestUtil.checkTextInTable(R.string.dissolve_medium_in_sample_b)

        TestUtil.nextPage()

        onView(withContentDescription("4")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.label_compartment_bag)

        TestUtil.nextPage(3)

        getInstrumentation().waitForIdleSync()

        TestUtil.checkTextInTable(R.string.let_incubate)

        onView(withText(R.string.read_instructions)).perform(click())

        onView(withText(R.string.below25Degrees)).check(matches(isDisplayed()))

        onView(withText(R.string.incubate_in_portable)).check(matches(isDisplayed()))

        val appCompatButton3 = onView(
                Matchers.allOf(withId(android.R.id.button1), withText("OK"),
                        TestUtil.childAtPosition(
                                TestUtil.childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        appCompatButton3.perform(ViewActions.scrollTo(), click())

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        mDevice.waitForIdle()

        TestHelper.clickCloseButton()

        mDevice.waitForIdle()

        if (skipOpeningExternalApp()) {
            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.clickExternalSourceButton(buttonIndex)
        }

        onView(withId(R.id.button_phase_2)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()))

        takePhoto()

        onView(withContentDescription("8")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.change_colors_to_match)

        TestUtil.checkTextInTable(R.string.note_blue_green_specks)

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.click_compartments_to_change)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        val riskText = getString(R.string.low_risk_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lowRisk = riskText[0].trim { it <= ' ' }
        val safe = riskText[1].trim { it <= ' ' }

        onView(withText(lowRisk)).check(matches(isDisplayed()))
        onView(withText(safe)).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(withText("0")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText("0")))
        onView(withId(R.id.textResult2)).check(matches(not(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        mDevice.pressBack()

        sleep(500)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        val customShapeButton = onView(Matchers.allOf(withId(R.id.compartments),
                isDisplayed()))

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f))
        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f))
        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f))

        TestUtil.nextPage()

        getInstrumentation().waitForIdleSync()

        val riskText2 = getString(R.string.intermediate_possibly_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val intermediateRisk = riskText2[0].trim { it <= ' ' }
        val possiblySafe = riskText2[1].trim { it <= ' ' }

        if (id == Constants.CBT_ID_2) {
            onView(withText(lowRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("56")))
            onView(withId(R.id.textResult2)).check(matches(withText("56")))
        } else {
            onView(withText(intermediateRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("5.6")))
            onView(withId(R.id.textResult2)).check(matches(withText("5.6")))
        }

        onView(withText(possiblySafe)).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(not(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        TestUtil.prevPage()

        sleep(500)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        val customShapeButton1 = onView(Matchers
                .allOf(withId(R.id.compartments), isDisplayed()))

        customShapeButton1.perform(TestUtil.clickPercent(0.1f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.3f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.5f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.7f, 0.1f))
        customShapeButton1.perform(TestUtil.clickPercent(0.9f, 0.1f))
        customShapeButton1.perform(TestUtil.clickPercent(0.1f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.5f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.9f, 0.1f))

        TestUtil.nextPage()

        val riskText3 = getString(R.string.very_high_risk_unsafe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val veryHighRisk = riskText3[1].trim { it <= ' ' }

        if (id == Constants.CBT_ID_2) {
            onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText(">1000")))
            onView(withId(R.id.textResult2)).check(matches(withText(">1000")))
        } else {
            onView(withText(veryHighRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText(">100")))
            onView(withId(R.id.textResult2)).check(matches(withText(">100")))
        }

        getInstrumentation().waitForIdleSync()

        TestUtil.nextPage()

        onView(withText(R.string.round_off_test)).check(matches(isDisplayed()))
        TestUtil.checkTextInTable(R.string.dispose_contents_bag)

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        TestHelper.clickSubmitButton()
    }

    private fun runCbtTotalColiformTest(id: String, buttonIndex: Int) {

        val intent = Intent()
        if (skipOpeningExternalApp()) {
            intent.type = "text/plain"
            intent.action = EXTERNAL_APP_ACTION
            val data = Bundle()
            data.putString(SensorConstants.RESOURCE_ID, id)
            data.putString(SensorConstants.LANGUAGE, BuildConfig.TEST_LANGUAGE)
            intent.putExtras(data)

            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.gotoSurveyForm()
            TestUtil.nextSurveyPage("Coliforms2")
            TestHelper.clickExternalSourceButton(buttonIndex)
        }

        val textView = onView(
                Matchers.allOf(withText("www.aquagenx.com"),
                        TestUtil.childAtPosition(
                                TestUtil.childAtPosition(
                                        IsInstanceOf.instanceOf(LinearLayout::class.java),
                                        0),
                                1),
                        isDisplayed()))
        textView.check(matches(withText("www.aquagenx.com")))

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed()))

        onView(withText("Water - E.coli + Total Coliform")).check(matches(isDisplayed()))

        onView(withText(R.string.submitResult)).check(matches(isDisplayed()))

        onView(withText(R.string.prepare_sample)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        TestUtil.checkTextInTable(R.string.prepare_area_put_on_gloves)

        if (id == Constants.CBT_ID_4) {
            TestUtil.nextPage()

            TestUtil.checkTextInTable(R.string.dissolve_medium_in_sample_tc)
        }

        TestUtil.checkTextInTable(R.string.open_growth_medium_powder)

        onView(withContentDescription("1")).check(matches(DrawableMatcher.hasDrawable()))

        val imageView = onView(
                Matchers.allOf(withContentDescription("1"),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        onView(Matchers.allOf(withId(R.id.pager_indicator),
                TestUtil.childAtPosition(
                        Matchers.allOf(withId(R.id.layout_footer),
                                TestUtil.childAtPosition(
                                        IsInstanceOf.instanceOf(RelativeLayout::class.java),
                                        1)),
                        0),
                isDisplayed()))

        TestUtil.nextPage()

        if (id == Constants.CBT_ID_3) {
            onView(withContentDescription("2")).check(matches(DrawableMatcher.hasDrawable()))

            TestUtil.checkTextInTable(R.string.dissolve_medium_in_sample_tc)

            TestUtil.checkTextInTable(R.string.label_compartment_bag)

            TestUtil.nextPage()
        }

        onView(withContentDescription("4")).check(matches(DrawableMatcher.hasDrawable()))

        onView(withText(getString(R.string.tear_perforated_seam) +
                " " + getString(R.string.rub_sides_of_compartment))).check(matches(isDisplayed()))

        onView(withText(getString(R.string.slowly_pour_sample_into_bag) +
                " " + getString(R.string.fill_evenly_across_bag))).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(getString(R.string.attach_seal_clip) +
                " " + getString(R.string.snap_rod_shaped))).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(getString(R.string.roll_fold_bag) +
                " " + getString(R.string.close_top_of_bag))).check(matches(isDisplayed()))

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.let_incubate)

        onView(withText(R.string.read_instructions)).perform(click())

        onView(withText(R.string.below25Degrees)).check(matches(isDisplayed()))

        onView(withText(R.string.incubate_in_portable)).check(matches(isDisplayed()))

        val appCompatButton3 = onView(
                Matchers.allOf(withId(android.R.id.button1), withText("OK"),
                        TestUtil.childAtPosition(
                                TestUtil.childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        appCompatButton3.perform(ViewActions.scrollTo(), click())

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        mDevice.waitForIdle()

        TestHelper.clickCloseButton()

        mDevice.waitForIdle()

        if (skipOpeningExternalApp()) {
            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.clickExternalSourceButton(buttonIndex)
        }

        onView(withId(R.id.button_phase_2)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()))

        takePhoto()

        onView(withContentDescription("8")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.change_colors_to_match)

        TestUtil.checkTextInTable(R.string.note_blue_green_specks)

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.click_compartments_to_change)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.take_photo_incubated_tc)).check(matches(isDisplayed()))

        takePhoto()

        TestUtil.checkTextInTable(R.string.change_colors_to_match)

        TestUtil.checkTextInTable(R.string.note_total_coliform)

        TestUtil.checkTextInTable(R.string.remember_to_shine)

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.click_compartments_to_change)

        TestUtil.nextPage()

        val riskText = getString(R.string.low_risk_safe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lowRisk = riskText[0].trim { it <= ' ' }
        val safe = riskText[1].trim { it <= ' ' }

        onView(withText(lowRisk)).check(matches(isDisplayed()))
        onView(withText(safe)).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(withText("0")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText("0")))
        onView(withId(R.id.textResult2)).check(matches(isDisplayed()))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        mDevice.pressBack()

        sleep(500)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        val customShapeButton = onView(Matchers
                .allOf(withId(R.id.compartments), isDisplayed()))

        customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f))
        customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f))
        customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f))

        TestUtil.nextPage()

        getInstrumentation().waitForIdleSync()

        if (id == Constants.CBT_ID_4) {
            onView(withText(lowRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("0")))
            onView(withId(R.id.textResult2)).check(matches(withText("56")))
        } else {
            onView(withText(lowRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("0")))
            onView(withId(R.id.textResult2)).check(matches(withText("5.6")))
        }

        onView(withText(safe)).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(isDisplayed()))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        TestUtil.prevPage(4)

        sleep(500)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        val customShapeButton1 = onView(Matchers
                .allOf(withId(R.id.compartments), isDisplayed()))

        customShapeButton1.perform(TestUtil.clickPercent(0.1f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.3f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.5f, 0.5f))
        customShapeButton1.perform(TestUtil.clickPercent(0.7f, 0.1f))
        customShapeButton1.perform(TestUtil.clickPercent(0.9f, 0.1f))

        TestUtil.nextPage()

        val riskText3 = getString(R.string.very_high_risk_unsafe)
                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val veryHighRisk = riskText3[1].trim { it <= ' ' }

        if (id == Constants.CBT_ID_4) {
            onView(withText(R.string.very_unsafe)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText(">1000")))
            onView(withId(R.id.textResult2)).check(matches(withText(">1000")))
        } else {
            onView(withText(veryHighRisk)).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText(">100")))
            onView(withId(R.id.textResult2)).check(matches(withText(">100")))
        }

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.next)).perform(click())

        TestUtil.checkTextInTable(R.string.dispose_contents_bag)

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        TestHelper.clickSubmitButton()
    }

    private fun takePhoto() {
        if (BuildConfig.TEST_RUNNING.get()) {
            TestUtil.nextPage()
        } else {
            onView(withId(R.id.takePhoto)).perform(click())

            sleep(2000)

            mDevice.findObject(
                By.res("com.android.camera2:id/shutter_button").desc("Shutter")
                    .clazz("android.widget.ImageView").text(Pattern.compile(""))
                    .pkg("com.android.camera2")
            ).clickAndWait(
                Until.newWindow(), 6000
            )

            sleep(2000)

            mDevice.findObject(
                By.res("com.android.camera2:id/done_button").desc("Done")
                    .clazz("android.widget.ImageButton")
            ).clickAndWait(
                Until.newWindow(), 6000
            )

            sleep(2000)
        }
    }

    @Test
    fun cbt_Test2() {

        val testConfigRepository = TestConfigRepository()

        val testList = testConfigRepository.getTests(TestType.CBT)
        for (i in 0 until TestConstants.CBT_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.CBT)

            val uuid = testList[i].uuid

            run {
                val intent = Intent()
                intent.type = "text/plain"
                intent.action = EXTERNAL_APP_ACTION
                val data = Bundle()
                data.putString(SensorConstants.RESOURCE_ID, uuid)
                data.putString(SensorConstants.LANGUAGE, BuildConfig.TEST_LANGUAGE)
                intent.putExtras(data)

                mTestActivityRule.launchActivity(intent)

                navigateToCbtTest()

                navigateToCbtTest2(i)
            }
        }
    }

    private fun navigateToCbtTest(): Int {

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

    private fun navigateToCbtTest2(testNumber: Int) {

        sleep(1000)

        onView(withText(R.string.submitResult)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        takePhoto()

        for (i in 0..16) {

            try {
                sleep(1000)

                if (i == 1) {
                    val customShapeButton = onView(Matchers.allOf(withId(R.id.compartments),
                            isDisplayed()))

                    customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f))
                } else if (i == 2 && (testNumber == 2 || testNumber == 3)) {
                    takePhoto()
                } else if (i == 4 && (testNumber == 2 || testNumber == 3)) {
                    onView(withId(R.id.image_pageRight)).perform(click())
                } else if (i == 4) {
                    val customShapeButton = onView(Matchers.allOf(withId(R.id.compartments),
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
