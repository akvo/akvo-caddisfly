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

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.databinding.TestItemBinding;
import org.akvo.caddisfly.model.TestInfo;

import java.util.List;

public class TestInfoAdapter extends RecyclerView.Adapter<TestInfoAdapter.TestInfoViewHolder> {

    @Nullable
    private final TestInfoClickCallback mTestInfoClickCallback;
    private List<? extends TestInfo> mTestList;

    public TestInfoAdapter(@Nullable TestInfoClickCallback clickCallback) {
        mTestInfoClickCallback = clickCallback;
    }

    public void setTestList(final List<? extends TestInfo> testList) {
        if (mTestList == null) {
            mTestList = testList;
            notifyItemRangeInserted(0, testList.size());
        }
    }

    @Override
    public TestInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TestItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.test_item,
                        parent, false);
        binding.setCallback(mTestInfoClickCallback);
        return new TestInfoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TestInfoViewHolder holder, int position) {
        holder.binding.setTestInfo(mTestList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mTestList == null ? 0 : mTestList.size();
    }

    public TestInfo getItemAt(int i) {
        return mTestList.get(i);
    }

    static class TestInfoViewHolder extends RecyclerView.ViewHolder {

        final TestItemBinding binding;

        TestInfoViewHolder(TestItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
