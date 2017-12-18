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

package org.akvo.caddisfly.sensor.liquid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;

public class ChamberBelowFragment extends BaseRunTest implements RunTest {

    public ChamberBelowFragment() {
        // Required empty public constructor
    }

    public static ChamberBelowFragment newInstance(TestInfo testInfo) {
        ChamberBelowFragment fragment = new ChamberBelowFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initializeTest() {
        super.initializeTest();
        binding.imageIllustration.setVisibility(View.GONE);
        binding.circleView.setVisibility(View.GONE);

        if (!cameraStarted) {

            setupCamera();

            cameraStarted = true;

            camera.start();

            binding.cameraView.setOnClickListener(v -> {
                binding.cameraView.setOnClickListener(null);
                startRepeatingTask();
            });
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return binding.getRoot();
    }
}
