package org.akvo.caddisfly.sensor.colorimetry.strip.instructions_strip;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.json.JSONException;

public class InstructionBrandDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id"; //NON-NLS
    private String instructionText;
    private Drawable instructionDrawable;
    private InstructionsListener listener;

    public static InstructionBrandDetailFragment newInstance(int itemid)
    {
        InstructionBrandDetailFragment fragment = new InstructionBrandDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ITEM_ID, itemid);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstructionBrandDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            int id = getArguments().getInt(ARG_ITEM_ID);
            try {
                instructionText = listener.getInstruction(id);
                instructionDrawable = listener.getInstructionDrawable(id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            listener = (InstructionsListener) activity;
        }
        catch (ClassCastException e)
        {
            throw  new ClassCastException("must implement InstructionActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instruction_detail, container, false);

        if (instructionText != null) {
            TextView textView = ((TextView) rootView.findViewById(R.id.fragment_instruction_detailTextView));
            textView.setText(instructionText);
        }

        if(instructionDrawable != null) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.fragment_instruction_detailImageView);
            imageView.setImageDrawable(instructionDrawable);
        }

        return rootView;
    }
}
