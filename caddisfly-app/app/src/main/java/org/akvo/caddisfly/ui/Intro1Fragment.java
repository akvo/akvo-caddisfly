/*
 * Copyright (C) 2018 Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Flow.
 *
 * Akvo Flow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Flow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Flow.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.akvo.caddisfly.ui;

import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.akvo.caddisfly.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Intro1Fragment extends Fragment {

    public Intro1Fragment() {
    }

    public static Intro1Fragment newInstance() {
        return new Intro1Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intro_fragment_page1, container, false);
        ImageView imageview = view.findViewById(R.id.main_icon);
        ClipDrawable drawable = (ClipDrawable) imageview.getBackground();
        drawable.setLevel(9000);
        return view;
    }
}
