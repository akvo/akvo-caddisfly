/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.RawRes;
import android.util.Log;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Utility functions to file and folder manipulation
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Delete a file
     *
     * @param path     the path to the file
     * @param fileName the name of the file to delete
     */
    public static void deleteFile(File path, String fileName) {
        File file = new File(path, fileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Get the root of the files storage directory, depending on the resource being app internal
     * (not concerning the user) or not (users might need to pull the resource from the storage).
     *
     * @param internal true for app specific resources, false otherwise
     * @return The root directory for this kind of resources
     */
    public static String getFilesStorageDir(boolean internal) {
        if (internal) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File path = CaddisflyApp.getApp().getExternalFilesDir(null);
                if (path == null) {
                    return CaddisflyApp.getApp().getFilesDir().getAbsolutePath();
                } else {
                    return path.getAbsolutePath();
                }
            } else
                return CaddisflyApp.getApp().getFilesDir().getAbsolutePath();
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void saveToFile(File folder, String name, String data) {
        try {

            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdirs();
            }

            File file = new File(folder, name);
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            FileWriter filewriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(data);

            out.close();
            filewriter.close();
        } catch (Exception e) {
            Log.d("failed to save file", e.toString());
        }
    }

    /**
     * Read the text from a file
     *
     * @param file the file to read text from
     * @return the loaded text
     */
    public static String loadTextFromFile(File file) {

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Load lines of strings from a file
     *
     * @param fileName the file name
     * @return an list of string lines
     */
    public static ArrayList<String> loadFromFile(String fileName) {

        try {
            ArrayList<String> arrayList = new ArrayList<>();
            File folder = AppConfig.getFilesDir(AppConfig.FileType.CALIBRATION);
            if (folder.exists()) {

                File file = new File(folder, fileName);

                FileReader filereader = new FileReader(file);

                BufferedReader in = new BufferedReader(filereader);
                String line;
                while ((line = in.readLine()) != null) {
                    arrayList.add(line);
                }
                in.close();
                filereader.close();
            }
            return arrayList;
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Read a text file from the raw resources folder
     *
     * @param context    the context
     * @param resourceId the resource Id
     * @return the read text
     */
    @SuppressWarnings("SameParameterValue")
    public static String readRawTextFile(Context context, @RawRes int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    /**
     * Delete all files in a path
     *
     * @param path the path
     */
    public static void deleteFiles(String path) {

        File file = new File(path);

        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException ignored) {
            }
        }
    }

}
