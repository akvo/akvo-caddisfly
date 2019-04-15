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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import static android.app.Activity.RESULT_OK;
import static org.akvo.caddisfly.common.AppConfig.FILE_PROVIDER_AUTHORITY_URI;
import static org.akvo.caddisfly.common.AppConfig.SKIP_RESULT_VALIDATION;

public class ResultPhotoFragment extends BaseFragment {

    private static final int MANUAL_TEST = 2;
    private OnPhotoTakenListener mListener;
    private String imageFileName = "";
    private String currentPhotoPath;
    private String resultImagePath;
    private ImageView imageResult;

    private Button takePhotoButton;

    /**
     * Get the instance.
     */
    public static ResultPhotoFragment newInstance() {
        return new ResultPhotoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result_photo, container, false);

        imageResult = view.findViewById(R.id.imageResult);

        takePhotoButton = view.findViewById(R.id.takePhoto);

        if (!imageFileName.isEmpty()) {

            final File newPhotoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);
            resultImagePath = newPhotoPath.getAbsolutePath() + File.separator + imageFileName;

            File imgFile = new File(resultImagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageResult.setImageBitmap(myBitmap);
                imageResult.setBackground(null);
                takePhotoButton.setText(R.string.retakePhoto);
            }
        }

        takePhotoButton.setOnClickListener(view1 -> takePhoto());

        return view;
    }

    private void takePhoto() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (pictureIntent.resolveActivity(Objects.requireNonNull(
                getActivity()).getPackageManager()) != null) {
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
                    photoUri = FileProvider.getUriForFile(getActivity(),
                            FILE_PROVIDER_AUTHORITY_URI,
                            photoFile);
                }
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(pictureIntent, MANUAL_TEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            final File newPhotoPath = FileHelper.getFilesDir(FileHelper.FileType.RESULT_IMAGE);

            resultImagePath = newPhotoPath.getAbsolutePath() + File.separator + imageFileName;

            if (currentPhotoPath != null) {
                ImageUtil.resizeImage(currentPhotoPath, resultImagePath, 640);

                File imageFile = new File(currentPhotoPath);
                if (imageFile.exists() && !new File(currentPhotoPath).delete()) {
                    Toast.makeText(getActivity(), R.string.delete_error, Toast.LENGTH_SHORT).show();
                }
            }

            File imgFile = new File(resultImagePath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageResult.setImageBitmap(myBitmap);
                imageResult.setBackground(null);
                takePhotoButton.setText(R.string.retakePhoto);
            }

            mListener.onPhotoTaken(imageFileName);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        imageFileName = UUID.randomUUID().toString();

        File storageDir = Objects.requireNonNull(getActivity())
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPhotoTakenListener) {
            mListener = (OnPhotoTakenListener) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement OnPhotoTakenListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    boolean isValid() {
        return SKIP_RESULT_VALIDATION || (resultImagePath != null && !resultImagePath.isEmpty() && new File(resultImagePath).exists());
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public interface OnPhotoTakenListener {
        void onPhotoTaken(String photoPath);
    }
}
