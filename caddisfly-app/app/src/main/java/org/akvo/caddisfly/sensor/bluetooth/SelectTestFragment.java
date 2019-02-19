package org.akvo.caddisfly.sensor.bluetooth;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.util.StringUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class SelectTestFragment extends Fragment {

    private TestInfo testInfo;

    public static SelectTestFragment getInstance(TestInfo testInfo) {
        SelectTestFragment fragment = new SelectTestFragment();
        Bundle args = new Bundle();
        args.putParcelable(ConstantKey.TEST_INFO, testInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_test, container, false);

        if (getArguments() != null) {
            testInfo = getArguments().getParcelable(ConstantKey.TEST_INFO);
        }

        SpannableStringBuilder selectionInstruction = StringUtil.toInstruction((AppCompatActivity) getActivity(), testInfo,
                String.format(StringUtil.getStringByName(getContext(), testInfo.getSelectInstruction()),
                        StringUtil.convertToTags(testInfo.getMd610Id()), testInfo.getName()));

        ((TextView) view.findViewById(R.id.textSelectInstruction)).setText(selectionInstruction);

        return view;
    }

}
