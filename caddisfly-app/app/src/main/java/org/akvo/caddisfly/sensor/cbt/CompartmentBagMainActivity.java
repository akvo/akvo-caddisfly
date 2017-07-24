package org.akvo.caddisfly.sensor.cbt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CompartmentBagMainActivity extends BaseActivity implements CompartmentBagFragment.OnFragmentInteractionListener {

    public static final int REQUEST_TAKE_PHOTO = 1;
    private final SparseArray<String> results = new SparseArray<>();
    String mCurrentPhotoPath;
    String imageFileName = "";
    private FragmentManager fragmentManager;
    private String mResult = "00000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartment_bag_main);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtInstructionsFragment.newInstance(), "cbtInstructions")
                .commit();
    }

    public void onClickNextButton(View view) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CompartmentBagFragment.newInstance(mResult), "compartmentFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();
    }

    public void onClickMatchedButton(View view) {

        Toast.makeText(this, "Take a photo of the compartment bag", Toast.LENGTH_LONG).show();

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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.akvo.caddisfly.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 2000);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void onClickShowResult(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtResultFragment.newInstance(mResult), "resultFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.layoutFragment, CbtResultFragment.newInstance(mResult), "resultFragment")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                            .addToBackStack(null)
                            .commit();
                    break;
            }
        }
    }

    @Override
    public void onFragmentInteraction(String key) {
        mResult = key;
    }

    public void onClickAcceptResult(View view) {

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        Intent resultIntent = new Intent(getIntent());

        results.clear();

        final File photoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

        ImageUtil.resizeImage(mCurrentPhotoPath, photoPath.getAbsolutePath() + File.separator + imageFileName);

        //noinspection ResultOfMethodCallIgnored
        new File(mCurrentPhotoPath).delete();

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult);

        results.put(1, mpnValue.getMpn());
        results.put(2, StringUtil.getStringResourceByName(this, mpnValue.getRiskCategory()).toString());

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, imageFileName, null);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

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

    public void onClickIncubationTimes(View view) {

        DialogFragment newFragment = new FireMissilesDialogFragment();
        newFragment.show(getSupportFragmentManager(), "missiles");
    }

    public static class FireMissilesDialogFragment extends DialogFragment {
        @SuppressLint("InflateParams")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_incubation_times, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
