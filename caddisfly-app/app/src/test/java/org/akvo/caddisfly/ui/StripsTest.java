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
import org.akvo.caddisfly.sensor.colorimetry.stripv2.models.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.ui.BrandInfoActivity;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.ui.TestTypeListActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowListView;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class StripsTest {

    @Test
    public void titleIsCorrect() {

        Activity activity = Robolectric.setupActivity(TestTypeListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void testCount() throws Exception {
        Activity activity = Robolectric.setupActivity(TestTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);
        assertSame(20, listView.getCount());
        assertEquals("Water - Total Iron",
                ((StripTest.Brand) listView.getAdapter().getItem(18)).getName());
        assertEquals("Water - Total Iron",
                ((TextView) listView.getChildAt(18).findViewById(R.id.text_title)).getText());
    }

    @Test
    public void testTitles() throws Exception {
        Activity activity = Robolectric.setupActivity(TestTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        for (int i = 0; i < listView.getCount(); i++) {
            StripTest.Brand brand = ((StripTest.Brand) listView.getAdapter().getItem(0));
            String title = brand.getName();
            assertEquals(title,
                    ((TextView) listView.getChildAt(0).findViewById(R.id.text_title)).getText());
        }
    }

    @Test
    public void clickTest() {

        Activity activity = Robolectric.setupActivity(TestTypeListActivity.class);
        ListView listView = activity.findViewById(R.id.list_types);

        ShadowListView list = shadowOf(listView);
        assertTrue(list.performItemClick(1));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(BrandInfoActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(TestTypeListActivity.class);

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

}
