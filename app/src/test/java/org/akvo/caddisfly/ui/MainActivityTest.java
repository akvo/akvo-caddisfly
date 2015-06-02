package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.ColorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("unused")
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    @Test
    public void titleIsCorrect() {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity.getTitle().toString().equals("Akvo Caddisfly"));
    }

//    @Test
//    public void startSettingsActivity() throws Exception {
//        Activity activity = Robolectric.setupActivity(MainActivity.class);
//        shadowOf(activity).clickMenuItem(R.id.action_settings);
//
//        Intent expectedIntent = new Intent(activity, SettingsActivity.class);
//        assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
//    }

    @Test
    public void testGetPpmValue() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = -1;
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        //ArrayList<ResultRange> arrayList = new ArrayList<>();
        TestInfo testInfo = new TestInfo(null, "" , "" , 0);
        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(-1.0, bundle.getDouble("resultValue"));
    }

    @Test
    public void testGetPpmValue2() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            if (i > 1500) {
                colors[i] = 16742460;
            } else {
                colors[i] = -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "" , "" , 0);
        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(-1, bundle.getInt("resultColor"));
    }

    @Test
    public void testGetPpmValue3() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            if (i > 1000) {
                colors[i] = 16742460;
            } else {
                colors[i] = -1;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "" , "" , 0);
        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        assertEquals(16742460, bundle.getInt("resultColor"));
    }

    @Test
    public void testGetPpmValue4() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(250, 171, 130);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "" , "" , 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 87, 181)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(255, 124, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(255, 146, 139)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(250, 171, 130)));
        testInfo.addRange(new ResultRange(2, Color.rgb(245, 185, 122)));


        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        //assertEquals(16742460, bundle.getInt("resultColor"));
        assertEquals(null, bundle);
    }

    @Test
    public void testGetPpmValue5() throws Exception {
        int[] colors = new int[2500];
        for (int i = 0; i < 2500; i++) {
            colors[i] = Color.rgb(255, 0, 38);
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, 50, 50, Bitmap.Config.ARGB_8888);
        TestInfo testInfo = new TestInfo(null, "" , "" , 0);
        testInfo.addRange(new ResultRange(0, Color.rgb(255, 88, 177)));
        testInfo.addRange(new ResultRange(0.5, Color.rgb(254, 101, 157)));
        testInfo.addRange(new ResultRange(1, Color.rgb(254, 115, 138)));
        testInfo.addRange(new ResultRange(1.5, Color.rgb(254, 128, 119)));
        testInfo.addRange(new ResultRange(2, Color.rgb(254, 142, 99)));

        Bundle bundle = ColorUtils.getPpmValue(bitmap, testInfo, 50);
        //assertEquals(16742460, bundle.getInt("resultColor"));
        assertEquals(null, bundle);
    }


}