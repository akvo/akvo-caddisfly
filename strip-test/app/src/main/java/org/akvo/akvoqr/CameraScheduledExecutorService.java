package org.akvo.akvoqr;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by linda on 12/13/15.
 */
public class CameraScheduledExecutorService {

    ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);

    public CameraScheduledExecutorService() {

    }

    /*
    * Schedule a runnable with an initial delay in milliseconds
    */
    public void scheduleRunnable(Runnable runnable, long initialDelay)
    {
        if(!scheduledExecutorService.isShutdown())
        scheduledExecutorService.schedule(runnable, initialDelay, TimeUnit.MILLISECONDS);
    }

    /*
    * Schedule a runnable with a delay in milliseconds
    * This method schedules a task to be executed periodically.
    * The task is executed the first time after the initialDelay, and then recurringly every time the period expires.
     */
    public void scheduleRunnableWithFixedDelay(Runnable runnable, long initialDelay, long delay)
    {
        if(!scheduledExecutorService.isShutdown())
        scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);

    }

    public void shutdown()
    {
        scheduledExecutorService.shutdown();
    }
}