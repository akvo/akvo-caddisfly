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
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView listView;

    private OnFragmentInteractionListener mListener;

    public InstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InstructionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructionFragment newInstance(String param1, String param2) {
        InstructionFragment fragment = new InstructionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_instructions, container, false);
        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        listView = (ListView) view.findViewById(R.id.list_instructions);

        InstructionListAdapter instructionsListAdapter = new InstructionListAdapter();

        JSONArray instructions = testInfo.getInstructions();
        if (instructions != null) {
            for (int i = 0; i < instructions.length(); i++) {
                try {

                    Object item = instructions.getJSONObject(i).get("text");

                    instructionsListAdapter.addInstruction((String) item);

                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }

        listView.setAdapter(instructionsListAdapter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
            //                  + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private static class ViewHolder {
        private TextView textInstruction;
        private TextView textNumber;
    }

    // Adapter for holding devices found through scanning.
    private class InstructionListAdapter extends BaseAdapter {
        private final List<String> mInstructions;
        private final LayoutInflater mInflater;

        InstructionListAdapter() {
            super();
            mInstructions = new ArrayList<>();
            mInflater = getActivity().getLayoutInflater();
        }

        void addInstruction(String instruction) {
            mInstructions.add(instruction);
        }

        void clear() {
            mInstructions.clear();
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
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
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

            Spanned spanned = StringUtil.getStringResourceByName(getActivity(), mInstructions.get(position));
            viewHolder.textInstruction.setText(spanned);

            return view;
        }
    }
}
