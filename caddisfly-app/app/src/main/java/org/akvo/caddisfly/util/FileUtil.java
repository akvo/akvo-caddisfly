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

package org.akvo.caddisfly.util;

import android.content.Context;
import android.os.Environment;

import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Utility functions to file and folder manipulation
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Delete a file
     *
     * @param path     the path to the file
     * @param fileName the name of the file to delete
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean deleteFile(File path, String fileName) {
        File file = new File(path, fileName);
        return file.delete();
    }

    /**
     * Get the root of the files storage directory, depending on the resource being app internal
     * (not concerning the user) or not (users might need to pull the resource from the storage).
     *
     * @param internal true for app specific resources, false otherwise
     * @return The root directory for this kind of resources
     */
    @SuppressWarnings("SameParameterValue")
    public static String getFilesStorageDir(Context context, boolean internal) {
        if (internal) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File path = context.getExternalFilesDir(null);
                if (path == null) {
                    return context.getFilesDir().getAbsolutePath();
                } else {
                    return path.getAbsolutePath();
                }
            } else {
                return CaddisflyApp.getApp().getFilesDir().getAbsolutePath();
            }
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void saveToFile(File folder, String name, String data) {

        File file = new File(folder, name);

        PrintWriter pw = null;
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            pw = new PrintWriter(w);
            pw.write(data);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * Read the text from a file
     *
     * @param file the file to read text from
     * @return the loaded text
     */
    public static String loadTextFromFile(File file) {

        if (file.exists()) {

            InputStreamReader isr = null;
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, StandardCharsets.UTF_8);

                StringBuilder stringBuilder = new StringBuilder();

                int i;
                while ((i = isr.read()) != -1) {
                    stringBuilder.append((char) i);
                }
                return stringBuilder.toString();

            } catch (IOException ignored) {
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return "";
    }

    /**
     * Load lines of strings from a file
     *
     * @param path     the path to the file
     * @param fileName the file name
     * @return an list of string lines
     */
    public static ArrayList<String> loadFromFile(File path, String fileName) {

        ArrayList<String> arrayList = new ArrayList<>();
        if (path.exists()) {

            File file = new File(path, fileName);

            BufferedReader bufferedReader = null;
            InputStreamReader isr = null;
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                bufferedReader = new BufferedReader(isr);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    arrayList.add(line);
                }

                return arrayList;

            } catch (IOException ignored) {
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }
}
