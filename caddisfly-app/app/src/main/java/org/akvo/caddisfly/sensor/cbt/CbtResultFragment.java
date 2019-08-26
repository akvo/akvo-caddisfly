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

package org.akvo.caddisfly.sensor.cbt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.ui.BaseFragment;
import org.akvo.caddisfly.util.StringUtil;

import java.util.Objects;

public class CbtResultFragment extends BaseFragment {

    private TextView textRisk1;
    private TextView textRisk2;
    private TextView textResult1;
    private TextView textResult2;
    private LinearLayout layoutRisk;
    private MpnValue mpnValue = null;
    private MpnValue mpnValue2 = null;
    private String result = "00000";
    private String result2 = "00000";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CbtResultFragment.
     */
    public static CbtResultFragment newInstance() {
        return new CbtResultFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cbt_result, container, false);

        if (mpnValue == null) {
            mpnValue = TestConfigHelper.getMpnValueForKey("00000", "0");
        }
        if (mpnValue2 == null) {
            mpnValue2 = TestConfigHelper.getMpnValueForKey("00000", "0");
        }

        textRisk1 = view.findViewById(R.id.textRisk1);
        textRisk2 = view.findViewById(R.id.textRisk2);
        textResult1 = view.findViewById(R.id.textResult1);
        textResult2 = view.findViewById(R.id.textResult2);
        layoutRisk = view.findViewById(R.id.layoutRisk);

        showResult();

        return view;
    }

    void setResult(String result, String sampleQuantity) {
        this.result = result;
        mpnValue = TestConfigHelper.getMpnValueForKey(result, sampleQuantity);
        showResult();
    }

    void setResult2(String result, String sampleQuantity) {
        this.result2 = result;
        mpnValue2 = TestConfigHelper.getMpnValueForKey(result, sampleQuantity);
        showResult();
    }

    private void showResult() {
        if (getActivity() != null) {
            String[] results = StringUtil.getStringResourceByName(Objects.requireNonNull(getActivity()),
                    mpnValue.getRiskCategory()).toString().split("/");

            textRisk1.setText(results[0].trim());
            if (results.length > 1) {
                textRisk2.setText(results[1].trim());
            }

            layoutRisk.setBackgroundColor(mpnValue.getBackgroundColor1());
            textResult1.setText(mpnValue.getMpn());
            textResult2.setText(mpnValue2.getMpn());
        }
    }

    String getResult() {
        return result;
    }

    String getResult2() {
        return result2;
    }
}
