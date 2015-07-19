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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtils;
import org.akvo.caddisfly.util.ApiUtils;

public class TypeListActivity extends AppCompatActivity implements TypeListFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_list);

        ApiUtils.lockScreenOrientation(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setTitle(getResources().getString(R.string.calibrate));
        }
    }

    @Override
    public void onFragmentInteraction(TestInfo testInfo) {
        MainApp mainApp = (MainApp) getApplicationContext();
        mainApp.setSwatches(testInfo.getCode());


        if (testInfo.getType() == 0) {
            if (!MainApp.hasCameraFlash) {
                AlertUtils.showError(this, R.string.error,
                        getString(R.string.errorCameraFlashRequired),
                        null,
                        R.string.ok, null, null);
            } else {
                final Intent intent = new Intent(this, CalibrateListActivity.class);
                startActivity(intent);
            }
        } else {

            boolean hasOtg = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
            if (hasOtg) {
                final Intent intent = new Intent(this, CalibrateSensorActivity.class);
                startActivity(intent);
            } else {
                AlertUtils.showMessage(this, R.string.notSupported, R.string.phoneDoesNotSupport);
            }
        }

    }
}
