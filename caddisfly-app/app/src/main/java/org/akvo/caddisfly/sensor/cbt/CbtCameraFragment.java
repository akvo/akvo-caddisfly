package org.akvo.caddisfly.sensor.cbt;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentCbtCameraBinding;
import org.akvo.caddisfly.ui.BaseFragment;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.photo.BitmapPhoto;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;

import static io.fotoapparat.log.Loggers.fileLogger;
import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.log.Loggers.loggers;
import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;
import static io.fotoapparat.result.transformer.SizeTransformers.scaled;

public class CbtCameraFragment extends BaseFragment {

    private static final String ARG_PARAM1 = "arg";
    FragmentCbtCameraBinding binding;
    private PermissionsDelegate permissionsDelegate;
    //    private String mKey = "00000";
    private boolean hasCameraPermission;
    private FotoapparatSwitcher fotoapparatSwitcher;

    public static Fragment newInstance(String key) {
        CbtCameraFragment fragment = new CbtCameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cbt_camera, container, false);

        binding.setCallback(this);

        permissionsDelegate = new PermissionsDelegate(getActivity());

        hasCameraPermission = permissionsDelegate.hasCameraPermission();

        if (hasCameraPermission) {
            binding.cameraView.setVisibility(View.VISIBLE);
        } else {
            permissionsDelegate.requestCameraPermission();
        }

        setupFotoapparat();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            fotoapparatSwitcher.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            fotoapparatSwitcher.stop();
        }
    }

    public void onClickCapture() {
        takePicture();
    }

    private void setupFotoapparat() {
        Fotoapparat backFotoapparat = createFotoapparat(LensPosition.BACK);
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(backFotoapparat);
    }

    private Fotoapparat createFotoapparat(LensPosition position) {
        return Fotoapparat
                .with(getContext())
                .into(binding.cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .photoSize(standardRatio(biggestSize()))
                .lensPosition(lensPosition(position))
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                .flash(firstAvailable(
                        autoRedEye(),
                        autoFlash(),
                        torch(),
                        off()
                ))
                .logger(loggers(
                        logcat(),
                        fileLogger(getContext())
                ))
                .cameraErrorCallback(new CameraErrorCallback() {
                    @Override
                    public void onError(CameraException e) {
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    private void takePicture() {
        PhotoResult photoResult = fotoapparatSwitcher.getCurrentFotoapparat().takePicture();

        photoResult
                .toBitmap(scaled(0.25f))
                .whenAvailable(new PendingResult.Callback<BitmapPhoto>() {
                    @Override
                    public void onResult(BitmapPhoto result) {
                        binding.result.setImageBitmap(result.bitmap);
                        binding.result.setRotation(-result.rotationDegrees);

                        binding.result.setVisibility(View.VISIBLE);
                        binding.cameraView.setVisibility(View.GONE);
                        binding.buttonCapture.setVisibility(View.GONE);
                        binding.buttonOk.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            binding.cameraView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(view, "Result");
    }
}
