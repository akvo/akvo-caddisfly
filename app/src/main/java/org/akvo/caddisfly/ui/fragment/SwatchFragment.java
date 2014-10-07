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

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.adapter.SwatchesAdapter;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.model.ColorInfo;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class SwatchFragment extends ListFragment {

    public SwatchFragment() {
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swatch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.swatches);

        Activity activity = getActivity();
        if (activity != null) {

            MainApp mainApp = (MainApp) activity.getApplicationContext();

            if (mainApp != null) {
                ArrayList<Integer> swatchList = new ArrayList<Integer>();
                for (ColorInfo aColorList : mainApp.colorList) {
                    swatchList.add(aColorList.getColor());
                }

                Integer[] colorArray = swatchList.toArray(new Integer[swatchList.size()]);

                SwatchesAdapter swatchesAdapter = new SwatchesAdapter(getActivity(), colorArray);
                setListAdapter(swatchesAdapter);
            }
        }
    }

}
