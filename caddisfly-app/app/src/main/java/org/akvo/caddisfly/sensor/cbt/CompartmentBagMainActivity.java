package org.akvo.caddisfly.sensor.cbt;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseActivity;

public class CompartmentBagMainActivity extends BaseActivity implements CompartmentBagFragment.OnFragmentInteractionListener {

    private FragmentManager fragmentManager;
    private String mResult = "00000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartment_bag_main);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtInstructionsFragment.newInstance(), "cbtInstructions")
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickNextButton(View view) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CompartmentBagFragment.newInstance(), "compartmentFragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .commit();
    }

    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtResultFragment.newInstance(mResult), "resultFragment")
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .commit();
    }

    @Override
    public void onFragmentInteraction(String key) {
        mResult = key;
    }

    public void onClickAcceptResult(View view) {
        finish();

    }
}
