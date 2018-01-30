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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.sensor.chamber.ChamberTestActivity;
import org.akvo.caddisfly.util.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;

import java.io.File;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ChamberTest {

    private static final String CADDISFLY_RESOURCE_ID = "caddisflyResourceUuid";
    private static final String CADDISFLY_QUESTION_ID = "questionId";
    private static final String CADDISFLY_QUESTION_TITLE = "questionTitle";
    private static final String CADDISFLY_LANGUAGE = "language";

    public static void saveCalibration(String name) {

        String file = "0.0=255  38  186\n"
                + "0.5=255  51  129\n"
                + "1.0=255  59  89\n"
                + "1.5=255  62  55\n"
                + "2.0=255  81  34\n";

        File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, Constants.FLUORIDE_ID);

        FileUtil.saveToFile(path, name, file);
    }

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(TestListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void testCount() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST);

        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        assertSame(4, recyclerView.getChildCount());

        assertTestTitle(recyclerView, 0, "Water - Chromium");
        assertTestTitle(recyclerView, 1, "Water - Fluoride");
        assertTestTitle(recyclerView, 2, "Water - Free Chlorine");
        assertTestTitle(recyclerView, 3, "Water - Free Chlorine");
    }

    private void assertTestTitle(RecyclerView recyclerView, int index, String title) {
        assertEquals(title,
                ((TestInfoAdapter) recyclerView.getAdapter()).getItemAt(index).getName());
        assertEquals(title,
                ((TextView) recyclerView.getChildAt(index).findViewById(R.id.text_title)).getText());
    }

    @Test
    public void testTitles() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST);
        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            TestInfo testInfo = ((TestInfoAdapter) recyclerView.getAdapter()).getItemAt(i);
            assertTestTitle(recyclerView, i, testInfo.getName());
        }
    }

    @Test
    public void clickTest() {

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.CHAMBER_TEST);

        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        recyclerView.getChildAt(1).performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent = shadowOf(activity).getNextStartedActivity();

        assertNull(nextIntent);

        ShadowApplication application = shadowOf(activity.getApplication());
        application.grantPermissions(permissions);
        controller.resume();

        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);

        recyclerView.getChildAt(1).performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent2 = shadowOf(activity).getNextStartedActivity();
        if (nextIntent2.getComponent() != null) {
            assertEquals(ChamberTestActivity.class.getCanonicalName(),
                    nextIntent2.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(TestListActivity.class);

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

    @Test
    public void textExternalWithoutPermission() {

        Intent intent = new Intent(AppConfig.EXTERNAL_APP_ACTION);

        Bundle data = new Bundle();
        data.putString(CADDISFLY_RESOURCE_ID, Constants.FLUORIDE_ID);
        data.putString(CADDISFLY_QUESTION_ID, "123");
        data.putString(CADDISFLY_QUESTION_TITLE, "Fluoride");
        data.putString(CADDISFLY_LANGUAGE, "en");

        intent.putExtras(data);
        intent.setType("text/plain");

        ActivityController controller = Robolectric.buildActivity(TestActivity.class, intent).create();

        controller.start();

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog sAlert = shadowOf(alert);

        assertEquals(sAlert.getMessage(), "Calibration for Water - Fluoride is incomplete\n" +
                "\n" +
                "Do you want to calibrate now?");

    }
//
//    @Test
//    public void testFromExternalActivity() {
//        assertTrue(testExternalActivity(false));
//        assertTrue(testExternalActivity(true));
//    }
//
//    private boolean testExternalActivity(boolean setCalibration) {
//
//        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//        Intent intent = new Intent(AppConfig.EXTERNAL_APP_ACTION);
//
//        Bundle data = new Bundle();
//        data.putString(CADDISFLY_RESOURCE_ID, SensorConstants.FLUORIDE_ID);
//        data.putString(CADDISFLY_QUESTION_ID, "123");
//        data.putString(CADDISFLY_QUESTION_TITLE, "Fluoride");
//        data.putString(CADDISFLY_LANGUAGE, "en");
//
//        intent.putExtras(data);
//        intent.setType("text/plain");
//
//        ActivityController controller = Robolectric.buildActivity(TestActivity.class).withIntent(intent).create();
//        Activity activity = (Activity) controller.get();
//
//        ShadowApplication application = shadowOf(activity.getApplication());
//        application.grantPermissions(permissions);
//
//        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);
//
//        controller.start();
//
//        if (setCalibration) {
//            controller.stop();
//
//            saveCalibration(" _AutoBackup");
//
//            controller.resume();
//
//            Intent nextIntent = shadowOf(activity).getNextStartedActivity();
//            if (nextIntent.getComponent() != null) {
//                assertEquals(ColorimetryTestActivity.class.getCanonicalName(),
//                        nextIntent.getComponent().getClassName());
//            }
//        } else {
//
//            AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
//            ShadowAlertDialog sAlert = shadowOf(alert);
//
//            assertEquals(sAlert.getTitle().toString(), activity.getString(R.string.cannotStartTest));
//
//            assertTrue(sAlert.getMessage().toString().contains(activity.getString(R.string.doYouWantToCalibrate)));
//        }
//
//        return true;
//    }
//
//    @Test
//    public void testExternalTestStart() {
//
//        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.FLUORIDE_ID);
//
//        Intent intent = new Intent();
//        intent.putExtra("testInfo", new TestInfo());
//        ActivityController controller = Robolectric.buildActivity(ColorimetryTestActivity.class).withIntent(intent).create();
//        Activity activity = (Activity) controller.get();
//
//        ShadowApplication application = shadowOf(activity.getApplication());
//        application.grantPermissions(permissions);
//
//        ShadowPackageManager pm = shadowOf(RuntimeEnvironment.application.getPackageManager());
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
//        pm.setSystemFeature(PackageManager.FEATURE_CAMERA_FLASH, true);
//
//        controller.resume();
//
//        Button button = activity.findViewById(R.id.button_prepare);
//        button.performClick();
//
//        Intent nextIntent = shadowOf(activity).getNextStartedActivity();
//        if (nextIntent.getComponent() != null) {
//            assertEquals(SelectDilutionActivity.class.getCanonicalName(),
//                    nextIntent.getComponent().getClassName());
//        }
//    }
}
