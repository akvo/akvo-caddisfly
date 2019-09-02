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
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.sensor.cbt.CbtActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class CbtTest {

    @Test
    public void titleIsCorrect() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create();
        controller.start();

        Activity activity = (Activity) controller.get();
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Water - E.coli");
    }

    @Test
    public void clickingInstructions() {

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create();
        controller.start();

        Activity activity = (Activity) controller.get();
        Button button = activity.findViewById(R.id.button_phase_2);

        button.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent = shadowOf(activity).getNextStartedActivity();
        assertNull(nextIntent);

//        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(null));

        ShadowApplication application = shadowOf(activity.getApplication());
        application.grantPermissions(permissions);
        controller.resume();

        button.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent2 = shadowOf(activity).getNextStartedActivity();
        if (nextIntent2.getComponent() != null) {
            assertEquals(CbtActivity.class.getCanonicalName(),
                    nextIntent2.getComponent().getClassName());
        }

//        ViewPager viewPager = activity.findViewById(R.id.viewPager);
//        assertNotNull(viewPager);
//        assertEquals(viewPager.getVisibility(), View.VISIBLE);

    }

    @Test
    public void clickingNext() {

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create().start();
        Activity activity = (Activity) controller.get();

        Button button = activity.findViewById(R.id.button_prepare);
        button.performClick();

        Intent nextIntent = shadowOf(activity).getNextStartedActivity();

        assertNull(nextIntent);

//        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(null));

        ShadowApplication application = shadowOf(activity.getApplication());
        application.grantPermissions(permissions);
        controller.resume();

        button.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent2 = shadowOf(activity).getNextStartedActivity();
        if (nextIntent2.getComponent() != null) {
            assertEquals(CbtActivity.class.getCanonicalName(),
                    nextIntent2.getComponent().getClassName());
        }

//        CountDownLatch latch = new CountDownLatch(1);
//        try {
//            latch.await(1, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Take a photo of the compartment bag"));

    }
}
