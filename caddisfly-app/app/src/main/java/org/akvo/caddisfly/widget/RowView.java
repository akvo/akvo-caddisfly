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

package org.akvo.caddisfly.widget;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.akvo.caddisfly.R;

/**
 * A single numbered row for a list.
 */
public class RowView extends TableRow {
    private final TextView textNumber;
    private final TextView textPara;

    /**
     * A single numbered row for a list.
     *
     * @param context the context
     * @param attrs the attributeSet
     */
    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            inflater.inflate(R.layout.row_view, this, true);
        }

        TableRow tableRow = (TableRow) getChildAt(0);

        textNumber = (TextView) tableRow.getChildAt(0);
        textPara = (TextView) tableRow.getChildAt(1);
    }

    public RowView(Context context) {
        this(context, null);
    }

    public void setNumber(String s) {
        textNumber.setText(s);
    }

    public void append(Spanned s) {
        textPara.append(s);
    }

    public String getString() {
        return textPara.getText().toString();
    }

    public void enableLinks(boolean b) {
        textPara.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
