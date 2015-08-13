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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.AlertUtils;

public class TypeListActivity extends BaseActivity implements TypeListFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setTitle(getResources().getString(R.string.calibrate));
        }
    }

    @Override
    public void onFragmentInteraction(TestInfo testInfo) {
        CaddisflyApp caddisflyApp = (CaddisflyApp) getApplicationContext();
        caddisflyApp.loadTestConfiguration(testInfo.getCode());

        switch (testInfo.getType()) {
            case COLORIMETRIC_LIQUID:
                //Only start the colorimetry calibration if the device has a camera flash
                if (!CaddisflyApp.hasFeatureCameraFlash(this)) {
                    AlertUtils.showError(this, R.string.cannotCalibrate,
                            getString(R.string.errorCameraFlashRequired),
                            null,
                            R.string.ok, null, null);
                } else {
                    final Intent intent = new Intent(this, CalibrateListActivity.class);
                    startActivity(intent);
                }
                break;
            case SENSOR:
                //Only start the sensor activity if the device supports 'On The Go'(OTG) feature
                boolean hasOtg = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                if (hasOtg) {
                    AlertUtils.askQuestion(this, R.string.warning, R.string.incorrectCalibrationCanAffect,
                            R.string.calibrate, R.string.cancel, true,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final Intent intent = new Intent(getBaseContext(), CalibrateSensorActivity.class);
                                    startActivity(intent);
                                }
                            });
                } else {
                    alertFeatureNotSupported();
                }
                break;
        }
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s\r\n\r\n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtils.showMessage(this, R.string.notSupported, message);
    }
}
