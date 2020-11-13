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
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.rule.ActivityTestRule
import org.akvo.caddisfly.R
import org.akvo.caddisfly.TestConstants.STRIP_TESTS_COUNT
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.model.TestType
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class StripsTest {

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
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)

        val controller =
                Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity

        val recyclerView = activity.findViewById<RecyclerView>(R.id.list_types)

        assertSame(STRIP_TESTS_COUNT, recyclerView.adapter?.itemCount)

        val adapter = recyclerView.adapter as TestInfoAdapter?
        recyclerView.adapter
        assert(adapter != null)
        assertEquals("Water - Potassium",
                adapter!!.getItemAt(20).name)
        assertEquals("Soil - Phosphorous",
                (recyclerView.getChildAt(4).findViewById<View>(R.id.text_title) as TextView).text)
    }

    @Test
    fun testTitles() {
        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)

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
            val testInfo = (recyclerView.adapter as TestInfoAdapter).getItemAt(0)
            val title = testInfo.name
            assertEquals(title,
                    (recyclerView.getChildAt(0).findViewById<View>(R.id.text_title) as TextView).text)
        }
    }

    @Test
    fun clickTest() {

        val intent = Intent()
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST)

        val controller =
                Robolectric.buildActivity(TestListActivity::class.java, intent).create()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        controller.start().visible()

        Robolectric.flushForegroundThreadScheduler()
        ShadowLooper.pauseMainLooper()

        val activity = controller.get() as Activity

        val recyclerView = activity.findViewById<RecyclerView>(R.id.list_types)

        assertSame(STRIP_TESTS_COUNT, recyclerView.adapter?.itemCount)
        assertSame(5, recyclerView.childCount)

        recyclerView.getChildAt(1).performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent = shadowOf(activity).nextStartedActivity

        if (nextIntent.component != null) {
            assertEquals(TestActivity::class.java.canonicalName,
                    nextIntent.component!!.className)
        }
    }

    @Test
    fun clickHome() {
        val shadowActivity = shadowOf(rule.activity)
        shadowActivity.clickMenuItem(android.R.id.home)
        val intent = shadowOf(rule.activity).nextStartedActivity

        assertNull(intent)
    }
}
