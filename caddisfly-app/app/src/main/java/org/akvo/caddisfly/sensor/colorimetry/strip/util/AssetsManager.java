package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import android.content.res.AssetManager;

import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linda on 8/19/15
 */
public class AssetsManager {

    private static AssetsManager assetsManager;
    private final AssetManager manager;

    private AssetsManager() {
        this.manager = CaddisflyApp.getApp().getApplicationContext().getAssets();
    }

    public static AssetsManager getInstance() {
        if (assetsManager == null)
            assetsManager = new AssetsManager();

        return assetsManager;
    }

    public String loadJSONFromAsset(String fileName) {
        String json;
        try {
            if (manager == null)
                return null;

            InputStream is = manager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
