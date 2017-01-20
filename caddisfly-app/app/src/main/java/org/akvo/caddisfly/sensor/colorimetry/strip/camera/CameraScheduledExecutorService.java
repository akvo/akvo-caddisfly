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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.util.Log;

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

    private static final String TAG = "CamScheduledExecService";

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
            Log.e(TAG, e.getMessage(), e);
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
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a task to be executed periodically
     *
     * @param runnable     the runnable to execute
     * @param initialDelay the initial delay before executing the first time
     * @param delay        the delay between subsequent execution
     */
    @SuppressWarnings("SameParameterValue")
    void scheduleRunnableWithFixedDelay(Runnable runnable, long initialDelay, long delay) {
        try {
            if (!scheduledExecutorService.isShutdown()) {

                ScheduledFuture taskExecuted = scheduledExecutorService.scheduleWithFixedDelay(runnable,
                        initialDelay, delay, TimeUnit.MILLISECONDS);
                tasks.put(runnable.toString(), taskExecuted);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
