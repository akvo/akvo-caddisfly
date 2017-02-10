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

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.sensor.colorimetry.strip.camera.CameraActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.instructions.InstructionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Displays the brand information for the test.
 */
public class BrandInfoActivity extends BaseActivity {

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

        ImageView imageBrandLabel = (ImageView) findViewById(R.id.imageBrandLabel);

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
            setTitle(stripTest.getBrand(mUuid).getName());

//            try {
//                imageBrandLabel.setBackgroundColor(Color.parseColor(stripTest.getBrand(this, mUuid).getBackground()));
//            } catch (Exception ignored) {
//
//            }

            // Display the brand photo
            InputStream ims = null;
            try {
                Drawable drawable;
                String image = stripTest.getBrand(mUuid).getImage();

                if (image.contains(File.separator)) {
                    if (!image.contains(".")) {
                        image = image + ".webp";
                    }
                    drawable = Drawable.createFromPath(image);
                } else {
                    String path = getResources().getString(R.string.striptest_images);
                    ims = getAssets().open(path + File.separator + image + ".webp");
                    drawable = Drawable.createFromStream(ims, null);
                }

                imageBrandLabel.setImageDrawable(drawable);
                imageBrandLabel.setScaleType(stripTest.getBrand(mUuid).getImageScale().equals("centerCrop")
                        ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
            } catch (Exception ex) {
                Timber.e(ex);
            } finally {
                if (ims != null) {
                    try {
                        ims.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }

            }

            JSONArray instructions = stripTest.getBrand(mUuid).getInstructions();
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

        if (PreferencesUtil.getBoolean(this, R.string.showMinMegaPixelDialogKey, true)) {
            try {

                if (CameraHelper.getMaxSupportedMegaPixelsByCamera(this) < Constant.MIN_CAMERA_MEGA_PIXELS) {

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    View checkBoxView = View.inflate(this, R.layout.dialog_message, null);
                    CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            PreferencesUtil.setBoolean(getBaseContext(), R.string.showMinMegaPixelDialogKey, !isChecked);
                        }
                    });

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.warning);
                    builder.setMessage(R.string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_anyway, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    Intent intent = new Intent(getIntent());
                                    intent.setClass(getBaseContext(), CameraActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivityForResult(intent, 100);
                                }
                            })
                            .setNegativeButton(R.string.stop_test, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).show();

                } else {
                    Intent intent = new Intent(getIntent());
                    intent.setClass(this, CameraActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, 100);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            Intent intent = new Intent(getIntent());
            intent.setClass(this, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (data != null) {
                Intent intent = new Intent(data);
                setResult(resultCode, intent);
            }

            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        StripTest stripTest = new StripTest();
        setTitle(stripTest.getBrand(mUuid).getName());
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
