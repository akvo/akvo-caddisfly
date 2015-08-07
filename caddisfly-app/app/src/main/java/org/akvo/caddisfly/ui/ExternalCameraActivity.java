/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.encoder.MediaMuxerWrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExternalCameraActivity extends AppCompatActivity {
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_camera);

        FragmentManager mgr = getFragmentManager();
        final ExternalCameraFragment fragment = (ExternalCameraFragment) mgr.findFragmentById(R.id.fragment);

        if (wakeLock == null || !wakeLock.isHeld()) {
            PowerManager pm = (PowerManager) getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            wakeLock = pm
                    .newWakeLock(PowerManager.FULL_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "ExternalCameraWakeLock");
            wakeLock.acquire();
        }

        fragment.pictureCallback = new ExternalCameraFragment.PictureCallback() {
            @Override
            public void onPictureTaken(Bitmap bitmap) {
                try {
                    // get buffered output stream for saving a captured still image as a file on external storage.
                    // the file name is came from current time.
                    // You should use extension name as same as CompressFormat when calling Bitmap#compress.
                    final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png");
                    assert outputFile != null;
                    final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
                    //noinspection TryFinallyCanBeTryWithResources
                    try {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                            os.flush();
                        } catch (final IOException ignored) {
                        }
                    } finally {
                        os.close();
                    }
                } catch (final IOException ignored) {
                }
                if (fragment.hasTestCompleted()) {
                    finish();
                }
            }
        };

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_external_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

    }
}
