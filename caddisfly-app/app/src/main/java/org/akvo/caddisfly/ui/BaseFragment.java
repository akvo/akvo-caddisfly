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

package org.akvo.caddisfly.ui;

import android.view.View;
import android.widget.TextView;

import org.akvo.caddisfly.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    protected void setTitle(View view, String title) {
        if (getActivity() != null) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);

            if (toolbar != null) {
                try {
                    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
                } catch (Exception ignored) {
                    // do nothing
                }
            }

            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);

                TextView textTitle = view.findViewById(R.id.textToolbarTitle);
                if (textTitle != null) {
                    textTitle.setText(title);
                }
            }
        }
    }
}
