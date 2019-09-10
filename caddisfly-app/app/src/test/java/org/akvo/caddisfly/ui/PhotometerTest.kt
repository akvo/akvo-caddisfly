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
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.akvo.caddisfly.R
import org.akvo.caddisfly.TestConstants.MD610_TESTS_COUNT
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.model.TestType
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
class PhotometerTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(TestListActivity::class.java)

    @Test
    fun titleIsCorrect() {
        val textView = rule.activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertEquals(textView.text, "Select Test")
    }

    @Test
    fun testCount() {

        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.BLUETOOTH)

        val controller =
                Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity

        val recyclerView = activity.findViewById<RecyclerView>(R.id.list_types)

        assertSame(MD610_TESTS_COUNT, (recyclerView.adapter as TestInfoAdapter).itemCount)

        assertEquals("Cyanide",
                (recyclerView.adapter as TestInfoAdapter).getItemAt(26).name)
        assertEquals("62 Ammonia",
                (recyclerView.getChildAt(4).findViewById<View>(R.id.text_title) as TextView).text)
    }

    @Test
    fun sensorTitles() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.BLUETOOTH)

        val controller =
                Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity

        val recyclerView = activity.findViewById<RecyclerView>(R.id.list_types)

        for (i in 0 until recyclerView.childCount) {
            val testInfo = (recyclerView.adapter as TestInfoAdapter).getItemAt(i)

            var title = testInfo.name
            assertEquals(title,
                    (recyclerView.adapter as TestInfoAdapter).getItemAt(i).name)

            title = testInfo.md610Id + " " + testInfo.name
            assertEquals(title,
                    (recyclerView.getChildAt(i).findViewById<View>(R.id.text_title) as TextView).text)
        }
    }

    @Test
    fun clickTest() {

        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.BLUETOOTH)

        val controller =
                Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity

        val recyclerView = activity.findViewById<RecyclerView>(R.id.list_types)

        assertSame(MD610_TESTS_COUNT, (recyclerView.adapter as TestInfoAdapter).itemCount)

        recyclerView.getChildAt(1).performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent = shadowOf(activity).nextStartedActivity
        if (nextIntent != null && nextIntent.component != null) {
            assertEquals(TestActivity::class.java.canonicalName,
                    nextIntent.component!!.className)
        }
    }

    @Test
    fun clickHome() {
        val activity = rule.activity
        val shadowActivity = shadowOf(activity)
        shadowActivity.clickMenuItem(android.R.id.home)
        val intent = shadowOf(activity).nextStartedActivity
        assertNull(intent)
    }
}
