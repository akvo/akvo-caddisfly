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

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainTest {

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertEquals(activity.getTitle(), "Akvo Caddisfly");

        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertNull(textView);
    }

    @Test
    public void onCreateShouldInflateTheMenu() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        assertNull(toolbar);
    }

    @Test
    public void introTest() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        AppCompatImageButton button = activity.findViewById(R.id.button_info);

        button.performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();

        if (intent.getComponent() != null) {
            assertEquals(AboutActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void introNext() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);

        Button button = activity.findViewById(R.id.button_next);
        button.performClick();

        Button buttonOk = activity.findViewById(R.id.button_ok);
        buttonOk.performClick();
    }
}
