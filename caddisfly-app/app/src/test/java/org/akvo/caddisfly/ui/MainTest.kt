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

import android.os.Build
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.akvo.caddisfly.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MainTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun app_TitleIsCorrect() {
        assertEquals(rule.activity.title, "Akvo Caddisfly")

        val textView = rule.activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertNull(textView)
    }

    @Test
    fun app_OnCreateShouldInflateTheMenu() {
        val toolbar = rule.activity.findViewById<Toolbar>(R.id.toolbar)
        assertNull(toolbar)
    }

    @Test
    fun app_IntroTest() {
        val button = rule.activity.findViewById<AppCompatImageButton>(R.id.button_info)

        button.performClick()
        val intent = shadowOf(rule.activity).nextStartedActivity

        if (intent.component != null) {
            assertEquals(AboutActivity::class.java.canonicalName,
                    intent.component!!.className)
        }
    }

    @Test
    fun app_IntroNext() {
        val button = rule.activity.findViewById<Button>(R.id.button_next)
        button.performClick()

        val buttonOk = rule.activity.findViewById<Button>(R.id.button_ok)
        buttonOk.performClick()
    }
}
