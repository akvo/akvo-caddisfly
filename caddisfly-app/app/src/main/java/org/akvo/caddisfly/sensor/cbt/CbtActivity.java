package org.akvo.caddisfly.sensor.cbt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static org.akvo.caddisfly.common.AppConfig.FILE_PROVIDER_AUTHORITY_URI;

public class CbtActivity extends BaseActivity
        implements CompartmentBagFragment.OnCompartmentBagSelectListener {

    private static final int CBT_TEST = 1;

    private String imageFileName = "";
    private String currentPhotoPath;
    private String cbtResult = "00000";
    private TestInfo testInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbt);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(ConstantKey.CURRENT_PHOTO_PATH);
            imageFileName = savedInstanceState.getString(ConstantKey.CURRENT_IMAGE_FILE_NAME);
            testInfo = savedInstanceState.getParcelable(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        if (testInfo == null) {
            return;
        }

        if (savedInstanceState == null) {
            startCbtTest();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ConstantKey.CURRENT_PHOTO_PATH, currentPhotoPath);
        outState.putString(ConstantKey.CURRENT_IMAGE_FILE_NAME, imageFileName);
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(testInfo.getName());
    }

    private void startCbtTest() {
        if (testInfo.getHasImage()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {

                    (new Handler()).postDelayed(() -> {
                        Toast toast = Toast.makeText(this, R.string.take_photo_compartments, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 200);
                        toast.show();
                    }, 400);

                    Uri photoUri;

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        photoUri = Uri.fromFile(photoFile);
                    } else {
                        photoUri = FileProvider.getUriForFile(this,
                                FILE_PROVIDER_AUTHORITY_URI,
                                photoFile);
                    }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, CBT_TEST);
                } else {
                    Toast.makeText(this, "Error taking photo. Please close any other app that may be using the camera",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        } else {
            showCompartmentInput();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CBT_TEST) {
                (new Handler()).postDelayed(this::showCompartmentInput, 1000);

                final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);
                String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;
                ImageUtil.resizeImage(currentPhotoPath, resultImagePath, 1280);

                File imageFile = new File(currentPhotoPath);
                if (imageFile.exists() && !new File(currentPhotoPath).delete()) {
                    Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            finish();
        }
    }

    private void showCompartmentInput() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                CompartmentBagFragment.newInstance(cbtResult), "compartmentFragment")
                .commit();
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

    public void onCompartmentBagSelect(String key) {
        cbtResult = key;
    }

    @SuppressWarnings("unused")
    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                CbtResultFragment.newInstance(cbtResult, testInfo.getSampleQuantity()), "resultFragment")
                .addToBackStack(null)
                .commit();
    }

    @SuppressWarnings("unused")
    public void onClickSubmitResult(View view) {

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(cbtResult, testInfo.getSampleQuantity());

        results.put(1, StringUtil.getStringResourceByName(this,
                mpnValue.getRiskCategory(), "en").toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, String.valueOf(mpnValue.getConfidence()));

        JSONObject resultJson = TestConfigHelper.getJsonResult(this, testInfo,
                results, null, imageFileName);

        Intent resultIntent = new Intent();
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

    public static class IncubationTimesDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_incubation_times, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
            return builder.create();
        }
    }

}
