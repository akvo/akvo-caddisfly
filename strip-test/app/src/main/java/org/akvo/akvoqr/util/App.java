package org.akvo.akvoqr.util;

import android.app.Application;
import android.content.Intent;

/**
 * Created by linda on 8/19/15.
 */
public class App extends Application {

   // private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //context = this;

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        String error = "";
        for(StackTraceElement ste: e.getStackTrace())
        {
           error += ste.getFileName() + " " + ste.getClassName() + " " + ste.getMethodName() + " line no: " + ste.getLineNumber() + "\n";
        }
        error += e.getMessage();

        Intent intent = new Intent ();
        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
        intent.putExtra(Constant.ERROR, error);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);

        System.exit(1); // kill off the crashed app
    }


//    public static Context getMyApplicationContext()
//    {
//        return context;
//    }
}
