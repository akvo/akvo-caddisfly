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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.akvo.caddisfly.R
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.repository.TestConfigRepository
import org.akvo.caddisfly.sensor.cbt.CbtActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CbtTest {

    @Test
    fun titleIsCorrect() {

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller = Robolectric.buildActivity(TestActivity::class.java, intent).create()
        controller.start()

        val activity = controller.get() as Activity
        val textView = activity.findViewById<TextView>(R.id.textToolbarTitle)
        assertEquals(textView.text, "Water - E.coli")
    }

    @Test
    fun clickingInstructions() {

        val permissions = arrayOf(Manifest.permission.CAMERA)

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller = Robolectric.buildActivity(TestActivity::class.java, intent).create()
        controller.start()

        val activity = controller.get() as Activity
        val button = activity.findViewById<Button>(R.id.button_phase_2)

        button.performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent = shadowOf(activity).nextStartedActivity
        assertEquals("android.content.pm.action.REQUEST_PERMISSIONS", nextIntent.action)

        //        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(null));

        val application = shadowOf(activity.application)
        application.grantPermissions(*permissions)
        controller.resume()

        button.performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent2 = shadowOf(activity).nextStartedActivity
        if (nextIntent2.component != null) {
            assertEquals(CbtActivity::class.java.canonicalName,
                    nextIntent2.component!!.className)
        }

        //        ViewPager viewPager = activity.findViewById(R.id.viewPager);
        //        assertNotNull(viewPager);
        //        assertEquals(viewPager.getVisibility(), View.VISIBLE);

    }

    @Test
    fun clickingNext() {

        val permissions = arrayOf(Manifest.permission.CAMERA)

        val testConfigRepository = TestConfigRepository()
        val testInfo = testConfigRepository.getTestInfo(Constants.CBT_ID)

        val intent = Intent()
        val data = Bundle()
        data.putParcelable(ConstantKey.TEST_INFO, testInfo)
        intent.putExtras(data)

        val controller = Robolectric.buildActivity(TestActivity::class.java, intent).create().start()
        val activity = controller.get() as Activity

        val button = activity.findViewById<Button>(R.id.button_prepare)
        button.performClick()

        val nextIntent = shadowOf(activity).nextStartedActivity

        assertEquals("android.content.pm.action.REQUEST_PERMISSIONS", nextIntent.action)

        //        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(null));

        val application = shadowOf(activity.application)
        application.grantPermissions(*permissions)
        controller.resume()

        button.performClick()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntent2 = shadowOf(activity).nextStartedActivity
        if (nextIntent2.component != null) {
            assertEquals(CbtActivity::class.java.canonicalName,
                    nextIntent2.component!!.className)
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
