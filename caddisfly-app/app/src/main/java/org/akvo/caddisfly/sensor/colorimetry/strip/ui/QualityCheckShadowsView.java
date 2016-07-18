/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.strip.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by linda on 11/5/15
 */
public class QualityCheckShadowsView extends QualityCheckView {


    public QualityCheckShadowsView(Context context) {
        super(context);
    }

    public QualityCheckShadowsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QualityCheckShadowsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //set percentage to a value that results in a negative at the qualityChecksOK of the onDraw
        percentage = 101;
    }

    @Override
    protected double fromPercentageToNumber() {
        // calculate the percentage back to value that fits NUMBER_OF_BARS
        // we want the number to range between 0 (= heavy shadow) and 6 (no shadow)
        return (100 - percentage) * NUMBER_OF_BARS * 0.01;
    }
}
