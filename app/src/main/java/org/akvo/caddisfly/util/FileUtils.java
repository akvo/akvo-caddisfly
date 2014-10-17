/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.akvo.caddisfly.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {

    private FileUtils() {
    }

    public static String getStoragePath(Context context, long locationId, String folderName,
                                        boolean create) {

        if (folderName != null && folderName.startsWith(File.separator)) {
            return folderName;
        }

        if (locationId > -1) {
            assert folderName != null;
            if (!folderName.isEmpty()) {
                folderName = locationId + File.separator + folderName;
            } else {
                folderName = String.valueOf(locationId);
            }
        }

        //File sdDir = Environment.getExternalStorageDirectory();

        File sdDir = context.getExternalFilesDir(null);

        File appDir = new File(sdDir, folderName != null ? folderName : "");

        if (!appDir.exists()) {
            if (!create) {
                return "";
            }
            if (!appDir.mkdirs()) {
                return "";
            }
        }

        return appDir.getPath() + File.separator;
    }

    public static void deleteFile(String folder, String fileName) {
        File external = Environment.getExternalStorageDirectory();
        String path = external.getPath() + folder;
        File file = new File(path + fileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

/*    public static void deleteFiles(ArrayList<String> files) {

        if (files != null) {
            for (String file1 : files) {
                File file = new File(file1);
                file.delete();
            }
        }
    }*/

/*    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFolder(Context context, long locationId, String folderName) {

        File file = new File(getStoragePath(context, locationId, folderName, false));
        deleteFolder(file);

    }*/

    public static void deleteFolder(File folder) {

        if (folder.exists()) {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("rm -r " + folder.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Reading file paths from SDCard
/*    public static ArrayList<String> getFilePaths(Context context, String folderName,
                                                 long locationId) {
        return getFilePaths(context, folderName, "", locationId);
    }*/

    // Reading file paths from SDCard
    @SuppressWarnings("SameParameterValue")
    public static ArrayList<String> getFilePaths(Context context, String folderName,
                                                 String subFolder, long locationId) {

        ArrayList<String> filePaths = new ArrayList<String>();

        String folderPath = getStoragePath(context, locationId, folderName, false);

        folderPath += subFolder;

        File directory = new File(folderPath);

        if (directory.isDirectory()) {
            File[] listFiles = directory.listFiles();

            if (listFiles != null && listFiles.length > 0) {

                for (File listFile : listFiles) {

                    if (listFile.isFile()) {
                        String filePath = listFile.getAbsolutePath();

                        //if (IsSupportedFile(filePath)) {
                        filePaths.add(filePath);
                    }
                }
            }
        }

        return filePaths;
    }

/*
    public static void saveToFile(Context context, String path, String name, String data) {
        try {

            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(path + name);
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fileWriter);

            out.write(data);

            out.close();
            fileWriter.close();
        } catch (Exception e) {
            Log.d("failed to save file", e.toString());
        }
    }
*/

    public static ArrayList<String> loadFromFile(String name) {
        try {
            File external = Environment.getExternalStorageDirectory();
            String path = external.getPath() + Config.CALIBRATE_FOLDER_NAME;
            ArrayList<String> arrayList = null;

            File folder = new File(path);
            if (folder.exists()) {

                File file = new File(path + name);

                FileReader filereader = new FileReader(file);

                BufferedReader in = new BufferedReader(filereader);

                String data = in.readLine();
                if (data != null) {
                    arrayList = new ArrayList<String>(Arrays.asList(data.substring(1, data.length() - 1).split(",\\s*")));
                }

                in.close();
                filereader.close();
            }
            return arrayList;
        } catch (Exception e) {
            Log.d("failed to load file", e.toString());
        }

        return null;
    }

    public static void saveText(String fileName, String content) {
        FileOutputStream outputStream;

        File file = new File(fileName);
        try {
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            if (file.createNewFile()) {
                try {
                    outputStream = new FileOutputStream(file);
                    outputStream.write(content.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
