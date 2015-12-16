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

    private List<String> brandnames;
    private int resource;
    private Context context;
    private StripTest stripTest;
    private StripTest.Brand brand;
    private List<StripTest.Brand.Patch> patches;

    public ChooseStriptestAdapter(Context context, int resource, List<String> brandnames) {
        super(context, resource);

        this.context = context;
        this.resource = resource;
        this.brandnames = brandnames;
        this.stripTest = new StripTest();
    }

    @Override
    public int getCount() {
        return brandnames.size();
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

        if(brandnames!=null) {
            if(stripTest!=null) {
               brand = stripTest.getBrand(context, brandnames.get(position));
            }
            if(brand!=null) {
                patches = brand.getPatches();

                if(patches!=null && patches.size()>0) {
                    String subtext = "";
                    for (int i = 0; i < patches.size(); i++) {
                        subtext += patches.get(i).getDesc() + ", ";
                    }
                    int indexLastSep = subtext.lastIndexOf(",");
                    subtext = subtext.substring(0, indexLastSep);
                    holder.textView.setText(brand.getName());

                    holder.subtextView.setText(subtext);
                }
            }
            else holder.textView.setText(brandnames.get(position));
        }
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
