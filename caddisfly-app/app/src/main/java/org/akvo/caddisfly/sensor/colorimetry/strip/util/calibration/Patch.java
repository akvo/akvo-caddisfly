package org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration;

/**
 * Created by linda on 7/28/15.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Patch {
    public int x;
    public int y;
    public int d;
    public float red;
    public float green;
    public float blue;

    public Patch(float blue, float green, float red, int x, int y, int d) {

        this.blue = blue;
        this.green = green;
        this.red = red;
        this.x = x;
        this.y = y;
        this.d = d;

        System.out.println("***blue: " + blue + " green: " + green + " red: " + red);
    }
}
