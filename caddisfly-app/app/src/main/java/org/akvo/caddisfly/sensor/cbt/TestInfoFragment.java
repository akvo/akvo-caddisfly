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

package org.akvo.caddisfly.sensor.cbt;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.TestDetailFragmentBinding;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.viewmodel.TestInfoViewModel;

public class TestInfoFragment extends Fragment {

    private static final String KEY_PRODUCT_ID = "product_id";

    private TestDetailFragmentBinding mBinding;

    /**
     * Creates test fragment for specific uuid
     */
    public static TestInfoFragment forProduct(TestInfo testInfo) {
        TestInfoFragment fragment = new TestInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PRODUCT_ID, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.test_detail_fragment, container, false);

        return mBinding.getRoot();
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


        final TestInfoViewModel model = new TestInfoViewModel();
        //ViewModelProviders.of(this).get(TestInfoViewModel.class);

        TestInfo testInfo = getArguments().getParcelable(KEY_PRODUCT_ID);

        model.setTest(testInfo);

        mBinding.setTestInfoViewModel(model);

        mBinding.setTestInfo(testInfo);

        if (testInfo != null) {
            getActivity().setTitle(testInfo.getName());
        }
    }

}
