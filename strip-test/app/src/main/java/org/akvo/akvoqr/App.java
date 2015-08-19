package org.akvo.akvoqr;

import android.app.Application;
import android.content.Context;

/**
 * Created by linda on 8/19/15.
 */
public class App extends Application {

    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getMyApplicationContext()
    {
        return context;
    }
}
