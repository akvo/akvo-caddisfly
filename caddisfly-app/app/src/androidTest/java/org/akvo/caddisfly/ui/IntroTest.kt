package org.akvo.caddisfly.ui


import android.os.Build
import android.view.View
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
import org.akvo.caddisfly.util.*
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class IntroTest {

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
    fun app_IntroTest() {

        val goToTest = getString(R.string.goToTest)

        onView(withId(R.id.button_info)).perform(click())

        sleep(500)

        navigateUp()

        sleep(500)

        onView(withId(R.id.button_info)).perform(click())

        sleep(1000)

        pressBack()

        sleep(500)

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

        onView(withText(R.string.next)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withId(R.id.button_info)).perform(click())

        onView(withText(R.string.legalInformation)).perform(click())

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

        enterDiagnosticMode()

        onView(withId(R.id.actionSettings)).perform(click())

        pressBack()

        leaveDiagnosticMode()

        sleep(1000)

        pressBack()

        sleep(3000)

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.next)).perform(click())

        getInstrumentation().waitForIdleSync()

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        if (!skipOpeningExternalApp(Build.VERSION.SDK_INT)) {

            onView(withText(R.string.go_to_external_app)).perform(click())

            getInstrumentation().waitForIdleSync()

            sleep(500)

            mDevice.waitForIdle()

            gotoSurveyForm()

            mActivityTestRule.finishActivity()

            sleep(2000)

            assertNotNull(TestUtil.findObject(goToTest))
        }
    }

    @Test
    fun app_LaunchExternalApp() {

        goToMainScreen()

        val goToTest = getString(R.string.goToTest)

        sleep(2000)

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        if (!skipOpeningExternalApp(Build.VERSION.SDK_INT)) {

            onView(withText(R.string.go_to_external_app)).perform(click())

            sleep(500)

            mActivityTestRule.finishActivity()

            mDevice.waitForIdle()

            gotoSurveyForm()

            sleep(2000)

            assertNotNull(TestUtil.findObject(goToTest))
        }
    }

    private fun navigateUp() {
        val imageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        childAtPosition(
                                allOf<View>(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        imageButton.perform(click())
    }
}
