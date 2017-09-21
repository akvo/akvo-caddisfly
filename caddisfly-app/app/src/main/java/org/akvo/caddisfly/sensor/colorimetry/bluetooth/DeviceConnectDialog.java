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

package org.akvo.caddisfly.sensor.colorimetry.bluetooth;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.akvo.caddisfly.R;

public class DeviceConnectDialog extends DialogFragment {

    private static final double WIDTH_PERCENTAGE = 0.98;

    public static DeviceConnectDialog newInstance() {
        return new DeviceConnectDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("MD610 not found");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View newFileView = inflater.inflate(R.layout.dialog_device_instructions, null);
        builder.setView(newFileView);

        builder.setPositiveButton(R.string.retry, (dialog, which) -> {
            InterfaceCommunicator listener = (InterfaceCommunicator) getActivity();
            listener.sendRequestCode(Activity.RESULT_OK);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            InterfaceCommunicator listener = (InterfaceCommunicator) getActivity();
            listener.sendRequestCode(Activity.RESULT_CANCELED);
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        int dialogWidth = (int) (WIDTH_PERCENTAGE * getResources().getDisplayMetrics().widthPixels);

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(dialogWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

    }

    public interface InterfaceCommunicator {
        void sendRequestCode(int code);
    }

}
