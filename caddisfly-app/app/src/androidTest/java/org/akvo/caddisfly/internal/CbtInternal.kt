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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtInternal {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            }
        }
    }

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
        TestHelper.loadData(mActivityTestRule.activity, TestHelper.mCurrentLanguage)
        scale = mActivityTestRule.activity.resources.displayMetrics.density
        mActivityTestRule.finishActivity()
    }

    @Test
    fun testCbt1() {
        cbtInstructions(Constants.CBT_ID)
    }

    @Test
    fun testCbt2() {
        cbtInstructions(Constants.CBT_ID_2)
    }

    @Test
    fun testCbt3() {
        cbtInstructions2(Constants.CBT_ID_3)
    }

    @Test
    fun testCbt4() {
        cbtInstructions2(Constants.CBT_ID_4)
    }

    private fun cbtInstructions(id: String) {

        val intent = Intent()
        if (skipOpeningExternalApp()) {
            intent.type = "text/plain"
            intent.action = AppConfig.EXTERNAL_APP_ACTION
            val data = Bundle()
            data.putString(SensorConstants.RESOURCE_ID, id)
            data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage)
            intent.putExtras(data)

            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.gotoSurveyForm()
            TestUtil.nextSurveyPage("Coliforms")
            TestHelper.clickExternalSourceButton(0)
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
            TestHelper.clickExternalSourceButton(0)
        }

        onView(withId(R.id.button_phase_2)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withContentDescription("8")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.change_colors_to_match)

        TestUtil.checkTextInTable(R.string.note_blue_green_specks)

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.click_compartments_to_change)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText("Low Risk")).check(matches(isDisplayed()))

        onView(withText("Safe")).check(matches(isDisplayed()))

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

        if (id == Constants.CBT_ID_2) {
            onView(withText("Low Risk")).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("56")))
            onView(withId(R.id.textResult2)).check(matches(withText("56")))
        } else {
            onView(withText("Intermediate Risk")).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("5.6")))
            onView(withId(R.id.textResult2)).check(matches(withText("5.6")))
        }

        onView(withText("Possibly Safe")).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(not(isDisplayed())))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        onView(withText(R.string.next)).perform(click())

        TestUtil.checkTextInTable(R.string.dispose_contents_bag)

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        TestHelper.clickSubmitButton()
    }

    private fun cbtInstructions2(id: String) {

        val intent = Intent()
        if (skipOpeningExternalApp()) {
            intent.type = "text/plain"
            intent.action = AppConfig.EXTERNAL_APP_ACTION
            val data = Bundle()
            data.putString(SensorConstants.RESOURCE_ID, id)
            data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage)
            intent.putExtras(data)

            mTestActivityRule.launchActivity(intent)
        } else {
            TestHelper.gotoSurveyForm()
            TestUtil.nextSurveyPage("Coliforms")
            TestHelper.clickExternalSourceButton(0)
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

        onView(withContentDescription("2")).check(matches(DrawableMatcher.hasDrawable()))

        TestUtil.checkTextInTable(R.string.dissolve_medium_in_sample_tc)

        TestUtil.checkTextInTable(R.string.label_compartment_bag)

        TestUtil.nextPage()

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
            TestHelper.clickExternalSourceButton(0)
        }

        onView(withId(R.id.button_phase_2)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not(isDisplayed())))

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()))

        TestUtil.nextPage()

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

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.change_colors_to_match)

        TestUtil.checkTextInTable(R.string.note_total_coliform)

        TestUtil.checkTextInTable(R.string.remember_to_shine)

        TestUtil.nextPage()

        TestUtil.checkTextInTable(R.string.click_compartments_to_change)

        TestUtil.nextPage()

        onView(withText("Low Risk")).check(matches(isDisplayed()))
        onView(withText("Safe")).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(withText("0")))
        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(withText("0")))
        onView(withId(R.id.textResult2)).check(matches(isDisplayed()))
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

        if (id == Constants.CBT_ID_4) {
            onView(withText("Low Risk")).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("0")))
            onView(withId(R.id.textResult2)).check(matches(withText("56")))
        } else {
            onView(withText("Low Risk")).check(matches(isDisplayed()))
            onView(withId(R.id.textResult1)).check(matches(withText("0")))
            onView(withId(R.id.textResult2)).check(matches(withText("5.6")))
        }

        onView(withText("Safe")).check(matches(isDisplayed()))

        onView(withId(R.id.textResult1)).check(matches(isDisplayed()))
        onView(withId(R.id.textResult2)).check(matches(isDisplayed()))
        onView(withId(R.id.textUnit2)).check(matches(withText("MPN/100ml")))

        onView(withText(R.string.next)).perform(click())

        TestUtil.checkTextInTable(R.string.dispose_contents_bag)

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not(isDisplayed())))

        TestHelper.clickSubmitButton()
    }

    @Test
    fun testCbtWithoutSurvey() {

        val testConfigRepository = TestConfigRepository()

        val testList = testConfigRepository.getTests(TestType.CBT)
        for (i in 0 until TestConstants.CBT_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.CBT)

            val uuid = testList[i].uuid

//            val id = uuid.substring(uuid.lastIndexOf("-") + 1)

            //            if (("9991fb84dd90 606b771e0ffe 6060e4dbe59d").contains(id))
            //
            run {
                val intent = Intent()
                intent.type = "text/plain"
                intent.action = AppConfig.EXTERNAL_APP_ACTION
                val data = Bundle()
                data.putString(SensorConstants.RESOURCE_ID, uuid)
                data.putString(SensorConstants.LANGUAGE, TestHelper.mCurrentLanguage)
                intent.putExtras(data)

                mTestActivityRule.launchActivity(intent)

                navigateToCbtTest()

                navigateToCbtTest2()
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

    private fun navigateToCbtTest2() {

        sleep(1000)

        onView(withText(R.string.submitResult)).check(matches(isDisplayed())).perform(click())

        sleep(1000)

        for (i in 0..16) {

            try {
                sleep(1000)

                if (i == 2) {
                    val customShapeButton = onView(Matchers.allOf(withId(R.id.compartments),
                            isDisplayed()))

                    customShapeButton.perform(TestUtil.clickPercent(0.1f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.5f, 0.5f))
                    customShapeButton.perform(TestUtil.clickPercent(0.9f, 0.1f))
                } else if (i == 5) {
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
