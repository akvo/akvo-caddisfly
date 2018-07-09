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

package org.akvo.caddisfly.ui;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.ActivityTestListBinding;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ConfigDownloader;

public class TestListActivity extends BaseActivity
        implements TestListFragment.OnListFragmentInteractionListener {

    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private static final int REQUEST_SYNC_PERMISSION = 101;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final String[] storagePermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TestListFragment fragment;
    private TestInfo testInfo;
    private ActivityTestListBinding b;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionsDelegate.resultGranted(grantResults)) {
            if (requestCode == REQUEST_SYNC_PERMISSION) {
                startSync();
            } else {
                startTest();
            }
        } else {
            String message;
            if (requestCode == REQUEST_SYNC_PERMISSION) {
                message = getString(R.string.storagePermission);
            } else {
                message = getString(R.string.cameraAndStoragePermissions);
            }

            Snackbar snackbar = Snackbar
                    .make(b.mainLayout, message,
                            Snackbar.LENGTH_LONG)
                    .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(this));

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

            snackbar.setActionTextColor(typedValue.data);
            View snackView = snackbar.getView();
            TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
            textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = DataBindingUtil.setContentView(this, R.layout.activity_test_list);

        setTitle(R.string.selectTest);

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            TestType testType = (TestType) getIntent().getSerializableExtra(ConstantKey.TYPE);

            fragment = TestListFragment.newInstance(testType);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, TestListFragment.TAG).commit();
        }
    }

    /**
     * Shows the detail fragment.
     */
    private void navigateToTestDetails() {

        startTest();
    }

    private void startTest() {

        testInfo = (new TestConfigRepository()).getTestInfo(testInfo.getUuid());

        if (testInfo == null || testInfo.getIsGroup()) {
            return;
        }

        if (testInfo.getResults().size() == 0) {
            ErrorMessages.alertCouldNotLoadConfig(this);
            return;
        }

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        intent.putExtra("internal", true);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(TestInfo testInfo) {
        this.testInfo = testInfo;
        navigateToTestDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (BuildConfig.showExperimentalTests && AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_test_list, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Download and sync test config.
     *
     * @param item menu item click on
     */
    @SuppressWarnings("unused")
    public void onDownloadTests(MenuItem item) {
        if (permissionsDelegate.hasPermissions(storagePermission)) {
            startSync();
        } else {
            permissionsDelegate.requestPermissions(storagePermission, REQUEST_SYNC_PERMISSION);
        }
    }

    private void startSync() {
        ConfigDownloader.syncExperimentalConfig(this, () -> {
            if (fragment != null) {
                fragment.refresh();
            }
        });
    }

    public interface SyncCallbackInterface {
        void onDownloadFinished();
    }
}
