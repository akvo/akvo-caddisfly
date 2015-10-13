package org.akvo.akvoqr.calibration;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by markwestra on 01/08/2015.
 */
public class CalibrationData {
    public int hsizePixel;
    public int vsizePixel;
    public double hfac;
    public double vfac;
    public String cardVersion;
    public String date;
    public String unit;
    public Double patchSize;
    public Double hsize;
    public Double vsize;
    public Map<String,Location> locations;
    public Map<String,CalValue> calValues;
    public List<WhiteLine> whiteLines;
    public Double[] stripArea;

    public CalibrationData(){
        this.locations = new HashMap<String,Location>();
        this.calValues = new HashMap<String,CalValue>();
        this.whiteLines = new ArrayList<WhiteLine>();
        this.stripArea = new Double[4];
    }

    public class Location {
        public Double x;
        public Double y;
        public Boolean grayPatch;

        public Location(Double x, Double y, Boolean grayPatch){
            this.x = x;
            this.y = y;
            this.grayPatch = grayPatch;
        }
    }

    public class CalValue {
        public int R;
        public int G;
        public int B;

        public CalValue(int R, int G, int B){
            this.R = R;
            this.G = G;
            this.B = B;

        }
    }

    public class WhiteLine {
        public Double[] p;
        public Double width;

        public WhiteLine(Double x1, Double y1, Double x2, Double y2, Double width){
            Double[] pArray = new Double[4];
            pArray[0] = x1;
            pArray[1] = y1;
            pArray[2] = x2;
            pArray[3] = y2;
            this.p = pArray;
            this.width = width;
        }
    }

    public void addLocation(String label, Double x, Double y, Boolean grayPatch){
        Location loc = new Location(x,y,grayPatch);
        this.locations.put(label,loc);
    }

    public void addCal(String label, int R, int G, int B){
        CalValue calVal = new CalValue(R,G,B);
        this.calValues.put(label,calVal);
    }

    public void addWhiteLine(Double x1, Double y1, Double x2, Double y2, Double width){
        WhiteLine line = new WhiteLine(x1,y1,x2,y2,width);
        this.whiteLines.add(line);
    }
}
