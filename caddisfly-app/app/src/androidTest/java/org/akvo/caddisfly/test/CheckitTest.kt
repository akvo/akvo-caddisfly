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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckitTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(getInstrumentation())
            }
        }
    }

    @Rule
    @JvmField
    var mIntentsRule = IntentsTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mIntentsRule.activity, mCurrentLanguage)
        clearPreferences(mIntentsRule)
        stubCameraIntent()
    }

    private fun stubCameraIntent() {
        val result = createImageCaptureStub()
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)
    }

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE")


    private fun createImageCaptureStub(): Instrumentation.ActivityResult {
        // Put the drawable in a bundle.
        val bundle = Bundle()
        bundle.putParcelable("data", BitmapFactory.decodeResource(
                mIntentsRule.activity.resources, R.drawable.closer))

        // Create the Intent that will include the bundle.
        val resultData = Intent()
        resultData.putExtras(bundle)

        // Create the ActivityResult with the Intent.
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Test
    fun runManualCheckitTest() {

        gotoSurveyForm()

        TestUtil.nextSurveyPage("CheckIt")

        sleep(1000)

        clickExternalSourceButton(0)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.checkit_comparator_uses))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.fill_both_cells))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.close_one_cell))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        var phrase = getString(R.string.add_one_tablet_to_other_cell)
        phrase = phrase.replace("%reagent1", "DPD No. 1 (R) (511310)")
        onView(withText(phrase)).check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.place_second_cell))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.match_colors_by_rotating))
                .check(matches(isDisplayed()))

        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withText(R.string.insert_colored_disk))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        onView(withText(R.string.fill_both_cells))
                .check(matches(isDisplayed()))

        TestUtil.nextPage()

        pressBack()

        pressBack()

        pressBack()

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.skip)).perform(click())

        sleep(500)

        onView(withText(R.string.takePhoto)).perform(click())

        sleep(1500)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        TestUtil.swipeLeft()

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("12345"), closeSoftKeyboard())

        sleep(500)

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("4.1"), closeSoftKeyboard())

        sleep(500)

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("3.98"), closeSoftKeyboard())

        onView(withText(R.string.next)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.skip)).perform(click())

        sleep(500)

        onView(withText(R.string.takePhoto)).perform(click())

        sleep(1500)

        onView(withText(R.string.next)).perform(click())

        TestUtil.swipeLeft()

        onView(withId(R.id.image_pageLeft)).check(matches(not<View>(isDisplayed())))

        onView(withId(R.id.image_pageRight)).check(matches(not<View>(isDisplayed())))

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("4.1"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.editResult)).check(matches(isDisplayed()))
                .perform(replaceText("2.53"), closeSoftKeyboard())

        onView(withText(R.string.next)).perform(click())

        sleep(500)

        val submitButton = onView(
                allOf<View>(withId(R.id.buttonSubmit), withText(R.string.submitResult),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.viewPager),
                                        1),
                                2),
                        isDisplayed()))
        submitButton.perform(click())

        mIntentsRule.finishActivity()

        sleep(2000)

        assertNotNull(mDevice.findObject(By.text("Free chlorine: 3.98 mg/l")))
        assertNotNull(mDevice.findObject(By.text("Total chlorine: 2.53 mg/l")))
    }
}
