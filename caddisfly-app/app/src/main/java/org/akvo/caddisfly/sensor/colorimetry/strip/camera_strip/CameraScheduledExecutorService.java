package org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by linda on 12/13/15.
 */
class CameraScheduledExecutorService {

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5, Executors.defaultThreadFactory());

    private Map<String, ScheduledFuture> tasks = new HashMap<>();

    /*
    * Schedule a runnable with an initial delay in milliseconds
    */
    public void scheduleRunnable(Runnable runnable, long initialDelay) {
        try {
            if (!scheduledExecutorService.isShutdown()) {

               scheduledExecutorService.schedule(runnable, initialDelay, TimeUnit.MILLISECONDS);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void cancelTasks(long delay)
    {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                for (String s : tasks.keySet()) {
                    boolean canceled = tasks.get(s).cancel(true);
                    if (canceled)
                        tasks.remove(s);

                    System.out.println("***task key = " + s + " is canceled: " + canceled);
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /*
        * Schedule a runnable with a delay in milliseconds
        * This method schedules a task to be executed periodically.
        * The task is executed the first time after the initialDelay, and then recurringly every time the period expires.
         */
    public void scheduleRunnableWithFixedDelay(Runnable runnable, long initialDelay, long delay)
    {
        try {
            if (!scheduledExecutorService.isShutdown()) {

                ScheduledFuture taskExecuted =  scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);
                tasks.put(runnable.toString(), taskExecuted);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void shutdown()
    {
        scheduledExecutorService.shutdown();
    }
}