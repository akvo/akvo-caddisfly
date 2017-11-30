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

package org.akvo.caddisfly.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.Constants;
import org.akvo.caddisfly.helper.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by linda on 8/19/15
 */
public final class AssetsManager {

    private static AssetsManager assetsManager;
    private final AssetManager manager;

    private String json;
    private String experimentalJson;
    private String customJson;

    public AssetsManager() {
        this.manager = CaddisflyApp.getApp().getApplicationContext().getAssets();

        json = loadJSONFromAsset(Constants.TESTS_META_FILENAME);

//        experimentalJson = loadJSONFromAsset("experimental_tests.json");

//        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.EXP_CONFIG),
//                Constants.TESTS_META_FILENAME);
//        experimentalJson = FileUtil.loadTextFromFile(file);

        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG),
                Constants.TESTS_META_FILENAME);
        customJson = FileUtil.loadTextFromFile(file);

    }

    public static AssetsManager getInstance() {
        if (assetsManager == null) {
            assetsManager = new AssetsManager();
        }

        return assetsManager;
    }

    public static Drawable getImage(Context context, String imageName) {

        String path = context.getResources().getString(R.string.instruction_images);
        try {

            InputStream ims = context.getAssets().open(path + "/" + imageName.toLowerCase(Locale.US) + ".webp");

            return Drawable.createFromStream(ims, null);

        } catch (IOException ignored) {
        }
        return null;
    }

//    /**
//     * Read file from asset directory
//     *
//     * @param context  current activity
//     * @param fileName file to read
//     * @return content of the file, string format
//     */
//    private String readFromAsset(final Context context, final String fileName) {
//        String text = "";
//        try {
//            InputStream is = context.getAssets().open(fileName);
//
//            int size = is.available();
//
//            // Read the entire asset into a local byte buffer.
//            byte[] buffer = new byte[size];
//            //noinspection ResultOfMethodCallIgnored
//            is.read(buffer);
//            is.close();
//            text = new String(buffer, "UTF-8");
//        } catch (IOException e) {
//            Timber.e(e);
//        }
//        return text;
//    }

    public String loadJSONFromAsset(String fileName) {
        String json;
        InputStream is = null;
        try {
            if (manager == null) {
                return null;
            }

            is = manager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Timber.e(ex);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Timber.e(e);
                }
            }
        }
        return json;
    }

    public String getJson() {
        return json;
    }

    public String getExperimentalJson() {
        return experimentalJson;
    }

    public String getCustomJson() {
        return customJson;
    }

}
