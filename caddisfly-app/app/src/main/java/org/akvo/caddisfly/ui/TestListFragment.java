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

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.FragmentListBinding;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.List;

public class TestListFragment extends LifecycleFragment {

    public static final String TAG = "TestListViewModel";

    private OnListFragmentInteractionListener mListener;

    private final TestInfoClickCallback mTestInfoClickCallback = test -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Runnable runnable = () -> mListener.onListFragmentInteraction(test);
            (new Handler()).postDelayed(runnable, 200);
        }
    };
    private TestType mTestType;
    private TestInfoAdapter mTestInfoAdapter;

    public static TestListFragment newInstance(TestType testType) {

        TestListFragment fragment = new TestListFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", testType);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mTestType = (TestType) getArguments().get("type");

        FragmentListBinding b = DataBindingUtil.inflate(inflater,
                R.layout.fragment_list, container, false);

        mTestInfoAdapter = new TestInfoAdapter(mTestInfoClickCallback);

        b.listTypes.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        b.listTypes.setHasFixedSize(true);
        b.listTypes.setAdapter(mTestInfoAdapter);

        return b.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final TestListViewModel viewModel =
                ViewModelProviders.of(this).get(TestListViewModel.class);

        List<TestInfo> tests = viewModel.getTests(mTestType);
        if (tests.size() == 1) {
            mListener.onListFragmentInteraction(tests.get(0));
        } else {
            mTestInfoAdapter.setTestList(tests);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCalibrationSelectedListener");
        }
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(TestInfo testInfo);
    }
}
