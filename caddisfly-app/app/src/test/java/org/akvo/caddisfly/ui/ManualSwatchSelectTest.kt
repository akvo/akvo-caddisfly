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

package org.akvo.caddisfly.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.sensor.manual.SwatchSelectTestActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class ManualSwatchSelectTest {

    @Test
    fun titleIsCorrect() {

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller =
                Robolectric.buildActivity(TestActivity::class.java, intent).create()
        controller.start()

        val activity = controller.get() as Activity
        val textView = activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertEquals(textView.text, "Water - Chlorine, pH")
    }

    @Test
    fun clickingNext() {

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller =
                Robolectric.buildActivity(TestActivity::class.java, intent).create().start()
        val activity = controller.get() as Activity

        val button = activity.findViewById<Button>(R.id.button_prepare)
        button.performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent2 = shadowOf(activity).nextStartedActivity
        if (nextIntent2.component != null) {
            assertEquals(SwatchSelectTestActivity::class.java.canonicalName,
                    nextIntent2.component!!.className)
        }
    }

    @Test
    fun selectResults() {

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller =
                Robolectric.buildActivity(SwatchSelectTestActivity::class.java, intent).create().start()
        val activity = controller.get() as Activity

        val viewPager = activity.findViewById<ViewPager>(R.id.viewPager)

        for (i in 0..6) {
            viewPager.adapter?.instantiateItem(viewPager, i + 1)
            Robolectric.flushForegroundThreadScheduler()
            ShadowLooper.pauseMainLooper()
            activity.findViewById<View>(R.id.image_pageRight).performClick()
        }

        var textView = activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertEquals(activity.getString(R.string.select_color_intervals), textView.text)

        SystemClock.sleep(3000)

        viewPager.adapter?.instantiateItem(viewPager, 8)
        activity.findViewById<View>(R.id.image_pageRight).performClick()
        textView = activity.findViewById(R.id.textToolbarTitle)
        assertEquals(activity.getString(R.string.result), textView.text)
    }
}
