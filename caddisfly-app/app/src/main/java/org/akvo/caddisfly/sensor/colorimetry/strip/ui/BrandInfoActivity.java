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
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.camera.CameraActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.instructions.InstructionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.model.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;

import java.io.InputStream;

public class BrandInfoActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private String mBrandCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_info);

        mBrandCode = getIntent().getStringExtra(Constant.BRAND);

        if (mBrandCode != null) {
            StripTest stripTest = new StripTest();

            // Display the brand in title
            setTitle(stripTest.getBrand(mBrandCode).getName());

            // Display the brand photo
            ImageView imageView = (ImageView) findViewById(R.id.fragment_choose_strip_testImageView);
            try {
                String path = getResources().getString(R.string.striptest_images);
                InputStream ims = getAssets().open(path + "/" + mBrandCode + ".png");

                Drawable drawable = Drawable.createFromStream(ims, null);

                ims.close();

                imageView.setImageDrawable(drawable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        final Activity activity = this;

        // To start Camera
        Button buttonPrepareTest = (Button) findViewById(R.id.button_prepare);
        buttonPrepareTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        startCalibration();
                    }
                }

            }
        });

        // To display Instructions
        Button buttonInstruction = (Button) findViewById(R.id.button_instructions);
        buttonInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), InstructionActivity.class);
                intent.putExtra(Constant.BRAND, mBrandCode);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCalibration();
                } else {

                }
            }
        }
    }

    private void startCalibration() {
        Intent intent = new Intent(getBaseContext(), CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.BRAND, mBrandCode);
        startActivityForResult(intent, 100);
    }


    @Override
    public void onResume() {
        super.onResume();

        StripTest stripTest = new StripTest();
        setTitle(stripTest.getBrand(mBrandCode).getName());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }
}