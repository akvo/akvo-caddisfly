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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.liquid.ChamberTestActivity;

public class TestListActivity extends BaseActivity
        implements TestListFragment.OnListFragmentInteractionListener {

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TestInfo testInfo;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBindingUtil.setContentView(this, R.layout.activity_test_list);

        setTitle(R.string.selectTest);

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            TestType testType = (TestType) getIntent().getSerializableExtra("type");

            TestListFragment fragment = TestListFragment.newInstance(testType);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, TestListFragment.TAG).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Shows the detail fragment.
     */
    private void navigateToTestDetails() {

        if (testInfo.getSubtype() == TestType.CHAMBER_TEST) {
            if (permissionsDelegate.hasPermissions(permissions)) {
                startTest();
            } else {
                permissionsDelegate.requestPermissions(permissions);
            }
        } else {
            startTest();
        }
    }

    private void startTest() {
        if (testInfo != null && testInfo.getIsGroup()) {
            return;
        }

        if (testInfo == null || testInfo.getUuid() == null || testInfo.getResults().size() == 0) {
            ErrorMessages.alertCouldNotLoadConfig(this);
            return;
        }

        if (testInfo.getSubtype() == TestType.CHAMBER_TEST) {
            startCalibration();
        } else {
            Intent intent = new Intent(this, TestActivity.class);
            intent.putExtra(ConstantKey.TEST_INFO, testInfo);
            intent.putExtra("internal", true);
            startActivity(intent);
        }
    }

    private void startCalibration() {
        //Only start the colorimetry calibration if the device has a camera flash
        if (AppPreferences.useExternalCamera()
                || CameraHelper.hasFeatureCameraFlash(this,
                R.string.cannotCalibrate, R.string.ok, null)) {

            final Intent intent;
            if (testInfo.getResults().get(0).getColors().size() > 0) {
                intent = new Intent(this, ChamberTestActivity.class);
            } else {
                ErrorMessages.alertCouldNotLoadConfig(this);
                return;
            }
            intent.putExtra(ConstantKey.TEST_INFO, testInfo);
            startActivity(intent);
        }
    }

    @Override
    public void onListFragmentInteraction(TestInfo testInfo) {
        this.testInfo = testInfo;
        navigateToTestDetails();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
