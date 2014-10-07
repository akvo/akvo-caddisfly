/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.util.NetworkUtils;
import org.apache.http.Header;

import java.io.File;

public class VideoActivity extends Activity {

    private boolean downloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        File sdDir = this.getExternalFilesDir(null);
        final File videoFile = new File(sdDir, "training.mp4");

        if (videoFile.exists()) {
            playVideo(videoFile);
        } else {

            if (NetworkUtils.checkInternetConnection(this)) {
                progressBar.setVisibility(View.VISIBLE);
                downloading = true;
                AsyncHttpClient client = new AsyncHttpClient();
                client.get("http://caddisfly.ternup.com/akvoapp/caddisfly-training.mp4", new FileAsyncHttpResponseHandler(videoFile) {
                    @Override
                    public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, File response) {
                        playVideo(response);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                        //int progressPercentage = (int)100*bytesWritten/totalSize;
                        progressBar.setMax(totalSize);
                        progressBar.setProgress(bytesWritten);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        progressBar.setVisibility(View.GONE);
                        downloading = false;
                    }
                });
            }
        }


    }

    private void playVideo(File videoFile) {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        final VideoView videoHolder = (VideoView) this.findViewById(R.id.video_player_view);
        videoHolder.setMinimumWidth(width);
        videoHolder.setMinimumHeight(height);

        //getWindow().setFormat(PixelFormat.TRANSLUCENT);
        videoHolder.setMediaController(new MediaController(this));
        videoHolder.setVideoPath(videoFile.getAbsolutePath());
        videoHolder.requestFocus();
        videoHolder.start();

    }

    @Override
    public void onBackPressed() {
        if (!downloading) {
            super.onBackPressed();
        }

    }
}
