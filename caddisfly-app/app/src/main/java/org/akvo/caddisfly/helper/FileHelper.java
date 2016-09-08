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

package org.akvo.caddisfly.helper;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;

public class FileHelper {

    /**
     * The user created configuration file name
     */
    // Folders
    private static final String DIR_CALIBRATION = "Akvo Caddisfly/calibration"; // Calibration files
    private static final String DIR_CONFIG = "Akvo Caddisfly/config"; // Calibration files
    private static final String DIR_DOWNLOAD = "Download/Install"; // Calibration files
    private static final String DIR_IMAGE = "Akvo Caddisfly/image"; // Calibration files
    private static final String FLUORIDE_IMAGE = "Akvo Caddisfly/Fluoride"; // Calibration files
    private static final String TURBIDITY_IMAGE = "Akvo Caddisfly/Turbidity"; // Calibration files

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type FileType to determine the type of resource attempting to use.
     * @return File representing the root directory for the given FileType.
     */
    @SuppressWarnings("SameParameterValue")
    public static File getFilesDir(FileType type) {
        return getFilesDir(type, "");
    }

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type    FileType to determine the type of resource attempting to use.
     * @param subPath a sub directory to be created
     * @return File representing the root directory for the given FileType.
     */
    public static File getFilesDir(FileType type, String subPath) {
        String path = null;
        switch (type) {
            case CALIBRATION:
                path = FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + File.separator + DIR_CALIBRATION;
                break;
            case CONFIG:
                path = FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + File.separator + DIR_CONFIG;
                break;
            case DOWNLOAD:
                path = FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), true) + File.separator + DIR_DOWNLOAD;
                break;
            case IMAGE:
                path = FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + File.separator + DIR_IMAGE;
                break;
        }
        File dir = new File(path);
        if (!subPath.isEmpty()) {
            dir = new File(dir, subPath);
        }
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * The different types of files
     */
    public enum FileType {
        CALIBRATION, CONFIG, DOWNLOAD, IMAGE
    }

}
