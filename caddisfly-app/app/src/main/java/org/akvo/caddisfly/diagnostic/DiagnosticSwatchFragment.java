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

package org.akvo.caddisfly.diagnostic;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;

@SuppressWarnings("WeakerAccess")
public class DiagnosticSwatchFragment extends ListFragment {

    private static final String ARG_TEST_INFO = "testInfo";
    TestInfo testInfo;

    public static DiagnosticSwatchFragment newInstance(TestInfo testInfo) {
        DiagnosticSwatchFragment fragment = new DiagnosticSwatchFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            testInfo = getArguments().getParcelable(ARG_TEST_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swatch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            getActivity().setTitle(R.string.swatches);
        }

        if (testInfo.getSwatches().size() > 0) {
            DiagnosticSwatchesAdapter diagnosticSwatchesAdapter =
                    new DiagnosticSwatchesAdapter(getActivity(), testInfo.getSwatches());
            setListAdapter(diagnosticSwatchesAdapter);
        }
    }
}
