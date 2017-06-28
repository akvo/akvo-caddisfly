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

package org.akvo.caddisfly.sensor.colorimetry.bluetooth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class InstructionFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InstructionFragment.
     */
    public static InstructionFragment newInstance() {
        return new InstructionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_instructions, container, false);
        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        ListView listView = (ListView) view.findViewById(R.id.list_instructions);

        InstructionListAdapter instructionsListAdapter = new InstructionListAdapter();

        JSONArray instructions = testInfo.getInstructions();
        if (instructions != null) {
            for (int i = 0; i < instructions.length(); i++) {
                try {

                    Object item = instructions.getJSONObject(i).get("text");
                    instructionsListAdapter.addInstruction(StringUtil.toInstruction(getActivity(), item.toString()));

                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }

        listView.setAdapter(instructionsListAdapter);

        return view;
    }

    private static class ViewHolder {
        private TextView textInstruction;
        private TextView textNumber;
    }

    // Adapter for holding devices found through scanning.
    private class InstructionListAdapter extends BaseAdapter {
        private final List<SpannableStringBuilder> mInstructions;
        private final LayoutInflater mInflater;

        InstructionListAdapter() {
            super();
            mInstructions = new ArrayList<>();
            mInflater = getActivity().getLayoutInflater();
        }

        private void addInstruction(SpannableStringBuilder instruction) {
            mInstructions.add(instruction);
        }

        @Override
        public int getCount() {
            return mInstructions.size();
        }

        @Override
        public Object getItem(int i) {
            return mInstructions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.item_instruction, null);
                viewHolder = new ViewHolder();
                viewHolder.textInstruction = (TextView) view.findViewById(R.id.textInstruction);
                viewHolder.textNumber = (TextView) view.findViewById(R.id.textNumber);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.textNumber.setText(String.format("%s.", position + 1));

            viewHolder.textInstruction.setText(mInstructions.get(position));

            return view;
        }
    }
}
