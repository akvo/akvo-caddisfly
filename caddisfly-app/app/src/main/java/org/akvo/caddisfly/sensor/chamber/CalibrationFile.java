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

package org.akvo.caddisfly.sensor.chamber;


import android.content.Context;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;

public class CalibrationFile {

    /**
     * Save a single calibrated color.
     *
     * @param calibration The calibration object
     * @param resultColor The color value
     */
    public static void saveCalibratedData(Context context, TestInfo testInfo,
                                          Calibration calibration, final int resultColor) {

        if (resultColor != 0) {
            calibration.color = resultColor;
        }

        //Save a backup of the calibration details
        final String calibrationDetails = SwatchHelper.generateCalibrationFile(context, testInfo, true);

        final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, testInfo.getUuid());

        FileUtil.saveToFile(path, "_AutoBackup", calibrationDetails);

        Toast.makeText(context, R.string.calibrated, Toast.LENGTH_LONG).show();
    }

}
