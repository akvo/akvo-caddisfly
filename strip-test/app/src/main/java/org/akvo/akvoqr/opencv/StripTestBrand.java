package org.akvo.akvoqr.opencv;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 8/19/15.
 */
public class StripTestBrand {

    public static enum brand{
            AQUACHECK };

    private double stripLenght;
    private double stripHeight;
    private List<Patch> patches = new ArrayList<>();

    public StripTestBrand(brand brandEnum)
    {
        switch (brandEnum)
        {
            case AQUACHECK:
                this.stripHeight = 5; //mm
                this.stripLenght = 83.1; //mm
                patches.add(new Patch(0,8,8,3.3));
                patches.add(new Patch(1,8,8,17.2));
                break;
        }
    }

    public List<Patch> getPatches() {
        return patches;
    }

    public double getStripHeight() {
        return stripHeight;
    }

    public double getStripLenght() {
        return stripLenght;
    }

    public class Patch{
        int order;
        double width; //mm
        double height;//mm
        double position;//x in mm

        public Patch(int order, double width, double height, double position)
        {
            this.order = order;
            this.width = width;
            this.height = height;
            this.position = position;
        }
    }
}
