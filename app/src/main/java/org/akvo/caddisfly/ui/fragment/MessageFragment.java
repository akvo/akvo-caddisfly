/*
 * Copyright (C) TernUp Research Labs
 *
 * This file is part of Caddisfly
 *
 * Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import org.akvo.caddisfly.R;

public class MessageFragment extends DialogFragment {

    public static MessageFragment newInstance(String title, Message msg) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBundle("message", msg.getData());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //getDialog().setTitle(R.string.result);

        final View view = inflater.inflate(R.layout.fragment_message, container, false);

        //TextView titleView = (TextView) view.findViewById(R.id.titleView);
        Button button = (Button) view.findViewById(R.id.endSurveyButton);

        final Bundle bundle = getArguments().getBundle("message");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ResultDialogListener listener = (ResultDialogListener) getActivity();
                dismiss();
                listener.onFinishDialog(bundle);

            }
        });

        //titleView.setText(getArguments().getString("title", ""));

        return view;
    }

    public interface ResultDialogListener {
        void onFinishDialog(Bundle bundle);
    }
}
