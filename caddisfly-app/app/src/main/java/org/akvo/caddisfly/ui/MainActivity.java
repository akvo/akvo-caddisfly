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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.UpdateIntentService;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.sensor.ec.SensorActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.NetUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity {

    private final WeakRefHandler handler = new WeakRefHandler(this);
    private UpdateCheckReceiver receiver;
    private DownloadReceiver downloadReceiver;
    private DownloadManager downloadManager;
    private boolean mShouldClose = false;

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

    private static void installUpdate(final Context context, final File file) {

        PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey, Calendar.getInstance().getTimeInMillis());

        if (file != null) {

            int versionCode = 0;
            try {
                versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            int fileVersion;
            Pattern pattern = Pattern.compile("(\\d+).apk");
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) {
                fileVersion = Integer.parseInt(matcher.group(1));
                if (fileVersion > versionCode) {
                    String fileChecksum = getMD5Checksum(file.getPath());
                    String checksum = PreferencesUtil.getString(context, "updateChecksum", "");
                    if (fileChecksum == null || !fileChecksum.equals(checksum)) {
                        //delete the file if the checksum does not match
                        // noinspection ResultOfMethodCallIgnored
                        file.delete();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.update);
                        builder.setMessage(R.string.updateAvailable)
                                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.notNow, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        builder.create().show();
                    }
                } else {
                    // noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fabDisableDiagnostics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                        Toast.LENGTH_SHORT).show();

                AppPreferences.disableDiagnosticMode();

                switchLayoutForDiagnosticOrUserMode();

                changeActionBarStyleBasedOnCurrentMode();
            }
        });

        findViewById(R.id.buttonCalibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getBaseContext(), TypeListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        findViewById(R.id.buttonEcSensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean hasOtg = getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    CaddisflyApp.getApp().loadTestConfiguration("ECOND");
                    final Intent intent = new Intent(getBaseContext(), SensorActivity.class);
                    intent.putExtra("internal", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                } else {
                    alertFeatureNotSupported();
                }
            }
        });

        findViewById(R.id.buttonSurvey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSurvey();
            }
        });

        long updateLastCheck = PreferencesUtil.getLong(this, R.string.lastUpdateCheckKey);
        Calendar currentDate = Calendar.getInstance();

        if (AppConfig.USE_CUSTOM_AUTO_UPDATE) {

            if (currentDate.getTimeInMillis() - updateLastCheck > AppConfig.UPDATE_CHECK_INTERVAL) {

                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = downloadManager.query(new DownloadManager.Query());
                boolean isDownloaded = false;
                loop:
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    if (getString(R.string.appName).equals(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)))) {
                        int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                        switch (downloadStatus) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                //Uri fileLoc = downloadManager.getUriForDownloadedFile(downloadReference);
                                final String filePath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                                final File file = new File(filePath);

                                if (file.exists()) {
                                    isDownloaded = true;
                                    (new Handler()).postDelayed(new Runnable() {
                                        public void run() {
                                            installUpdate(MainActivity.this, file);
                                        }
                                    }, 300);

                                    break loop;
                                } else {
                                    downloadManager.remove(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
                                }
                                break;
                            case DownloadManager.STATUS_FAILED:
                                downloadManager.remove(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
                                break;
                            default:
                                isDownloaded = true;
                                break;
                        }
                    }
                }

                if (!isDownloaded) {
                    checkForUpdate();
                }
            }
        } else {

            if (currentDate.getTimeInMillis() - updateLastCheck > AppConfig.UPDATE_CHECK_INTERVAL) {

                int versionCode = 0;
                try {
                    versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (PreferencesUtil.getInt(getBaseContext(), "serverVersionCode", 0) > versionCode) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.update);

                    String packageName = "";
                    try {
                        packageName = getPackageManager().getPackageInfo(getPackageName(), 0).packageName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    final Uri marketUrl = Uri.parse("market://details?id=" + packageName);
                    builder.setMessage(R.string.updateAvailable)
                            .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    PreferencesUtil.setLong(getBaseContext(), R.string.lastUpdateCheckKey,
                                            Calendar.getInstance().getTimeInMillis());

                                    startActivity(new Intent(Intent.ACTION_VIEW, marketUrl));
                                }
                            })
                            .setNegativeButton(R.string.notNow, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    PreferencesUtil.setLong(getBaseContext(), R.string.lastUpdateCheckKey,
                                            Calendar.getInstance().getTimeInMillis());
                                }
                            });
                    builder.create().show();
                } else {
                    checkForUpdate();
                }
            }
        }
    }

    private void checkForUpdate() {
        if (NetUtil.isNetworkAvailable(this)) {
            IntentFilter filter = new IntentFilter(UpdateCheckReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new UpdateCheckReceiver();
            registerReceiver(receiver, filter);

            Intent msgIntent = new Intent(this, UpdateIntentService.class);
            startService(msgIntent);
        }
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setTitle(R.string.appName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShouldClose = false;
        switchLayoutForDiagnosticOrUserMode();

        CaddisflyApp.getApp().setAppLanguage(null, false, handler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            handler.sendEmptyMessage(0);
        }
    }

    /**
     * Show the diagnostic mode layout
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.actionSettings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSurvey() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (intent == null) {
            alertDependantAppNotFound();
        } else {
            mShouldClose = true;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    if (mShouldClose) {
                        finish();
                    }
                }
            }, 4000);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notFound, message);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(receiver);
        } catch (Exception ignored) {
        }
        if (AppConfig.USE_CUSTOM_AUTO_UPDATE) {
            try {
                unregisterReceiver(downloadReceiver);
            } catch (Exception ignored) {
            }
        }
        super.onDestroy();
    }

    /**
     * Handler to restart the app after language has been changed
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        public WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            f.recreate();
        }
    }

    public class DownloadReceiver extends BroadcastReceiver {

        public DownloadReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadReference = PreferencesUtil.getLong(context, "downloadReference");
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == referenceId) {
                Uri fileLoc = downloadManager.getUriForDownloadedFile(downloadReference);
                installUpdate(context, new File(fileLoc.getPath()));
            }
        }
    }

    public class UpdateCheckReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "org.akvo.caddisfly.intent.action.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject jsonMessage = new JSONObject(intent.getStringExtra(UpdateIntentService.RESPONSE_MESSAGE));
                if (jsonMessage.has("version")) {
                    int serverVersion = jsonMessage.getInt("version");
                    PreferencesUtil.setInt(context, "serverVersionCode", serverVersion);
                    PreferencesUtil.setLong(context, R.string.lastUpdateCheckKey, Calendar.getInstance().getTimeInMillis());

                    if (AppConfig.USE_CUSTOM_AUTO_UPDATE) {
                        int versionCode = 0;
                        try {
                            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (serverVersion > versionCode) {
                            final String location = jsonMessage.getString("fileName");
                            String checksum = jsonMessage.getString("md5Checksum");
                            PreferencesUtil.setString(context, "updateChecksum", checksum);
                            //String versionName = jsonMessage.getString("versionName");

                            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            Uri Download_Uri = Uri.parse(location);
                            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                            request.setAllowedOverRoaming(false);
                            request.setTitle(getString(R.string.appName));
                            String fileName = location.substring(location.lastIndexOf('/') + 1);
                            request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, fileName);

                            try {
                                unregisterReceiver(downloadReceiver);
                            } catch (Exception ignored) {
                            }

                            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                            downloadReceiver = new DownloadReceiver();
                            registerReceiver(downloadReceiver, filter);

                            PreferencesUtil.setLong(context, "downloadReference", downloadManager.enqueue(request));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}

