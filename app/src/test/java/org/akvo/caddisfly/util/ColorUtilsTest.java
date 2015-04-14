package org.akvo.caddisfly.util;

import android.graphics.Color;
import android.util.Pair;

import junit.framework.TestCase;

import org.akvo.caddisfly.model.ResultRange;
import org.akvo.caddisfly.model.TestInfo;

import java.util.Hashtable;
import java.util.List;

public class ColorUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(ColorUtils.class);
    }

//    public void testGetPpmValue() throws Exception {
//        int[] colors = new int[2500];
//        for (int i = 0; i < 50; i++) {
//            for (int j = 0; j < 50; j++) {
//                colors[i] = -1;
//            }
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(colors, 5, 5, Bitmap.Config.ARGB_8888);
//        ArrayList<ResultRange> arrayList = new ArrayList<>();
//        Bundle bundle = ColorUtils.getPpmValue(bitmap, arrayList, 50);
//        assertEquals(null, bundle);
//    }

    public void testGetDistance() throws Exception {
        double distance = ColorUtils.getDistance(Color.rgb(200, 200, 200), Color.rgb(100, 100, 100));
        assertEquals(173.20508075688772, distance, 0);
    }

    public void testGetColorRgbString() throws Exception {
        String rgb = ColorUtils.getColorRgbString(-13850285);
        assertEquals("44  169  83", rgb);
    }

    public void testAutoGenerateColors() throws Exception {
        Hashtable hashtable = new Hashtable();
        TestInfo testInfo = new TestInfo(hashtable, "FLUOR", "ppm", 0);

        for (int i = 0; i < 5; i++) {
            ResultRange resultRange = new ResultRange(((int) (Double.valueOf(i) * 10)) / 10f, Color.TRANSPARENT);
            testInfo.addRange(resultRange);
        }

        List list = ColorUtils.autoGenerateColors(testInfo);

        assertEquals(400, list.size());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(String.format("FLUOR-%.2f", i * 0.01), ((Pair) list.get(i)).first);
            assertEquals(-16777216, ((Pair) list.get(i)).second);
        }
    }

    public void testGetColorFromRgb() throws Exception {
        int color = ColorUtils.getColorFromRgb("44  169  83");
        assertEquals(-13850285, color);

    }

    public void testGetBrightness() throws Exception {
        int brightness = ColorUtils.getBrightness(Color.rgb(200, 255, 30));
        assertEquals(233, brightness);
    }
}