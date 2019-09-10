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

@file:Suppress("DEPRECATION")

package org.akvo.caddisfly.util

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import androidx.test.uiautomator.*
import org.akvo.caddisfly.R
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.TestConstants
import org.hamcrest.Matchers.allOf
import timber.log.Timber
import java.io.File
import java.util.*

lateinit var mDevice: UiDevice

val isPatchAvailable: Boolean
    get() = Build.MANUFACTURER == "samsung"

fun isLowMemoryDevice(model: String): Boolean {
    return model.contains("ASUS_Z007")
}

object TestHelper {

    const val mCurrentLanguage = "en"
    private const val TAKE_SCREENSHOTS = false
    private val STRING_HASH_MAP_EN = HashMap<String, String>()
    private val STRING_HASH_MAP_ES = HashMap<String, String>()
    private val STRING_HASH_MAP_FR = HashMap<String, String>()
    private val STRING_HASH_MAP_IN = HashMap<String, String>()
    lateinit var currentHashMap: Map<String, String>

//    private var mCounter: Int = 0

    val currentActivity: Activity
        get() {
            val currentActivity = arrayOf<Any>({ null })
            getInstrumentation().runOnMainSync {
                val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(RESUMED)
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity[0] = resumedActivities.iterator().next() as Activity
                }
            }
            return currentActivity[0] as Activity
        }


    private fun addString(key: String, vararg values: String) {
        STRING_HASH_MAP_EN[key] = values[0]
        if (values.size > 1) {
            STRING_HASH_MAP_ES[key] = values[1]
            STRING_HASH_MAP_FR[key] = values[2]
            STRING_HASH_MAP_IN[key] = values[3]
        } else {
            STRING_HASH_MAP_ES[key] = values[0]
            STRING_HASH_MAP_FR[key] = values[0]
            STRING_HASH_MAP_IN[key] = values[0]
        }
    }

    fun getString(@StringRes resourceId: Int): String {
        return getString(currentActivity, resourceId)
    }

    fun getString(activity: Activity, @StringRes resourceId: Int): String {
        val currentResources = activity.resources
        val assets = currentResources.assets
        val metrics = currentResources.displayMetrics
        val config = Configuration(currentResources.configuration)
        config.locale = Locale(mCurrentLanguage)
        val res = Resources(assets, metrics, config)

        return res.getString(resourceId)
    }

    fun loadData(activity: Activity, languageCode: String) {

        STRING_HASH_MAP_EN.clear()
        STRING_HASH_MAP_ES.clear()
        STRING_HASH_MAP_FR.clear()
        STRING_HASH_MAP_IN.clear()

        val currentResources = activity.resources
        val assets = currentResources.assets
        val metrics = currentResources.displayMetrics
        val config = Configuration(currentResources.configuration)
        config.locale = Locale(languageCode)
        val res = Resources(assets, metrics, config)

        addString(TestConstant.LANGUAGE, "English", "Español", "Français", "Bahasa Indonesia")
        //        addString("otherLanguage", "Français", "English");
        addString(TestConstant.FLUORIDE, res.getString(R.string.fluoride))
        addString("chlorine", res.getString(R.string.freeChlorine))
        addString("survey", res.getString(R.string.survey))
        addString("sensors", res.getString(R.string.sensors))
        addString("electricalConductivity", res.getString(R.string.electricalConductivity))
        addString("unnamedDataPoint", res.getString(R.string.unnamedDataPoint))
        addString("createNewDataPoint", res.getString(R.string.addDataPoint))
        addString(TestConstant.USE_EXTERNAL_SOURCE, res.getString(R.string.useExternalSource))
        addString(TestConstant.GO_TO_TEST, res.getString(R.string.goToTest))
        addString("next", res.getString(R.string.next))

        // Restore device-specific locale
        Resources(assets, metrics, currentResources.configuration)

        currentHashMap = when (languageCode) {
            "en" -> STRING_HASH_MAP_EN
            "es" -> STRING_HASH_MAP_ES
            "in" -> STRING_HASH_MAP_IN
            else -> STRING_HASH_MAP_FR
        }
    }

    //    fun takeScreenshot() {
//        if (TAKE_SCREENSHOTS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            val path = File(Environment.getExternalStorageDirectory().path
//                    + "/Akvo Caddisfly/screenshots/screen-" + mCounter++ + "-" + mCurrentLanguage + ".png")
//            mDevice.takeScreenshot(path, 0.5f, 60)
//        }
//    }
//
    fun takeScreenshot(name: String, page: Int) {
        if (TAKE_SCREENSHOTS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val path = File(Environment.getExternalStorageDirectory().path
                    + "/Akvo Caddisfly/screenshots/" + name + "-" + mCurrentLanguage + "-" +
                    String.format("%02d", page + 1) + ".png")
            mDevice.takeScreenshot(path, 0.1f, 30)
        }
    }

    fun goToMainScreen() {

        var found = false
        while (!found) {
            try {
                onView(withId(R.id.button_info)).check(matches(isDisplayed()))
                found = true
            } catch (e: NoMatchingViewException) {
                Espresso.pressBack()
            }

        }
    }

    fun activateTestMode() {

        @Suppress("ConstantConditionIf")
        if (!AppConfig.IS_TEST_MODE) {

            onView(withId(R.id.button_info)).perform(click())

            onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click())

            val version = CaddisflyApp.getAppVersion(false)

            onView(withText(version)).check(matches(isDisplayed()))

            enterDiagnosticMode()

            onView(withId(R.id.actionSettings)).perform(click())

            clickListViewItem("Test Mode")
        }

        goToMainScreen()
    }

    fun clickExternalSourceButton(index: Int) {

        var buttonText = currentHashMap[TestConstant.GO_TO_TEST]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            assert(buttonText != null)
            buttonText = buttonText!!.toUpperCase()
        }

        findButtonInScrollable(buttonText!!)

        val buttons = mDevice.findObjects(By.text(buttonText))

        if (index < buttons.size) {
            buttons[index].click()
        } else {
            val listView = UiScrollable(UiSelector())
            try {
                listView.scrollToEnd(1)
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }

            val buttons1 = mDevice.findObjects(By.text(buttonText))
            buttons1[buttons1.size - 1].click()
        }

        // New Android OS seems to popup a button for external app
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            sleep(1000)
            mDevice.findObject(By.text("Akvo Caddisfly")).click()
            sleep(1000)
        }

        mDevice.waitForWindowUpdate("", 2000)

        sleep(4000)
    }

    fun clickExternalSourceButton(text: String) {
        try {

            var buttonText = currentHashMap[text]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                assert(buttonText != null)
                buttonText = buttonText!!.toUpperCase()
            }

            findButtonInScrollable(buttonText!!)

            mDevice.findObject(UiSelector().text(buttonText)).click()

            // New Android OS seems to popup a button for external app
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && (text == TestConstant.USE_EXTERNAL_SOURCE || text == TestConstant.GO_TO_TEST)) {
                sleep(1000)
                mDevice.findObject(By.text("Akvo Caddisfly")).click()
                sleep(1000)
            }

            mDevice.waitForWindowUpdate("", 2000)

        } catch (e: UiObjectNotFoundException) {
            Timber.e(e)
        }

    }

    fun gotoSurveyForm() {

        val context = getInstrumentation().context
        val intent = context.packageManager.getLaunchIntentForPackage(TestConstants.FLOW_SURVEY_PACKAGE_NAME)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        if (!currentHashMap["unnamedDataPoint"]?.let { clickListViewItem(it) }!!) {

            val addButton = mDevice.findObject(UiSelector()
                    .resourceId("org.akvo.flow:id/add_data_point_fab"))

            try {
                if (addButton.exists() && addButton.isEnabled) {
                    addButton.click()
                }
            } catch (e: UiObjectNotFoundException) {
                Timber.e(e)
            }

        }

        // mDevice.findObject(By.text("Caddisfly Tests")).click();
    }

    fun enterDiagnosticMode() {
        for (i in 0..9) {
            onView(withId(R.id.textVersion)).perform(click())
        }
    }

    fun leaveDiagnosticMode() {
        onView(withId(R.id.fabDisableDiagnostics)).perform(click())
    }

    fun clearPreferences(activityTestRule: ActivityTestRule<*>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activityTestRule.activity)
        prefs.edit().clear().apply()
    }

    fun navigateUp() {
        val appCompatImageButton = onView(
                allOf<View>(withContentDescription(R.string.navigate_up),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()))
        appCompatImageButton.perform(click())
    }

    fun isDeviceInitialized(): Boolean {
        return ::mDevice.isInitialized
    }
}
