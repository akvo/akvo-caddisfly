package org.akvo.caddisfly.internal


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.ui.MainActivity
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.navigateUp
import org.akvo.caddisfly.util.TestHelper.takeScreenshot
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.sleep
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

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
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        loadData(mActivityTestRule.activity, BuildConfig.TEST_LANGUAGE)
        clearPreferences(mActivityTestRule)
    }

    @Test
    fun app_NavigationTest() {

        onView(withId(R.id.button_info)).perform(click())

        sleep(500)

        navigateUp()

        sleep(500)

        onView(withId(R.id.button_info)).perform(click())

        sleep(500)

        pressBack()

        onView(withId(R.id.button_info)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.legalInformation)).perform(click())

        pressBack()

        navigateUp()

        sleep(2000)

        onView(withText(R.string.appName)).check(matches(isDisplayed()))

        onView(withText(R.string.a_water_quality_testing_solution))
                .check(matches(isDisplayed()))

        onView(withText(R.string.test_water_quality_using))
                .check(matches(isDisplayed()))

        takeScreenshot()

        onView(withText(R.string.next)).perform(click())

        getInstrumentation().waitForIdleSync()

        takeScreenshot()

        onView(withId(R.id.button_info)).perform(click())

        takeScreenshot()

        onView(withText(R.string.legalInformation)).perform(click())

        takeScreenshot()

        onView(withId(R.id.homeButton)).perform(click())

        navigateUp()

        sleep(2000)

        onView(withText(R.string.appName)).check(matches(isDisplayed()))

        onView(withText(R.string.next)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.connect_with_app)).check(matches(isDisplayed()))

        onView(withText(R.string.app_is_integrated))
                .check(matches(isDisplayed()))

        onView(withText(R.string.to_conduct_a_water_quality_test))
                .check(matches(isDisplayed()))

        onView(withId(R.id.button_info)).perform(click())

    }
}
