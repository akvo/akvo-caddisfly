package org.akvo.caddisfly.test

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.activateTestMode
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.nextPage
import org.akvo.caddisfly.util.TestUtil.nextSurveyPage
import org.hamcrest.Matchers
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

@RequiresExternalApp
class StriptestTest : BaseTest() {

    companion object {

        private const val waterTab = "Striptest"
        private const val waterTab2 = "Striptest2"
        private const val soilTab = "Soil Striptest"

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
    var mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    override fun setUp() {
        super.setUp()
        loadData(mActivityRule.activity, mCurrentLanguage)
        clearPreferences(mActivityRule)
        activateTestMode()
    }

    @Test
    @RequiresDevice
    fun startStriptest() {
        test5in1()
    }

    @Test
    @RequiresDevice
    fun startSoilNitrogenTest() {
        testSoilNitrogen()
    }

    @Test
    @RequiresDevice
    fun startMerckpHTest() {
        testMerckPH()
    }

    @Test
    @RequiresDevice
    fun startNitrateTest() {
        testNitrate100()
    }

    @Test
    @RequiresDevice
    fun startMercuryTest() {
        testMercury()
    }

    private fun test5in1() {

        gotoSurveyForm()

        nextSurveyPage(waterTab)

        clickExternalSourceButton(3)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(6000)

        onView(withText(R.string.skip)).check(matches(isDisplayed()))

        onView(withText(R.string.skip)).perform(click())

        sleep(1000)

        onView(withText(R.string.skip)).check(doesNotExist())

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }
        }

        onView(withText(R.string.skip)).check(doesNotExist())

        TestHelper.clickStartButton()
        if (isPatchAvailable()) {
            sleep(36000)
        } else {
            sleep(5000)
        }

        onView(withText(R.string.skip)).check(doesNotExist())

        nextPage()

        onView(withText(R.string.skip)).check(doesNotExist())

        TestHelper.clickStartButton()

        if (isPatchAvailable()) {
            sleep(35000)

            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("Total Chlorine")).check(matches(isDisplayed()))
            onView(withText("0 mg/l")).check(matches(isDisplayed()))
            onView(withText("Free Chlorine")).check(matches(isDisplayed()))
            onView(withText("0.15 mg/l")).check(matches(isDisplayed()))
            onView(withText("Total Hardness")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText(R.string.no_result)).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText("Total Alkalinity")).check(matches(isDisplayed()))
            onView(withText("32 mg/l")).check(matches(isDisplayed()))

            mDevice.waitForIdle()
            mDevice.swipe(200, 750, 200, 600, 4)

            onView(withText("pH")).check(matches(isDisplayed()))
            onView(withText("6.2")).check(matches(isDisplayed()))
        } else {
            sleep(5000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()

        mActivityRule.finishActivity()

        sleep(2000)

        if (isPatchAvailable()) {
            assertNotNull(mDevice.findObject(By.text("Total Chlorine: 0.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Free Chlorine: 0.15 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Total Hardness:  mg/l")))
            assertNotNull(mDevice.findObject(By.text("Total Alkalinity: 32.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("pH: 6.2 ")))
        }
    }

    private fun testSoilNitrogen() {

        mDevice.waitForIdle()

        gotoSurveyForm()

        nextSurveyPage(soilTab)

        sleep(1000)

        mDevice.waitForIdle()

        clickExternalSourceButton(0)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        TestHelper.clickStartButton()

        if (isPatchAvailable()) {

            sleep(65000)

            onView(withText(R.string.result)).check(matches(isDisplayed()))

            onView(withText("Nitrogen")).check(matches(isDisplayed()))
            onView(withText("205.15 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrate Nitrogen")).check(matches(isDisplayed()))
            onView(withText("41 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrite Nitrogen")).check(matches(isDisplayed()))
            onView(withText("0.03 mg/l")).check(matches(isDisplayed()))
        } else {
            sleep(5000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()

        mActivityRule.finishActivity()

        sleep(2000)

        if (isPatchAvailable()) {
            assertNotNull(mDevice.findObject(By.text("Nitrogen: 205.15 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Nitrate Nitrogen: 41.0 mg/l")))
            assertNotNull(mDevice.findObject(By.text("Nitrite Nitrogen: 0.03 mg/l")))
        }
    }

    private fun testMerckPH() {

        gotoSurveyForm()

        nextSurveyPage(waterTab)

        clickExternalSourceButton(2)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        TestHelper.clickStartButton()

        sleep(5000)

        onView(withText(R.string.result)).check(matches(isDisplayed()))

        if (isPatchAvailable()) {
            onView(withText("pH")).check(matches(isDisplayed()))
            onView(withText("4.8")).check(matches(isDisplayed()))
        } else {
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        onView(withId(R.id.image_result)).check(matches(isDisplayed()))

        TestHelper.clickSubmitResultButton()

        if (isPatchAvailable()) {
            assertNotNull(mDevice.findObject(By.text("pH: 4.8 ")))
        }
    }

    private fun testNitrate100() {

        gotoSurveyForm()

        nextSurveyPage(waterTab2)

        clickExternalSourceButton(2)

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }
        }

        TestHelper.clickStartButton()

        if (isPatchAvailable()) {
            sleep(60000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("Nitrate")).check(matches(isDisplayed()))
            onView(withText("14.5 mg/l")).check(matches(isDisplayed()))
            onView(withText("Nitrite")).check(matches(isDisplayed()))
            onView(withText("1.85 mg/l")).check(matches(isDisplayed()))
        } else {
            sleep(5000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()
    }

    private fun testMercury() {

        gotoSurveyForm()

        nextSurveyPage(waterTab2)

        val listView = UiScrollable(UiSelector())
        try {
            listView.scrollToEnd(1)
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }

        clickExternalSourceButton(0)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        for (i in 0..16) {
            try {
                nextPage()
            } catch (e: Exception) {
                sleep(200)
                break
            }

        }

        TestHelper.clickStartButton()

        if (isPatchAvailable("aa4a4e3100c9")) {
            sleep(35000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("Mercury")).check(matches(isDisplayed()))
            onView(withText("5 ug/l")).check(matches(isDisplayed()))
            onView(withId(R.id.image_result)).check(matches(isDisplayed()))
        } else {
            sleep(5000)
            onView(withText(R.string.result)).check(matches(isDisplayed()))
            onView(withText("No strip found")).check(matches(isDisplayed()))
        }

        TestHelper.clickSubmitResultButton()

        mActivityRule.finishActivity()

        sleep(2000)

        if (isPatchAvailable("aa4a4e3100c9")) {
            mDevice.swipe(200, 750, 200, 600, 4)
            assertNotNull(mDevice.findObject(By.text("Mercury: 5.0 ug/l")))
        } else {
            assertNotNull(mDevice.findObject(By.text("Mercury: null ug/l")))
        }
    }

    @Test
    fun instructionsTest() {

        gotoSurveyForm()

        nextSurveyPage("Soil Striptest")

        clickExternalSourceButton(1)

        mDevice.waitForIdle()

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        onView(withText(R.string.collect_5ml_mehlich_sample))
                .check(matches(isDisplayed()))

        onView(withText("Soil - Phosphorous"))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.add_5_drops_po4_1)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.swirl_and_mix)).check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_smaller_container)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.add_6_drops_po4_2)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.dip_strip_15_seconds_and_remove)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.dip_strip_15_seconds_in_reagent_and_remove)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.shake_excess_water_off)).check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.place_strip_clr)).check(matches(isDisplayed()))

        onView(Matchers.allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(Matchers.allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.prepare_test)).perform(click())

        Espresso.pressBack()

        Espresso.pressBack()

        sleep(1000)

        onView(withText(R.string.prepare_test)).check(matches(isDisplayed()))
    }

    @Test
    fun ironStripTestInstructions() {

        TestHelper.goToMainScreen()

        gotoSurveyForm()

        nextSurveyPage("Striptest")

        clickExternalSourceButton(0)

        sleep(1000)

        mDevice.waitForIdle()

        sleep(1000)

        onView(withText("Water - Iron"))
                .check(matches(isDisplayed()))

        onView(withText(R.string.prepare_test)).perform(click())

        sleep(5000)

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))

        onView(withText("Water - Iron"))
                .check(matches(isDisplayed()))

        nextPage()

        onView(withText(R.string.open_one_foil_and_add_powder))
                .check(matches(isDisplayed()))

        onView(withId(R.id.pager_indicator)).check(matches(isDisplayed()))

        onView(withId(R.id.viewPager)).perform(ViewActions.swipeLeft())

        onView(Matchers.allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(Matchers.allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.prepare_test)).perform(click())

        Espresso.pressBack()

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))

        Espresso.pressBack()

        sleep(1000)

        onView(withText(R.string.prepare_test)).perform(click())

        onView(Matchers.allOf<View>(withContentDescription(R.string.navigate_up),
                withParent(withId(R.id.toolbar)),
                isDisplayed())).perform(click())

        onView(withText(R.string.fill_half_with_sample))
                .check(matches(isDisplayed()))
    }
}
