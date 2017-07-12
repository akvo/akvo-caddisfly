package org.akvo.caddisfly.sensor.cbt;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.akvo.caddisfly.R;

public class CompartmentBagMainActivity extends AppCompatActivity {

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

    }
}
