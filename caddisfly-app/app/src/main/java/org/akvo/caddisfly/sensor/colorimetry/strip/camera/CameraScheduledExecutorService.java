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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by linda on 12/13/15
 */
class CameraScheduledExecutorService {

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5, Executors.defaultThreadFactory());

    private final Map<String, ScheduledFuture> tasks = new HashMap<>();

    /*
    * Schedule a runnable with an initial delay in milliseconds
    */
    void scheduleRunnable(Runnable runnable, long initialDelay) {
        try {
            if (!scheduledExecutorService.isShutdown()) {

                scheduledExecutorService.schedule(runnable, initialDelay, TimeUnit.MILLISECONDS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void cancelTasks(long delay) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                for (String s : tasks.keySet()) {
                    boolean canceled = tasks.get(s).cancel(true);
                    if (canceled) {
                        tasks.remove(s);
                    }

                    //System.out.println("***task key = " + s + " is canceled: " + canceled);
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /*
        * This method schedules a task to be executed periodically.
        * The task is executed the first time after the initialDelay, and then repeats every time the period expires.
    */
    @SuppressWarnings("SameParameterValue")
    void scheduleRunnableWithFixedDelay(Runnable runnable, long initialDelay, long delay) {
        try {
            if (!scheduledExecutorService.isShutdown()) {

                ScheduledFuture taskExecuted = scheduledExecutorService.scheduleWithFixedDelay(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);
                tasks.put(runnable.toString(), taskExecuted);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
