package org.akvo.akvoqr.choose_striptest;

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
public class ChooseStriptestAdapter extends ArrayAdapter<String> {

    private List<String> instructions;
    private int resource;
    private Context context;

    public ChooseStriptestAdapter(Context context, int resource, List<String> instructions) {
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

        StripTest.Brand brand = StripTest.getInstance().getBrand(instructions.get(position));
        List<StripTest.Brand.Patch> patches = brand.getPatches();
        String subtext = "";
        for(int i=0;i<patches.size();i++)
        {
            subtext += patches.get(i).getDesc() + ", ";
        }
        int indexLastSep = subtext.lastIndexOf(",");
        subtext = subtext.substring(0, indexLastSep);
        holder.textView.setText(brand.getName());

        holder.subtextView.setText(subtext);

//        if(position==0) {
//            holder.imageView.setImageResource(R.drawable.progress_icon_top);
//        }
//        else {
//            holder.imageView.setImageResource(R.drawable.progress_icon_center);
//        }
        return view;

    }

    private static class ViewHolder
    {
        private ImageView imageView;
        private TextView textView;
        private TextView subtextView;

        public ViewHolder(View v)
        {
            imageView = (ImageView) v.findViewById(R.id.adapter_instructionsImageView);
            textView = (TextView) v.findViewById(R.id.adapter_instructionsTextView);
            subtextView = (TextView) v.findViewById(R.id.adapter_instructionsSubTextView);

        }
    }
}
