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

/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.akvo.caddisfly.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.FragmentTestDetailBinding;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;

public class TestInfoFragment extends Fragment {

    private FragmentTestDetailBinding b;

    /**
     * Creates test fragment for specific uuid
     */
    public static TestInfoFragment forProduct(TestInfo testInfo) {
        TestInfoFragment fragment = new TestInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data b layout
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_test_detail, container, false);

        final TestInfoViewModel model =
                ViewModelProviders.of(this).get(TestInfoViewModel.class);

        TestInfo testInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);

        if (testInfo != null) {

            if (AppPreferences.isDiagnosticMode()) {
                b.swatchView.setVisibility(View.VISIBLE);
                b.swatchView.setPatch(testInfo);
            }

            model.setTest(testInfo);

            b.setTestInfoViewModel(model);

            b.setTestInfo(testInfo);

            if (testInfo.getSubtype() == TestType.COLORIMETRIC_STRIP) {
                b.buttonPrepare.setText(R.string.prepare_test);
            }

            getActivity().setTitle(testInfo.getName());

        }

        return b.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


////        TestInfoViewModel.Factory factory = new TestInfoViewModel.Factory(
////                getActivity().getApplication(), getArguments().getInt(KEY_PRODUCT_ID));
////
////        final TestInfoViewModel model = ViewModelProviders.of(this, factory)
////                .get(ProductViewModel.class);
//
    }
}
