package org.akvo.caddisfly.util;

import android.graphics.Color;

import junit.framework.TestCase;

public class ColorUtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        ClassUtils.assertUtilityClassWellDefined(ColorUtils.class);
    }

//    public void testGetPpmValue() throws Exception {
//        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
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