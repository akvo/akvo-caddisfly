package org.akvo.caddisfly.sensor.striptest.utils;

/**
 * Created by markwestra on 13/06/2017
 */
public class ColorUtils {

    private final static float XREF = 95.047f;
    private final static float YREF = 100f;
    private final static float ZREF = 108.883f;
    private final static float eps = 0.008856f; //kE
    private final static float kappa = 903.3f; // kK
    private final static float kappaEps = 8.0f; // kKE

    // gamma corrected RGB scaled [0.255] to linear sRGB scaled [0..1]
    private static float gammaToLinearRGB(float x) {
        float C = x / 255.0f;
        if (C < 0.04045) {
            C /= 12.92f;
        } else {
            C = (float) Math.pow((C + 0.055) / 1.055, 2.4);
        }
        return C;
    }

    // YCbCr D65 to linear sRGB D65
    // according to JPEG standard ITU-T T.871 (05/2011), page 4
    // according to http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // and http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // using inverse sRGB companding
    // same as https://en.wikipedia.org/wiki/SRGB
    // https://www.itu.int/rec/dologin_pub.asp?lang=e&id=T-REC-T.871-201105-I!!PDF-E
    public static float[] YUVtoLinearRGB(float[] YUV) {
        float Rgamma = YUV[0] + 1.402f * YUV[2];
        float Ggamma = YUV[0] - 0.3441f * YUV[1] - 0.7141f * YUV[2];
        float Bgamma = YUV[0] + 1.772f * YUV[1];

        // make linear and clamp
        float Rlinear = Math.min(Math.max(0.0f, gammaToLinearRGB(Rgamma)), 1.0f);
        float Glinear = Math.min(Math.max(0.0f, gammaToLinearRGB(Ggamma)), 1.0f);
        float Blinear = Math.min(Math.max(0.0f, gammaToLinearRGB(Bgamma)), 1.0f);

        return new float[]{Rlinear, Glinear, Blinear};
    }

    // linear sRGB D65 scaled [0..1 ]to XYZ D65 scaled [0 .. 100]
//    public static float[] linearRGBtoXYZ(float[] RGB) {
//
//        // next we apply a transformation matrix to get XYZ D65
//        float[] XYZ = new float[3];
//        XYZ[0] = 0.4124564f * RGB[0] + 0.3575761f * RGB[1] + 0.1804375f * RGB[2];
//        XYZ[1] = 0.2126729f * RGB[0] + 0.7151522f * RGB[1] + 0.0721750f * RGB[2];
//        XYZ[2] = 0.0193339f * RGB[0] + 0.1191920f * RGB[1] + 0.9503041f * RGB[2];
//
//        // and we scale to 0..100
//        XYZ[0] *= 100f;
//        XYZ[1] *= 100f;
//        XYZ[2] *= 100f;
//        return XYZ;
//    }

    // XYZ D65 to gamma-corrected sRGB D65
    // according to http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // and http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // using sRGB gamma companding
    // we assume XYZ is scaled [0..100]
    // RGB is scaled to [0..255] and clamped
    public static int[] XYZtoRGBint(float[] XYZ) {
        float[] XYZscaled = new float[3];
        float[] RGB = new float[3];

        // first we scale to [0..1]
        for (int i = 0; i < 3; i++) {
            XYZscaled[i] = XYZ[i] / 100.0f;
        }

        // next, we apply a matrix:
        RGB[0] = 3.2404542f * XYZscaled[0] - 1.5371385f * XYZscaled[1] - 0.4985314f * XYZscaled[2];
        RGB[1] = -0.9692660f * XYZscaled[0] + 1.8760108f * XYZscaled[1] + 0.0415560f * XYZscaled[2];
        RGB[2] = 0.0556434f * XYZscaled[0] - 0.2040259f * XYZscaled[1] + 1.0572252f * XYZscaled[2];

        // next, we apply gamma encoding
        for (int i = 0; i < 3; i++) {
            if (RGB[i] < 0.0031308) {
                RGB[i] = 12.92f * RGB[i];
            } else {
                RGB[i] = (float) (1.055f * Math.pow(RGB[i], 1 / 2.4)) - 0.055f;
            }
        }

        // next, we scale to [0..255] and clamp
        int[] RGBint = new int[3];
        for (int i = 0; i < 3; i++) {
            RGB[i] *= 255.0f;
            RGBint[i] = Math.min(Math.max(0, Math.round(RGB[i])), 255);
        }
        return RGBint;
    }

    // XYZ D65 scaled [0..100] to LAB D65
    public static float[] XYZtoLAB(float[] XYZ) {
        float xr = XYZ[0] / XREF;
        float yr = XYZ[1] / YREF;
        float zr = XYZ[2] / ZREF;

        float fx, fy, fz;
        if (xr > eps) {
            fx = (float) Math.pow(xr, 1 / 3.0);
        } else {
            fx = (float) ((kappa * xr + 16.0) / 116.0);
        }

        if (yr > eps) {
            fy = (float) Math.pow(yr, 1 / 3.0);
        } else {
            fy = (float) ((kappa * yr + 16.0) / 116.0);
        }

        if (zr > eps) {
            fz = (float) Math.pow(zr, 1 / 3.0);
        } else {
            fz = (float) ((kappa * zr + 16.0) / 116.0);
        }

        float CIEL = (float) (116.0 * fy - 16.0);
        float CIEa = (500 * (fx - fy));
        float CIEb = (200 * (fy - fz));

        return new float[]{CIEL, CIEa, CIEb};
    }

    // Lab D65 to XYZ D65 scaled [0..100]
    // http://www.brucelindbloom.com/Eqn_Lab_to_XYZ.html
    public static float[] Lab2XYZ(float[] lab) {
        float CIEL = lab[0];
        float CIEa = lab[1];
        float CIEb = lab[2];

        float fy = (CIEL + 16.0f) / 116.0f;
        float fx = 0.002f * CIEa + fy;
        float fz = fy - 0.005f * CIEb;

        float fx3 = fx * fx * fx;
        float fz3 = fz * fz * fz;

        float xr = (fx3 > eps) ? fx3 : ((116.0f * fx - 16.0f) / kappa);
        float yr = (float) ((CIEL > kappaEps) ? Math.pow((CIEL + 16.0f) / 116.0f, 3.0f) : (CIEL / kappa));
        float zr = (fz3 > eps) ? fz3 : ((116.0f * fz - 16.0f) / kappa);

        return new float[]{xr * XREF, yr * YREF, zr * ZREF};
    }

    // deltaE2000 colour distance between two lab colour values
    public static float deltaE2000(float[] LabRef, float[] LabTarget) {
        float dhPrime;
        float kL = 1.0f;
        float kC = 1.0f;
        float kH = 1.0f;
        float lBarPrime = 0.5f * (LabRef[0] + LabTarget[0]);
        float c1 = (float) Math.sqrt(LabRef[1] * LabRef[1] + LabRef[2] * LabRef[2]);
        float c2 = (float) Math.sqrt(LabTarget[1] * LabTarget[1] + LabTarget[2] * LabTarget[2]);
        float cBar = 0.5f * (c1 + c2);
        float cBar7 = cBar * cBar * cBar * cBar * cBar * cBar * cBar;
        float g = (float) (0.5 * (1.0 - Math.sqrt(cBar7 / (cBar7 + 6103515625.0))));	/* 6103515625 = 25^7 */
        float a1Prime = LabRef[1] * (1.0f + g);
        float a2Prime = LabTarget[1] * (1.0f + g);
        float c1Prime = (float) Math.sqrt(a1Prime * a1Prime + LabRef[2] * LabRef[2]);
        float c2Prime = (float) Math.sqrt(a2Prime * a2Prime + LabTarget[2] * LabTarget[2]);
        float cBarPrime = 0.5f * (c1Prime + c2Prime);
        float h1Prime = (float) ((Math.atan2(LabRef[2], a1Prime) * 180.0) / Math.PI);
        if (h1Prime < 0.0) {
            h1Prime += 360.0;
        }
        float h2Prime = (float) ((Math.atan2(LabTarget[2], a2Prime) * 180.0) / Math.PI);
        if (h2Prime < 0.0) {
            h2Prime += 360.0;
        }
        float hBarPrime = (Math.abs(h1Prime - h2Prime) > 180.0f) ? (0.5f * (h1Prime + h2Prime + 360.0f)) : (0.5f * (h1Prime + h2Prime));
        float t = (float) (1.0 -
                0.17 * Math.cos(Math.PI * (hBarPrime - 30.0) / 180.0) +
                0.24 * Math.cos(Math.PI * (2.0 * hBarPrime) / 180.0) +
                0.32 * Math.cos(Math.PI * (3.0 * hBarPrime + 6.0) / 180.0) -
                0.20 * Math.cos(Math.PI * (4.0 * hBarPrime - 63.0) / 180.0));
        if (Math.abs(h2Prime - h1Prime) <= 180.0) {
            dhPrime = h2Prime - h1Prime;
        } else {
            dhPrime = (h2Prime <= h1Prime) ? (h2Prime - h1Prime + 360.0f) : (h2Prime - h1Prime - 360.0f);
        }
        float dLPrime = LabTarget[0] - LabRef[0];
        float dCPrime = c2Prime - c1Prime;
        float dHPrime = (float) (2.0 * Math.sqrt(c1Prime * c2Prime) * Math.sin(Math.PI * (0.5 * dhPrime) / 180.0));
        float sL = (float) (1.0 + ((0.015 * (lBarPrime - 50.0) * (lBarPrime - 50.0)) / Math.sqrt(20.0 + (lBarPrime - 50.0) * (lBarPrime - 50.0))));
        float sC = 1.0f + 0.045f * cBarPrime;
        float sH = 1.0f + 0.015f * cBarPrime * t;
        float dTheta = (float) (30.0 * Math.exp(-((hBarPrime - 275.0) / 25.0) * ((hBarPrime - 275.0) / 25.0)));
        float cBarPrime7 = cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime * cBarPrime;
        float rC = (float) (Math.sqrt(cBarPrime7 / (cBarPrime7 + 6103515625.0)));
        float rT = (float) (-2.0 * rC * Math.sin(Math.PI * (2.0 * dTheta) / 180.0));

        // deltaE2000
        return (float) Math.sqrt((dLPrime / (kL * sL)) * (dLPrime / (kL * sL)) +
                (dCPrime / (kC * sC)) * (dCPrime / (kC * sC)) +
                (dHPrime / (kH * sH)) * (dHPrime / (kH * sH)) +
                (dCPrime / (kC * sC)) * (dHPrime / (kH * sH)) * rT);
    }
}
