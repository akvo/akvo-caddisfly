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

package org.akvo.caddisfly.ui;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class MainTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void titleIsCorrect() {
        assertEquals(rule.getActivity().getTitle(), "Akvo Caddisfly");

        TextView textView = rule.getActivity().findViewById(R.id.textToolbarTitle);
        assertNull(textView);
    }

    @Test
    public void onCreateShouldInflateTheMenu() {
        Toolbar toolbar = rule.getActivity().findViewById(R.id.toolbar);
        assertNull(toolbar);
    }

    @Test
    public void introTest() {
        AppCompatImageButton button = rule.getActivity().findViewById(R.id.button_info);

        button.performClick();
        Intent intent = shadowOf(rule.getActivity()).getNextStartedActivity();

        if (intent.getComponent() != null) {
            assertEquals(AboutActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void introNext() {
        Button button = rule.getActivity().findViewById(R.id.button_next);
        button.performClick();

        Button buttonOk = rule.getActivity().findViewById(R.id.button_ok);
        buttonOk.performClick();
    }
}
