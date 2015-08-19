package org.akvo.akvoqr.opencv;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 8/19/15.
 */
public class StripTestBrand {

    public static enum brand{
            AQUACHECK };

    private List<Patch> patches = new ArrayList<>();

    public StripTestBrand(brand brandEnum)
    {
        switch (brandEnum)
        {
            case AQUACHECK:
                patches.add(new Patch(0,8,8,new double[]{4,4}));
                patches.add(new Patch(1,8,8,new double[]{20,4}));
        }
    }

    public List<Patch> getPatches() {
        return patches;
    }

    public class Patch{
        int order;
        double width; //mm
        double height;//mm
        double[] center;//x,y in mm

        public Patch(int order, double width, double height, double[] center)
        {
            this.order = order;
            this.width = width;
            this.height = height;
            this.center = center;
        }
    }
}
