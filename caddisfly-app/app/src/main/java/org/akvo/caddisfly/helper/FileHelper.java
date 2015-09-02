package org.akvo.caddisfly.helper;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;

public class FileHelper {

    /**
     * The user created configuration file name
     */
    // Files
    private static final String CONFIG_FILE = "tests.json";
    // Folders
    private static final String DIR_APK = "apk"; // App upgrades
    private static final String DIR_CALIBRATION = "Akvo Caddisfly/calibration"; // Calibration files
    private static final String DIR_CONFIG = "Akvo Caddisfly/config"; // Calibration files
    private static final String DIR_IMAGE = "Akvo Caddisfly/image"; // Calibration files


    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type FileType to determine the type of resource attempting to use.
     * @return File representing the root directory for the given FileType.
     */
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
            case APK:
                path = FileUtil.getFilesStorageDir(true) + File.separator + DIR_APK;
                break;
            case CALIBRATION:
                path = FileUtil.getFilesStorageDir(false) + File.separator + DIR_CALIBRATION;
                break;
            case CONFIG:
                path = FileUtil.getFilesStorageDir(false) + File.separator + DIR_CONFIG;
                break;
            case IMAGE:
                path = FileUtil.getFilesStorageDir(false) + File.separator + DIR_IMAGE;
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
     * Loads the tests from the json config file.
     * <p/>
     * Looks for the user created json file. If not found loads the internal json config file
     *
     * @return json configuration text
     */
    public static String getConfigJson() {

        File file = new File(getFilesDir(FileType.CONFIG), CONFIG_FILE);
        String text;

        //Look for external json config file otherwise use the internal default one
        if (file.exists()) {
            text = FileUtil.loadTextFromFile(file);
        } else {
            text = FileUtil.readRawTextFile(CaddisflyApp.getApp(), R.raw.tests_config);
        }

        return text;
    }

    /**
     * The different types of files
     */
    public enum FileType {
        APK, CALIBRATION, IMAGE, CONFIG
    }
}
