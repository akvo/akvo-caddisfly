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

package org.akvo.caddisfly.sensor.manual;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Displays the brand information for the test.
 */
public class ManualTestActivity extends BaseActivity {

    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
    private static final int MANUAL_TEST = 2;
    private static final int MANUAL_TEST_PHOTO = 4;

    String mCurrentPhotoPath;
    String imageFileName = "";

    @BindView(R.id.button_instructions)
    Button buttonInstruction;

    @BindView(R.id.button_prepare)
    Button buttonStart;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.imageBrandLabel)
    ImageView imageBrandLabel;

    private String mUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_info);

        ButterKnife.bind(this);

        mUuid = getIntent().getStringExtra(Constant.UUID);

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        buttonStart.setText(R.string.next);

        if (mUuid != null) {

            // Display the brand in title
            setTitle(testInfo.getTitle());

            // Display the brand photo
            InputStream ims = null;
            try {
                Drawable drawable;
                String image = testInfo.getImage();

                if (image != null) {
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
                    imageBrandLabel.setScaleType(testInfo.getImageScale().equals("centerCrop")
                            ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
                }
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
        }

        JSONArray instructions = testInfo.getInstructions();
        if (instructions == null || instructions.length() == 0) {
            buttonInstruction.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.button_prepare)
    void prepareTest() {
        if (CaddisflyApp.getApp().getCurrentTestInfo().getHasImage()) {
            if (!ApiUtil.hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                startTest();
            }
        } else {
            startTest();
        }
    }

    private void startTest() {
        startManualTest();
    }

    @OnClick(R.id.button_instructions)
    public void showInstructions() {
        Intent intent = new Intent(this, InstructionActivity.class);
        intent.putExtra(Constant.UUID, mUuid);
        startActivity(intent);
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
                startTest();
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, getString(R.string.cameraAndStoragePermissions),
                                Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(activity));

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
    }

    private void startManualTest() {
        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        if (testInfo.getHasImage()) {
            Toast.makeText(this, "Take a photo of the screen displaying the measurement on the meter", Toast.LENGTH_LONG).show();

            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {

                    Uri photoURI;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        photoURI = Uri.fromFile(photoFile);
                    } else {
                        photoURI = FileProvider.getUriForFile(this,
                                AppConfig.FILE_PROVIDER_AUTHORITY_URI,
                                photoFile);
                    }
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    startActivityForResult(pictureIntent, MANUAL_TEST_PHOTO);
                }
            }
        } else {

            final Intent intent1 = new Intent(this, MeasurementInputActivity.class);
            startActivityForResult(intent1, MANUAL_TEST);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        imageFileName = UUID.randomUUID().toString();

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFileName += ".jpg";
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    Intent intent = new Intent(data);
                    setResult(resultCode, intent);
                }

                finish();
            } else if (requestCode == MANUAL_TEST) {
                submitResult(data.getStringExtra(SensorConstants.RESPONSE));
            } else if (requestCode == MANUAL_TEST_PHOTO) {
                final Intent intent1 = new Intent(this, MeasurementInputActivity.class);
                startActivityForResult(intent1, MANUAL_TEST);
            }
        }
    }

    public void submitResult(String result) {
        Intent resultIntent = new Intent(getIntent());

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        if (mCurrentPhotoPath != null) {
            ImageUtil.resizeImage(mCurrentPhotoPath, resultImagePath);

            File imageFile = new File(mCurrentPhotoPath);
            if (imageFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                new File(mCurrentPhotoPath).delete();
            }
        }

        results.put(1, result);

        JSONObject resultJson = TestConfigHelper.getJsonResult(
                CaddisflyApp.getApp().getCurrentTestInfo(), results, -1, imageFileName, null);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
