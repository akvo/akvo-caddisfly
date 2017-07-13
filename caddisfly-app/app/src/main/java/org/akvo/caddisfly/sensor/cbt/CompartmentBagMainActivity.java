package org.akvo.caddisfly.sensor.cbt;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseActivity;

public class CompartmentBagMainActivity extends BaseActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartment_bag_main);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CompartmentBagFragment.newInstance(), "compartmentFragment")
                .setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
                .commit();

        setTitle("Set compartment colors");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
