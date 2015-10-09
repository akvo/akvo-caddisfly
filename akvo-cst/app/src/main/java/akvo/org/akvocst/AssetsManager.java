package akvo.org.akvocst;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linda on 8/19/15.
 */
public class AssetsManager {

    private static AssetsManager assetsManager;
    private AssetManager manager;
    public static AssetsManager getInstance(Context context)
    {
        if(assetsManager==null)
            assetsManager = new AssetsManager(context);

        return assetsManager;
    }
    private AssetsManager(Context context)
    {
        this.manager = context.getAssets();
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
