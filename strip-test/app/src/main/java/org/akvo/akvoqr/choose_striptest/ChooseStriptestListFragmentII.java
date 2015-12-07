package org.akvo.akvoqr.choose_striptest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.akvo.akvoqr.R;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callbacks} interface
 * to handle interaction events.
 */
public class ChooseStriptestListFragmentII extends ListFragment {

    private Callbacks mListener;
    private ChooseStriptestAdapter adapter;
    private StripTest stripTest;
    private List<String> brandnames;

    public ChooseStriptestListFragmentII() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_choose_striptest_list, container, false);

        if(stripTest==null)
            stripTest = StripTest.getInstance(getActivity());

        brandnames = stripTest.getBrandsAsList();

        if(brandnames!=null) {
            //order alpha-numeric on brand (same as the name of the .png file in assets)
            Collections.sort(brandnames);

            if (adapter == null) {
                adapter = new ChooseStriptestAdapter(getActivity(),
                        R.layout.adapter_choose_striptest, brandnames);
            }
            setListAdapter(adapter);
        }
        return rootview;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        setListAdapter(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks) {
            mListener = (Callbacks) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        mListener.onItemSelected(brandnames.get(position));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface Callbacks {

        void onItemSelected(String brandname);
    }
}
