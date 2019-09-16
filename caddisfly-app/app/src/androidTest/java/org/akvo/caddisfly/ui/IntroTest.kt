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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.akvo.caddisfly.R
import org.akvo.caddisfly.util.TestHelper
import org.akvo.caddisfly.util.TestHelper.clearPreferences
import org.akvo.caddisfly.util.TestHelper.enterDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.getString
import org.akvo.caddisfly.util.TestHelper.goToMainScreen
import org.akvo.caddisfly.util.TestHelper.gotoSurveyForm
import org.akvo.caddisfly.util.TestHelper.leaveDiagnosticMode
import org.akvo.caddisfly.util.TestHelper.loadData
import org.akvo.caddisfly.util.TestHelper.mCurrentLanguage
import org.akvo.caddisfly.util.TestUtil.childAtPosition
import org.akvo.caddisfly.util.mDevice
import org.akvo.caddisfly.util.skipOpeningExternalApp
import org.akvo.caddisfly.util.sleep
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
        loadData(mActivityTestRule.activity, mCurrentLanguage)
        clearPreferences(mActivityTestRule)
    }

    @Test
    fun introTest() {

        val goToTest = getString(R.string.goToTest)

        onView(withId(R.id.button_info)).perform(click())

        navigateUp()

        onView(withId(R.id.button_info)).perform(click())

        pressBack()

        onView(withId(R.id.button_info)).perform(click())

        onView(withText(R.string.legalInformation)).perform(click())

        pressBack()

        navigateUp()

        onView(withText(R.string.appName)).check(matches(isDisplayed()))

        onView(withText(R.string.a_water_quality_testing_solution))
                .check(matches(isDisplayed()))

        onView(withText(R.string.test_water_quality_using))
                .check(matches(isDisplayed()))

        onView(withText(R.string.next)).perform(click())

        onView(withId(R.id.button_info)).perform(click())

        onView(withText(R.string.legalInformation)).perform(click())

        onView(withId(R.id.homeButton)).perform(click())

        navigateUp()

        onView(withText(R.string.appName)).check(matches(isDisplayed()))

        onView(withText(R.string.next)).perform(click())

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

        pressBack()

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        if (!skipOpeningExternalApp(Build.MODEL)) {

            onView(withText(R.string.go_to_external_app)).perform(click())

            sleep(500)

            mDevice.waitForIdle()

            gotoSurveyForm()

            mActivityTestRule.finishActivity()

            sleep(2000)

            assertNotNull(mDevice.findObject(By.text(goToTest)))
        }
    }

    @Test
    fun launchExternalApp() {

        goToMainScreen()

        val goToTest = getString(R.string.goToTest)

        onView(withText(R.string.next)).perform(click())

        onView(withText(R.string.go_to_external_app)).check(matches(isDisplayed()))

        if (!skipOpeningExternalApp(Build.MODEL)) {

            onView(withText(R.string.go_to_external_app)).perform(click())

            sleep(500)

            mActivityTestRule.finishActivity()

            mDevice.waitForIdle()

            gotoSurveyForm()

            sleep(2000)

            assertNotNull(mDevice.findObject(By.text(goToTest)))
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
