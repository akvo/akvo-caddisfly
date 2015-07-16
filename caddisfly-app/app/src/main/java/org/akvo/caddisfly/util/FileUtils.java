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
import android.util.Log;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.model.TestInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public final class FileUtils {

    private FileUtils() {
    }

    public static void deleteFile(String folder, String fileName) {
        File external = Environment.getExternalStorageDirectory();
        String path = external.getPath() + folder;
        File file = new File(path + fileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public static void saveToFile(String path, String name, String data) {
        try {

            File folder = new File(path);
            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdirs();
            }

            File file = new File(path + name);
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

    public static String loadTextFromFile(String filename) {

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
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

    public static ArrayList<String> loadFromFile(TestInfo testInfo, String name) {
        try {
            File external = Environment.getExternalStorageDirectory();
            String path = external.getPath() + Config.CALIBRATE_FOLDER_NAME;
            ArrayList<String> arrayList = new ArrayList<>();
            boolean oldVersion = false;

            File folder = new File(path);
            if (folder.exists()) {

                File file = new File(path + name);

                FileReader filereader = new FileReader(file);

                BufferedReader in = new BufferedReader(filereader);
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.length() > 20 || line.contains("[")) {
                        oldVersion = true;
                        arrayList = new ArrayList<>(Arrays.asList(line.substring(1, line.length() - 1).split(",\\s*")));
                        break;
                    } else {
                        arrayList.add(line);
                    }
                }
                in.close();
                filereader.close();

                if (oldVersion) {
                    ArrayList<String> newArrayList = new ArrayList<>();
                    int start = (int) (testInfo.getRange(0).getValue() / 0.1);
                    int end = arrayList.size() + start;
                    int index = 0;
                    for (int i = start; i < end; i++) {
                        newArrayList.add(String.format("%.2f=%s", i * 0.1, arrayList.get(index++)));
                    }
                    return newArrayList;
                }

            }
            return arrayList;
        } catch (Exception e) {
            Log.d("failed to load file", e.toString());
        }

        return null;
    }

    @SuppressWarnings("SameParameterValue")
    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

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
}
