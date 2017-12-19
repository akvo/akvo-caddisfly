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

import android.databinding.BindingAdapter;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.Instruction;
import org.akvo.caddisfly.preference.AppPreferences;

import java.util.List;


public class BindingAdapters {
    @BindingAdapter("hideInNormalMode")
    public static void setHideInNormalMode(View view, boolean value) {
        view.setVisibility(AppPreferences.isDiagnosticMode() ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("background")
    public static void setBackground(View view, String dummy) {
        if (AppPreferences.isDiagnosticMode()) {
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.diagnostic));
        } else {
            TypedValue typedValue = new TypedValue();
            view.getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            view.setBackgroundColor(color);
        }
    }

    @BindingAdapter("instructionLinkVisibility")
    public static void setInstructionLinkVisibility(View view, List<Instruction> instructions) {
        view.setVisibility(instructions == null || instructions.size() == 0 ? View.INVISIBLE : View.VISIBLE);
    }


}