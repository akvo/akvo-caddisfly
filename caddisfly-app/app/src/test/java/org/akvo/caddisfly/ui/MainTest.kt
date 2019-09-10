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

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.akvo.caddisfly.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class MainTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun titleIsCorrect() {
        assertEquals(rule.activity.title, "Akvo Caddisfly")

        val textView = rule.activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertNull(textView)
    }

    @Test
    fun onCreateShouldInflateTheMenu() {
        val toolbar = rule.activity.findViewById<Toolbar>(R.id.toolbar)
        assertNull(toolbar)
    }

    @Test
    fun introTest() {
        val button = rule.activity.findViewById<AppCompatImageButton>(R.id.button_info)

        button.performClick()
        val intent = shadowOf(rule.activity).nextStartedActivity

        if (intent.component != null) {
            assertEquals(AboutActivity::class.java.canonicalName,
                    intent.component!!.className)
        }
    }

    @Test
    fun introNext() {
        val button = rule.activity.findViewById<Button>(R.id.button_next)
        button.performClick()

        val buttonOk = rule.activity.findViewById<Button>(R.id.button_ok)
        buttonOk.performClick()
    }
}
