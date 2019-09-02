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
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.sensor.manual.SwatchSelectTestActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ManualSwatchSelectTest {

    @Test
    public void titleIsCorrect() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create();
        controller.start();

        Activity activity = (Activity) controller.get();
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Water - Chlorine, pH");
    }

    @Test
    public void clickingNext() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create().start();
        Activity activity = (Activity) controller.get();

        Button button = activity.findViewById(R.id.button_prepare);
        button.performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent2 = shadowOf(activity).getNextStartedActivity();
        if (nextIntent2.getComponent() != null) {
            assertEquals(SwatchSelectTestActivity.class.getCanonicalName(),
                    nextIntent2.getComponent().getClassName());
        }
    }

    @Test
    public void selectResults() {

        TestConfigRepository testConfigRepository = new TestConfigRepository();
        TestInfo testInfo = testConfigRepository.getTestInfo(Constants.SWATCH_SELECT_TEST_ID);

        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(ConstantKey.TEST_INFO, testInfo);
        intent.putExtras(data);

        ActivityController controller = Robolectric.buildActivity(SwatchSelectTestActivity.class, intent).create().start();
        Activity activity = (Activity) controller.get();

        for (int i = 0; i < 7; i++) {
            activity.findViewById(R.id.image_pageRight).performClick();
        }

        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(activity.getString(R.string.select_color_intervals), textView.getText());

        SystemClock.sleep(3000);

//        Button button = activity.findViewById(R.id.image_pageRight);
//        button.performClick();

//        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Select the color intervals before continuing"));
    }
}
