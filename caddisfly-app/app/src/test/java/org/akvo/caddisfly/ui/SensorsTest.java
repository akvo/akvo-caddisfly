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
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.usb.SensorActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowListView;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SensorsTest {

    @Test
    public void titleIsCorrect() {

        Activity activity = Robolectric.setupActivity(SensorTypeListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void sensorCount() throws Exception {
        Activity activity = Robolectric.setupActivity(SensorTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);
        assertSame(3, listView.getCount());
        assertEquals("Soil - Electrical Conductivity",
                ((TestInfo) listView.getAdapter().getItem(0)).getTitle());
        assertEquals("Soil - Electrical Conductivity",
                ((TextView) listView.getChildAt(0).findViewById(R.id.text_title)).getText());
    }


    @Test
    public void sensorTitles() throws Exception {
        Activity activity = Robolectric.setupActivity(SensorTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        assertEquals("Soil - Electrical Conductivity",
                ((TestInfo) listView.getAdapter().getItem(0)).getTitle());
        assertEquals("Soil - Electrical Conductivity",
                ((TextView) listView.getChildAt(0).findViewById(R.id.text_title)).getText());

        assertEquals("Soil - Moisture",
                ((TestInfo) listView.getAdapter().getItem(1)).getTitle());
        assertEquals("Soil - Moisture",
                ((TextView) listView.getChildAt(1).findViewById(R.id.text_title)).getText());

        assertEquals("Water - Electrical Conductivity",
                ((TestInfo) listView.getAdapter().getItem(2)).getTitle());
        assertEquals("Water - Electrical Conductivity",
                ((TextView) listView.getChildAt(2).findViewById(R.id.text_title)).getText());
    }

    @Test
    public void clickSensorItem() {

        Activity activity = Robolectric.setupActivity(SensorTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        ShadowListView list = shadowOf(listView);
        assertTrue(list.performItemClick(1));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(SensorActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(SensorTypeListActivity.class);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

}
