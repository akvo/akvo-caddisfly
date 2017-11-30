package org.akvo.caddisfly.sensor.cbt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.models.StripTest;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PermissionsDelegate;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TestActivity extends BaseActivity implements CompartmentBagFragment.OnFragmentInteractionListener {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private final String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private CoordinatorLayout coordinatorLayout;
    private String mCurrentPhotoPath;
    private String imageFileName = "";
    private boolean hasPermissions;
    private TestInfo mTestInfo;
    private String mResult = "00000";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBindingUtil.setContentView(this, R.layout.activity_test);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        fragmentManager = getSupportFragmentManager();

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            mTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

            if (mTestInfo != null) {
                TestInfoFragment fragment = TestInfoFragment.forProduct(mTestInfo);

                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, "TestInfoFragment").commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPermissions = permissionsDelegate.hasPermissions(permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest();
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, getString(R.string.cameraAndStoragePermissions),
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mTestInfo != null) {
            setTitle(mTestInfo.getTitle());
        }
    }

    public void onInstructionsClick(View view) {

        CbtInstructionFragment instructionFragment = CbtInstructionFragment.forProduct(mTestInfo);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("instructions")
                .replace(R.id.fragment_container,
                        instructionFragment, null).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickIncubationTimes(View view) {
        DialogFragment newFragment = new IncubationTimesDialogFragment();
        newFragment.show(getSupportFragmentManager(), "incubationTimes");
    }

    public void onSiteLinkClick(View view) {
        String url = "www.aquagenx.com";
        if (!url.contains("http://")) {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void onStartTestClick(View view) {

        if (hasPermissions) {
            startTest();
        } else {
            permissionsDelegate.requestPermissions(permissions);
        }

    }

    private void startTest() {

        new Handler().postDelayed(() -> {
            Toast toast = Toast.makeText(this, R.string.take_photo_of_cbt, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 190);
            toast.show();
        }, 1000);

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

                Uri photoURI;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(photoFile);
                } else {
                    photoURI = FileProvider.getUriForFile(this,
                            "org.akvo.caddisfly.fileprovider",
                            photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, CompartmentBagFragment.newInstance(mResult), "compartmentFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .addToBackStack(null)
                            .commit();
                    break;
            }
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
    public void onFragmentInteraction(String key) {
        mResult = key;
    }

    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, CbtResultFragment.newInstance(mResult), "resultFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();

    }

    public void onClickAcceptResult(View view) {

        Intent resultIntent = new Intent(getIntent());

        SparseArray<String> results = new SparseArray<>();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        String resultImagePath = photoPath.getAbsolutePath() + File.separator + imageFileName;

        ImageUtil.resizeImage(mCurrentPhotoPath, resultImagePath);

        File imageFile = new File(mCurrentPhotoPath);
        if (imageFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(mCurrentPhotoPath).delete();
        }

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult);

        results.put(1, StringUtil.getStringResourceByName(this, mpnValue.getRiskCategory()).toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, mpnValue.getConfidence());

        JSONObject resultJson = TestConfigHelper.getJsonResult(mTestInfo, results, null, -1,
                imageFileName, StripTest.GroupType.INDIVIDUAL);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());
        resultIntent.putExtra(SensorConstants.IMAGE, resultImagePath);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    public static class IncubationTimesDialogFragment extends DialogFragment {
        @NonNull
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_incubation_times, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());


            return builder.create();
        }
    }
}
