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

package org.akvo.caddisfly.sensor.striptest.utils;

import android.content.res.AssetManager;

import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;

/**
 * Created by linda on 8/19/15
 */
public final class AssetsManager {

    private static AssetsManager assetsManager;
    private final AssetManager manager;

    private AssetsManager() {
        this.manager = CaddisflyApp.getApp().getApplicationContext().getAssets();
    }

    public static AssetsManager getInstance() {
        if (assetsManager == null) {
            assetsManager = new AssetsManager();
        }

        return assetsManager;
    }

    String loadJSONFromAsset(String fileName) {
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

            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
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
}
