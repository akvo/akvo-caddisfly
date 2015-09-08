package org.akvo.akvoqr.instructions_app;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.akvoqr.R;

/**
 * A fragment representing a single Instruction detail screen.
 * This fragment is either contained in a {@link InstructionListActivity}
 * in two-pane mode (on tablets) or a {@link InstructionDetailActivity}
 * on handsets.
 */
public class InstructionDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
//    private DummyContent.DummyItem mItem;
    private Instructions.Instruction instruction;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InstructionDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            instruction = Instructions.INSTRUCTION_MAP.get(getArguments().getString(ARG_ITEM_ID));

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_instruction_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (instruction != null) {
            TextView textView = ((TextView) rootView.findViewById(R.id.fragment_instruction_detailTextView));
            textView.setText(instruction.getInstruction());
            ImageView imageView = (ImageView) rootView.findViewById(R.id.fragment_instruction_detailImageView);
            System.out.println("***res type name: " + getResources().getResourceTypeName(instruction.getImageResId()));
            if(getResources().getResourceTypeName(instruction.getImageResId()).equals("drawable")) {

                imageView.setImageResource(instruction.getImageResId());
            }
            else if(getResources().getResourceTypeName(instruction.getImageResId()).equals("raw")) {

                textView.append("\n\nPlay Sound");
                imageView.setImageResource(R.drawable.button_play_sound);
                imageView.setPadding(0,10,0,0);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.futurebeep2);
                        mp.start();

                    }
                });

            }

        }

        return rootView;
    }
}
