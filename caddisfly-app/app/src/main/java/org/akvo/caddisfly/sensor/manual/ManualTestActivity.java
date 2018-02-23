package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ImageUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.akvo.caddisfly.common.AppConfig.FILE_PROVIDER_AUTHORITY_URI;

public class ManualTestActivity extends BaseActivity
        implements MeasurementInputFragment.OnSubmitResultListener {

    private static final int MANUAL_TEST = 2;

    private TestInfo testInfo;
    private FragmentManager fragmentManager;
    private String imageFileName = "";
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        setTitle(testInfo.getName());

        startManualTest();
    }

    private void startManualTest() {
        if (testInfo.getHasImage()) {

            (new Handler()).postDelayed(() -> {
                Toast toast = Toast.makeText(this, R.string.take_photo_meter_result, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.show();
            }, 400);

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

                    Uri photoUri;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        photoUri = Uri.fromFile(photoFile);
                    } else {
                        photoUri = FileProvider.getUriForFile(this,
                                FILE_PROVIDER_AUTHORITY_URI,
                                photoFile);
                    }
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(pictureIntent, MANUAL_TEST);
                }
            }
        } else {
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.add(R.id.fragment_container,
                    MeasurementInputFragment.newInstance(testInfo), "tubeFragment")
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (requestCode == MANUAL_TEST) {
                fragmentTransaction.replace(R.id.fragment_container,
                        MeasurementInputFragment.newInstance(testInfo), "manualFragment")
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onSubmitResult(String result) {
        Intent resultIntent = new Intent(getIntent());

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        if (currentPhotoPath != null) {
            ImageUtil.resizeImage(currentPhotoPath, resultImagePath);

            File imageFile = new File(currentPhotoPath);
            if (imageFile.exists() && !new File(currentPhotoPath).delete()) {
                Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
            }
        }

        results.put(1, result);

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, -1, imageFileName);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(ConstantKey.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
