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

import static android.app.Activity.RESULT_OK;
import static org.akvo.caddisfly.common.AppConstants.FILE_PROVIDER_AUTHORITY_URI;
import static org.akvo.caddisfly.helper.FileHelper.getFormImagesFolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentResultPhotoBinding;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.ImageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


public class ResultPhotoFragment extends BaseFragment {

    private static final String ARG_INSTRUCTION = "resultInstruction";
    private static final String ARG_RESULT_NAME = "resultName";
    private static final int MANUAL_TEST = 2;
    private OnPhotoTakenListener mListener;
    private String imageFileName = "";
    private String currentPhotoPath = "";
    private FragmentResultPhotoBinding b;

    /**
     * Get the instance.
     *
     * @param testName : Name of the test
     * @param id       : fragment id
     */
    public static ResultPhotoFragment newInstance(String testName, Instruction instruction, int id) {
        ResultPhotoFragment fragment = new ResultPhotoFragment();
        fragment.setFragmentId(id);
        Bundle args = new Bundle();
        args.putParcelable(ARG_INSTRUCTION, instruction);
        args.putString(ARG_RESULT_NAME, testName);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_result_photo, container, false);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(ConstantKey.CURRENT_PHOTO_PATH);
            imageFileName = savedInstanceState.getString(ConstantKey.CURRENT_IMAGE_FILE_NAME);
        }

        View view = b.getRoot();

        if (getArguments() != null) {

            Instruction instruction = getArguments().getParcelable(ARG_INSTRUCTION);
            b.setInstruction(instruction);

            String title = getArguments().getString(ARG_RESULT_NAME);
            if (title == null || title.isEmpty()) {
                b.textName.setVisibility(View.GONE);
            } else {
                b.textName.setText(title);
            }
        }

        if (imageFileName != null && !imageFileName.isEmpty()) {
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                b.imageResult.setImageBitmap(myBitmap);
                b.imageResult.setBackground(null);
                b.takePhoto.setText(R.string.retakePhoto);
            }
        }

        b.takePhoto.setOnClickListener(view1 -> takePhoto());

        return view;
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        outState.putString(ConstantKey.CURRENT_PHOTO_PATH, currentPhotoPath);
        outState.putString(ConstantKey.CURRENT_IMAGE_FILE_NAME, imageFileName);
        super.onSaveInstanceState(outState);
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
            if (currentPhotoPath != null) {
                ImageUtil.resizeImage(currentPhotoPath, currentPhotoPath, 640);
                File imgFile = new File(currentPhotoPath);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    b.imageResult.setImageBitmap(myBitmap);
                    b.imageResult.setBackground(null);
                    b.takePhoto.setText(R.string.retakePhoto);
                }
            }

            (new Handler()).postDelayed(() -> mListener.onPhotoTaken(), 600);
        }
    }

    private File createImageFile() throws IOException {
        imageFileName = UUID.randomUUID().toString();
        File image = File.createTempFile(imageFileName, ".jpg", getFormImagesFolder());
        imageFileName += ".jpg";
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

    public boolean isValid() {
        return BuildConfig.TEST_RUNNING.get() || (currentPhotoPath != null &&
                !currentPhotoPath.isEmpty() && new File(currentPhotoPath).exists());
    }

    public String getImageFileName() {
        return currentPhotoPath;
    }

    public interface OnPhotoTakenListener {
        void onPhotoTaken();
    }
}
