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

package org.akvo.caddisfly.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

/**
 * DO NOT USE ON YOUR OWN. All calls are handled through UpdateChecker.
 *
 * @author Raghav Sood
 */
class DownloadManager extends AsyncTask<String, Integer, String> {

    private static final int BYTE_ARRAY_LENGTH = 1024;

    private final Context mContext;

    private ProgressDialog progressDialog;

    private boolean installAfterDownload = true;

    private boolean downloaded = false;

    private String mChecksum;

    private File downloadFilePath = null;

    /**
     * DO NOT USE ON YOUR OWN. All calls are handled through UpdateChecker.
     *
     * @param context Activity context
     */
    public DownloadManager(Context context) {
        mContext = context;
        this.installAfterDownload = true;
    }

    //http://stackoverflow.com/questions/13152736/how-to-generate-an-md5-checksum-for-a-file-in-android
    private static String getMD5Checksum(String filePath) {
        String returnVal = "";
        InputStream input = null;
        try {
            input = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest md5Hash = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = input.read(buffer);
                if (numRead > 0) {
                    md5Hash.update(buffer, 0, numRead);
                }
            }
            input.close();

            byte[] md5Bytes = md5Hash.digest();
            for (byte md5Byte : md5Bytes) {
                returnVal += Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
        }
        return returnVal.toUpperCase();
    }

    /**
     * Checks to see if we have an active internet connection
     *
     * @return true if online, false otherwise
     */
    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wipe any existing apk file, and create a new File for the new one, according to the
     * given version
     *
     * @param downloadUrl the url from where the file was downloaded
     * @param version     the apk version
     * @return the new file
     */
    private File createFile(String downloadUrl, String version) {
        //cleanupDownloads(version);

        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
        File directory = new File(FileHelper.getFilesDir(FileHelper.FileType.APK), version);
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdir();
        }

        return new File(directory, fileName);
    }


    /**
     * Downloads the update file in a background task
     */
    @Override
    protected String doInBackground(String... downloadInfo) {
        if (isOnline()) {
            try {

                URL url = new URL(downloadInfo[0]);
                mChecksum = downloadInfo[1];
                String version = downloadInfo[2];

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(60000);
                connection.connect();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());

                downloadFilePath = createFile(url.getPath(), version);

                OutputStream output = new FileOutputStream(downloadFilePath);

                // this will be useful so that you can show a typical 0-100% progress bar
                // todo:fix content length problem
                int fileLength = connection.getContentLength();
                if (fileLength < 100) {
                    //could not determine the correct file size so use a typical file size
                    fileLength = AppConfig.UPDATE_FILE_TYPICAL_SIZE;
                }

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    byte[] data = new byte[BYTE_ARRAY_LENGTH];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        //publishing the progress....
                        publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);

                        if (isCancelled()) {
                            if (progressDialog != null) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                progressDialog = null;
                            }
                            return null;
                        }
                    }
                } finally {
                    output.flush();
                    output.close();
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Updates our progress bar with the download information
     */
    @Override
    protected void onProgressUpdate(Integer... changed) {
        progressDialog.setProgress(changed[0]);
    }

    /**
     * Sets up the progress dialog to notify user of download progress
     */
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(mContext.getString(R.string.updateFetching));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * Dismissed progress dialog, calls install() if installAfterDownload is true
     */
    @Override
    protected void onPostExecute(String result) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        downloaded = true;
        if (installAfterDownload) {
            if (downloadFilePath != null && downloadFilePath.exists()) {
                String fileChecksum = getMD5Checksum(downloadFilePath.getPath());
                if (fileChecksum != null && fileChecksum.equals(mChecksum)) {
                    install(downloadFilePath);
                } else {
                    //delete the file if the checksum does not match
                    // noinspection ResultOfMethodCallIgnored
                    downloadFilePath.delete();
                }
            } else {
                Toast.makeText(mContext,
                        mContext.getString(R.string.updateFailed), Toast.LENGTH_SHORT).show();

            }
        }
    }

    /**
     * Launches an Intent to install the apk update.
     */
    private void install(File filePath) {
        if (downloaded) {
            Uri fileLoc = Uri.fromFile(filePath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileLoc, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
