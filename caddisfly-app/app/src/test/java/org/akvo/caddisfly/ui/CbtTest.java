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
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import junit.framework.Assert;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.cbt.TestActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CbtTest {

    @Test
    public void titleIsCorrect() {

        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.CBT_ID);

        Activity activity = Robolectric.setupActivity(TestActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertTrue(textView.getText().toString().equals("E.coli – Aquagenx CBT"));
    }

    @Test
    public void clickingInstructions() {

        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.CBT_ID);

        Activity activity = Robolectric.setupActivity(TestActivity.class);
        Button button = activity.findViewById(R.id.button_instructions);

        button.performClick();

        ViewPager viewPager = activity.findViewById(R.id.viewPager);

        assertNotNull(viewPager);
        Assert.assertEquals(viewPager.getVisibility(), View.VISIBLE);

    }

    @Test
    public void clickingNext() {

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.CBT_ID);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class).create().start();
        Activity activity = (Activity) controller.get();

        Button button = activity.findViewById(R.id.button_prepare);
        button.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(null));

        ShadowApplication application = Shadows.shadowOf(activity.getApplication());
        application.grantPermissions(permissions);
        controller.resume();

        button.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Take a photo of the compartment bag"));

    }

}
