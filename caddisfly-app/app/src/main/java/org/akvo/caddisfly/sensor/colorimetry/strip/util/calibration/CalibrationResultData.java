/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.util.calibration;

import org.opencv.core.Mat;

/**
 * Created by markwestra on 01/08/2015
 */
public class CalibrationResultData {
    public final Mat calibratedImage;
    public final double meanE94;
    public final double maxE94;
    public final double totalE94;

    public CalibrationResultData(Mat img, double meanE94, double maxE94, double totalE94) {
        this.calibratedImage = img;
        this.meanE94 = meanE94;
        this.maxE94 = maxE94;
        this.totalE94 = totalE94;
    }
}
