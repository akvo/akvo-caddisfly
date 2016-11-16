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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.camera.CameraActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.instructions.InstructionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class BrandInfoActivity extends BaseActivity {

    private static final String TAG = "BrandInfoActivity";

    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private String mUuid;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_info);

        final Activity activity = this;
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        // To start Camera
        Button buttonPrepareTest = (Button) findViewById(R.id.button_prepare);
        buttonPrepareTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ApiUtil.hasPermissions(activity, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
                } else {
                    startCamera();
                }
            }
        });

        // To display Instructions
        Button buttonInstruction = (Button) findViewById(R.id.button_instructions);
        buttonInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), InstructionActivity.class);
                intent.putExtra(Constant.UUID, mUuid);
                startActivity(intent);
            }
        });

        mUuid = getIntent().getStringExtra(Constant.UUID);

        if (mUuid != null) {
            StripTest stripTest = new StripTest();

            // Display the brand in title
            setTitle(stripTest.getBrand(this, mUuid).getName());

            // Display the brand photo
            ImageView imageView = (ImageView) findViewById(R.id.fragment_choose_strip_testImageView);
            InputStream ims = null;
            try {
                String path = getResources().getString(R.string.striptest_images);
                ims = getAssets().open(path + "/" + stripTest.getBrand(this, mUuid).getImage() + ".png");

                Drawable drawable = Drawable.createFromStream(ims, null);
                imageView.setImageDrawable(drawable);
                imageView.setScaleType(stripTest.getBrand(this, mUuid).getImageScale().equals("centerCrop")
                        ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            } finally {
                if (ims != null) {
                    try {
                        ims.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

            }

            JSONArray instructions = stripTest.getBrand(this, mUuid).getInstructions();
            if (instructions == null || instructions.length() == 0) {
                buttonInstruction.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startCamera();
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, getString(R.string.cameraAndStoragePermissions),
                                Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ApiUtil.startInstalledAppDetailsActivity(activity);
                            }
                        });

                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    private void startCamera() {
        Intent intent = new Intent(getIntent());
        intent.setClass(this, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Intent intent = new Intent(getIntent());
            setResult(resultCode, intent);

            if (resultCode == RESULT_OK) {
                intent.putExtra(SensorConstants.RESPONSE, data.getStringExtra(SensorConstants.RESPONSE));
                intent.putExtra(SensorConstants.IMAGE, data.getStringExtra(SensorConstants.IMAGE));
            }

            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        StripTest stripTest = new StripTest();
        setTitle(stripTest.getBrand(this, mUuid).getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
