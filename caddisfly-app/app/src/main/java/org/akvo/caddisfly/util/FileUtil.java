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
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions to file and folder manipulation
 */
public final class FileUtil {

    private static final String TAG = "FileUtil";

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
            Log.e(TAG, e.getMessage(), e);
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
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
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
    public static List<String> loadFromFile(File path, String fileName) {

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
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }

        return null;
    }


    public static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     * Method to write characters to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     *
     * @return absolute path name of saved file, or empty string on failure.
     */
    @SuppressWarnings("SameParameterValue")
    public static String writeBitmapToExternalStorage(Bitmap bitmap, String dirPath, String fileName) {
        // Find the root of the external storage
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + FileHelper.ROOT_DIRECTORY + dirPath);
        File file = new File(dir, fileName);

        // check if directory exists and if not, create it
        boolean success = true;
        if (!dir.exists()) {
            success = dir.mkdirs();
        }

        if (success && bitmap != null) {
            try {
                FileOutputStream f = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(f);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

                for (byte s : byteArrayOutputStream.toByteArray()) {
                    bos.write(s);
                }
                bos.close();
                byteArrayOutputStream.close();
                f.close();
                return file.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        // on failure, return empty string
        return "";
    }

    public static void writeByteArray(Context context, byte[] data, String fileName) {

        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            for (byte s : data) {
                bos.write(s);
            }
            bos.close();
            outputStream.close();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static byte[] readByteArray(Context context, String fileName) throws IOException {

        byte[] data;
        int c;

        FileInputStream fis = context.openFileInput(fileName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedInputStream bos = new BufferedInputStream(fis);

        while ((c = bos.read()) != -1) {
            byteArrayOutputStream.write(c);
        }

        data = byteArrayOutputStream.toByteArray();

        bos.close();
        byteArrayOutputStream.close();
        fis.close();

        return data;
    }

    public static void writeToInternalStorage(Context context, String fileName, String json) {

        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            for (byte s : json.getBytes(StandardCharsets.UTF_8)) {
                outputStream.write(s);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public static String readFromInternalStorage(Context context, String fileName) {

        File file = new File(context.getFilesDir(), fileName);

        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;

            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            String json = stringBuilder.toString();

            br.close();
            in.close();
            fis.close();

            return json;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public static void deleteFromInternalStorage(Context context, final String contains) throws IOException {
        File file = context.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(contains);
            }
        };
        File[] files = file.listFiles(filter);
        if (files != null) {
            for (File f : files) {
                //noinspection ResultOfMethodCallIgnored
                if (!f.delete()) {
                    throw new IOException("Error while deleting files");
                }
            }
        }
    }

    public static boolean fileExists(Context context, String fileName) {
        return new File(context.getFilesDir() + File.separator + fileName).exists();
    }

    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static byte[] leIntToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }
}
