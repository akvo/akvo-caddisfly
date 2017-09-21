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
import org.akvo.caddisfly.sensor.colorimetry.bluetooth.BluetoothTypeListActivity;
import org.akvo.caddisfly.sensor.colorimetry.bluetooth.DeviceScanActivity;
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
public class PhotometerTest {

    @Test
    public void titleIsCorrect() {

        Activity activity = Robolectric.setupActivity(BluetoothTypeListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void testCount() throws Exception {
        Activity activity = Robolectric.setupActivity(BluetoothTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);
        assertSame(52, listView.getCount());
        assertEquals("Fluoride",
                ((TestInfo) listView.getAdapter().getItem(25)).getTitle());
        assertEquals("170 Fluoride",
                ((TextView) listView.getChildAt(25).findViewById(R.id.text_title)).getText());
    }

    @Test
    public void sensorTitles() throws Exception {
        Activity activity = Robolectric.setupActivity(BluetoothTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        for (int i = 0; i < listView.getCount(); i++) {
            TestInfo testInfo = ((TestInfo) listView.getAdapter().getItem(0));
            String title = String.format("%s %s", testInfo.getMd610Id(), testInfo.getTitle());
            assertEquals(title,
                    ((TextView) listView.getChildAt(0).findViewById(R.id.text_title)).getText());
        }
    }

    @Test
    public void clickTest() {

        Activity activity = Robolectric.setupActivity(BluetoothTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        ShadowListView list = shadowOf(listView);
        assertTrue(list.performItemClick(1));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(DeviceScanActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(BluetoothTypeListActivity.class);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

}
