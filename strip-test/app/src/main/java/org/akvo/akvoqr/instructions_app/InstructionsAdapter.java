package org.akvo.akvoqr.instructions_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.akvoqr.R;

import java.util.List;

/**
 * Created by linda on 9/5/15.
 */
public class InstructionsAdapter extends ArrayAdapter<Instructions.Instruction> {

    private List<Instructions.Instruction> instructions;
    private int resource;
    private Context context;

    public InstructionsAdapter(Context context, int resource, List<Instructions.Instruction> instructions) {
        super(context, resource);

        this.context = context;
        this.resource = resource;
        this.instructions = instructions;

    }

    @Override
    public int getCount() {
        return instructions.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;

        if(convertView==null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.textView.setText(instructions.get(position).getInstruction());

        if(position==0) {
            holder.imageView.setImageResource(R.drawable.progress_icon_top);
        }
        else {
            holder.imageView.setImageResource(R.drawable.progress_icon_center);
        }
        return view;

    }

    private static class ViewHolder
    {
        private ImageView imageView;
        private TextView textView;

        public ViewHolder(View v)
        {
            imageView = (ImageView) v.findViewById(R.id.adapter_instructionsImageView);
            textView = (TextView) v.findViewById(R.id.adapter_instructionsTextView);
        }
    }
}
