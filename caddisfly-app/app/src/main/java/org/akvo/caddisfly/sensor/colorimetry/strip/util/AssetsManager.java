package org.akvo.caddisfly.sensor.colorimetry.strip.util;

import android.content.res.AssetManager;

import org.akvo.caddisfly.app.CaddisflyApp;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linda on 8/19/15.
 */
public class AssetsManager {

    private static AssetsManager assetsManager;
    private AssetManager manager;
    public static AssetsManager getInstance()
    {
        if(assetsManager==null)
            assetsManager = new AssetsManager();

        return assetsManager;
    }
    private AssetsManager()
    {
        this.manager = CaddisflyApp.getApp().getApplicationContext().getAssets();
    }


    public String loadJSONFromAsset(String fname) {
        String json;
        try {
            if(manager==null)
                return null;

            InputStream is = manager.open(fname);
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
