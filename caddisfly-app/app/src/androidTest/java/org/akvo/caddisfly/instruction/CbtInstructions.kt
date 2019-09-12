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
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.common.TestConstants
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.ui.TestActivity
import org.akvo.caddisfly.util.DrawableMatcher.Companion.hasDrawable
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.isDeviceInitialized
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.TestUtil.checkTextInTable
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.TestUtil.clickPercent
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CbtInstructions {

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
    fun setUp() {
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
        scale = mActivityTestRule.activity.resources.displayMetrics.density
        mActivityTestRule.finishActivity()
    }


    @Test
    fun cbtInstructions() {

        gotoSurveyForm()

        nextSurveyPage("Coliforms")

        clickExternalSourceButton(0)

        val textView = onView(
                allOf<View>(withText("www.aquagenx.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf<View>(android.widget.LinearLayout::class.java),
                                        0),
                                1),
                        isDisplayed()))
        textView.check(matches(withText("www.aquagenx.com")))

        onView(withText(R.string.prepare_sample)).check(matches(isDisplayed()))

        onView(withText("Water - E.coli")).check(matches(isDisplayed()))

        onView(withText(R.string.submitResult)).check(matches(isDisplayed()))

        onView(withText(R.string.prepare_sample)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        checkTextInTable(R.string.prepare_area_put_on_gloves)

        checkTextInTable(R.string.open_growth_medium)

        onView(withContentDescription("1")).check(matches(hasDrawable()))

        val imageView = onView(
                allOf<View>(withContentDescription("1"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf<View>(android.widget.ScrollView::class.java),
                                        0),
                                6),
                        isDisplayed()))
        imageView.check(matches(isDisplayed()))

        onView(allOf<View>(withId(R.id.pager_indicator),
                childAtPosition(
                        allOf<View>(withId(R.id.layout_footer),
                                childAtPosition(
                                        IsInstanceOf.instanceOf<View>(android.widget.RelativeLayout::class.java),
                                        1)),
                        0),
                isDisplayed()))

        nextPage()

        onView(withContentDescription("2")).check(matches(hasDrawable()))

        checkTextInTable(R.string.dissolve_medium_in_sample_a)

        checkTextInTable(R.string.dissolve_medium_in_sample_b)

        nextPage()

        onView(withContentDescription("4")).check(matches(hasDrawable()))

        checkTextInTable(R.string.label_compartment_bag)

        nextPage(3)

        checkTextInTable(R.string.let_incubate)

        onView(withText(R.string.read_instructions)).perform(click())

        onView(withText(R.string.below25Degrees)).check(matches(isDisplayed()))

        onView(withText(R.string.incubate_in_portable)).check(matches(isDisplayed()))

        val appCompatButton3 = onView(
                allOf<View>(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        appCompatButton3.perform(scrollTo(), click())

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        mDevice.waitForIdle()

        TestHelper.clickCloseButton()

        mDevice.waitForIdle()

        clickExternalSourceButton(0)

        onView(withId(R.id.button_phase_2)).perform(click())

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withText(R.string.take_photo_of_incubated)).check(matches(isDisplayed()))

        nextPage()

        onView(withContentDescription("8")).check(matches(hasDrawable()))

        checkTextInTable(R.string.change_colors_to_match)

        checkTextInTable(R.string.note_blue_green_specks)

        nextPage()

        checkTextInTable(R.string.click_compartments_to_change)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))

        onView(withId(R.id.image_pageRight)).check(matches(isDisplayed()))

        nextPage()

        onView(withText("Low Risk")).check(matches(isDisplayed()))

        onView(withText("Safe")).check(matches(isDisplayed()))

        mDevice.pressBack()

        sleep(500)

        onView(withText(R.string.setCompartmentColors)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.next)).perform(click())

        checkTextInTable(R.string.dispose_contents_bag)

        if (scale > 1.5) {
            onView(withId(R.id.image_pageLeft)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        TestHelper.clickSubmitButton()
    }

    @Test
    @RequiresDevice
    fun testInstructionsCbt() {

        val testConfigRepository = TestConfigRepository()

        val testList = testConfigRepository.getTests(TestType.CBT)
        for (i in 0 until TestConstants.CBT_TESTS_COUNT) {

            assertEquals(testList!![i].subtype, TestType.CBT)
            val uuid = testList[i].uuid
            val id = uuid.substring(uuid.lastIndexOf("-") + 1)

            //            if (("bf80d7197176, ac22c9afa0ab").contains(id))
            //
            run {
                val intent = Intent()
                intent.type = "text/plain"
                intent.action = AppConfig.EXTERNAL_APP_ACTION
                val data = Bundle()
                data.putString(SensorConstants.RESOURCE_ID, uuid)
                data.putString(SensorConstants.LANGUAGE, mCurrentLanguage)
                intent.putExtras(data)

                mTestActivityRule.launchActivity(intent)

                val pages = navigateToCbtTest(id)

                sleep(2000)

                mTestActivityRule.launchActivity(intent)

                navigateToCbtTest2(id, pages)

                sleep(1000)

                //                jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
                //                listString.append("<li><span onclick=\"loadTestType(\'").append(id)
                //                        .append("\')\">").append(testList.get(i).getName()).append("</span></li>");
                //                TestHelper.getCurrentActivity().finish();
                //                mTestActivityRule.finishActivity();
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
                        allOf<View>(withText("Read Instructions"),
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

                sleep(300)
                break
            }

        }
        return pages + 1
    }


    //    @Test
    //    @RequiresDevice
    //    public void testInstructionsAll() {
    //
    //        final TestListViewModel viewModel =
    //                ViewModelProviders.of(mActivityTestRule.getActivity()).get(TestListViewModel.class);
    //
    //        List<TestInfo> testList = viewModel.getTests(TestType.CBT);
    //
    //        for (int i = 0; i < TestConstants.CBT_TESTS_COUNT; i++) {
    //            TestInfo testInfo = testList.get(i);
    //
    //            String id = testInfo.getUuid();
    //            id = id.substring(id.lastIndexOf("-") + 1);
    //
    //            int pages = navigateToTest(i, id);
    //
    //            onView(withId(R.id.imageBrand)).check(matches(hasDrawable()));
    //
    //            onView(withText(testInfo.getName())).check(matches(isDisplayed()));
    //
    //            mDevice.pressBack();
    //
    //            jsArrayString.append("[").append("\"").append(id).append("\",").append(pages).append("],");
    //        }
    //
    //        mActivityTestRule.finishActivity();
    //
    //        Log.d("Caddisfly", jsArrayString.toString());
    //        Log.d("Caddisfly", listString.toString());
    //
    //    }

    //    private int navigateToTest(int index, String id) {
    //
    //        gotoSurveyForm();
    //
    //        TestUtil.nextSurveyPage("Coliforms");
    //
    //        clickExternalSourceButton(index);
    //
    //        mDevice.waitForIdle();
    //
    //        sleep(1000);
    //
    //        takeScreenshot(id, -1);
    //
    //        mDevice.waitForIdle();
    //
    //        onView(withText(R.string.prepare_sample)).perform(click());
    //
    //        int pages = 0;
    //        for (int i = 0; i < 17; i++) {
    //            try {
    //                takeScreenshot(id, pages);
    //
    //                pages++;
    //
    //                try {
    //                    onView(withId(R.id.button_phase_2)).perform(click());
    //                    sleep(600);
    //                    takeScreenshot(id, pages);
    //                    pages++;
    //                    sleep(600);
    //                    mDevice.pressBack();
    //                } catch (Exception ignore) {
    //                }
    //
    //                onView(withId(R.id.image_pageRight)).perform(click());
    //
    //            } catch (Exception e) {
    //                sleep(600);
    //                Random random = new Random(Calendar.getInstance().getTimeInMillis());
    //                if (random.nextBoolean()) {
    //                    Espresso.pressBack();
    //                } else {
    //                    mDevice.pressBack();
    //                }
    //                break;
    //            }
    //        }
    //        return pages;
    //    }
}
