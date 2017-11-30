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

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.cbt.CbtInstructionFragment;
import org.akvo.caddisfly.sensor.cbt.TestInfoFragment;
import org.akvo.caddisfly.sensor.colorimetry.stripv2.utils.Constants;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.PermissionsDelegate;

public class ColorimetryTestActivity extends BaseActivity {

    private static final int REQUEST_TEST = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private final String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private CoordinatorLayout coordinatorLayout;
    private boolean hasPermissions;
    private TestInfo mTestInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBindingUtil.setContentView(this, R.layout.activity_test);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        FragmentManager fragmentManager = getSupportFragmentManager();

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

    public void onStartTestClick(View view) {

        if (hasPermissions) {
            startTest();
        } else {
            permissionsDelegate.requestPermissions(permissions);
        }

    }

    private void startTest() {

        final Intent intent = new Intent();
        // intent.putExtra(SensorConstants.IS_EXTERNAL_ACTION, mIsExternalAppCall);
        if (mTestInfo.getCanUseDilution()) {
            intent.setClass(this, SelectDilutionActivity.class);
        } else {
            intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
        }

        intent.putExtra(Constants.UUID, mTestInfo.getId());
        intent.putExtra(Constants.SEND_IMAGE_IN_RESULT, true);
        startActivityForResult(intent, REQUEST_TEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_TEST) {
            Intent intent = new Intent(data);
            this.setResult(Activity.RESULT_OK, intent);
            finish();
        }
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
