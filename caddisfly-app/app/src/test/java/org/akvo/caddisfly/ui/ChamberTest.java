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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.shadows.ShadowPackageManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ChamberTest {

    @Test
    public void titleIsCorrect() {

        Activity activity = Robolectric.setupActivity(TypeListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void testCount() throws Exception {
        Activity activity = Robolectric.setupActivity(TypeListActivity.class);
        ListView listView = activity.findViewById(android.R.id.list);
        assertSame(3, listView.getCount());
        assertEquals("Water - Fluoride",
                ((TestInfo) listView.getAdapter().getItem(1)).getTitle());
        assertEquals("Water - Fluoride",
                ((TextView) listView.getChildAt(1).findViewById(R.id.textName)).getText());
    }

    @Test
    public void testTitles() throws Exception {
        Activity activity = Robolectric.setupActivity(TypeListActivity.class);
        ListView listView = activity.findViewById(android.R.id.list);

        for (int i = 0; i < listView.getCount(); i++) {
            TestInfo testInfo = ((TestInfo) listView.getAdapter().getItem(0));
            String title = testInfo.getTitle();
            assertEquals(title,
                    ((TextView) listView.getChildAt(0).findViewById(R.id.textName)).getText());
        }
    }

    @Test
    public void clickTest() {

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ActivityController controller = Robolectric.buildActivity(TypeListActivity.class).create().start();
        Activity activity = (Activity) controller.get();

        ListView listView = activity.findViewById(android.R.id.list);

        ShadowListView list = shadowOf(listView);
        assertTrue(list.performItemClick(1));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNull(intent);

        ShadowApplication application = Shadows.shadowOf(activity.getApplication());
        application.grantPermissions(permissions);
        controller.resume();

        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);

        assertTrue(list.performItemClick(1));

        intent = shadowOf(activity).getNextStartedActivity();
        if (intent.getComponent() != null) {
            assertEquals(CalibrateListActivity.class.getCanonicalName(),
                    intent.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(TypeListActivity.class);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

}
