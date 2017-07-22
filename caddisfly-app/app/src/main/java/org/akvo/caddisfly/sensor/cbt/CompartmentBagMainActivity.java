package org.akvo.caddisfly.sensor.cbt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

public class CompartmentBagMainActivity extends BaseActivity implements CompartmentBagFragment.OnFragmentInteractionListener {

    private final SparseArray<String> results = new SparseArray<>();
    private FragmentManager fragmentManager;
    private String mResult = "00000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compartment_bag_main);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtInstructionsFragment.newInstance(), "cbtInstructions")
                .commit();
    }

    public void onClickNextButton(View view) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CompartmentBagFragment.newInstance(mResult), "compartmentFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();
    }

    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtCameraFragment.newInstance(mResult), "cameraFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();
    }

    public void onClickShowResult(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutFragment, CbtResultFragment.newInstance(mResult), "resultFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFragmentInteraction(String key) {
        mResult = key;
    }

    public void onClickAcceptResult(View view) {

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();

        Intent resultIntent = new Intent(getIntent());

        results.clear();

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(mResult);

        results.put(1, mpnValue.getMpn());
        results.put(2, StringUtil.getStringResourceByName(this, mpnValue.getRiskCategory()).toString());

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, -1, "", null);
        resultIntent.putExtra(SensorConstants.RESPONSE, resultJson.toString());

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
