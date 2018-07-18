package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.MpnValue;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONObject;

public class PoolTestActivity extends BaseActivity
        implements SwatchSelectFragment.OnSwatchSelectListener {

    private FragmentManager fragmentManager;
    private String cbtResult = "00000";
    private TestInfo testInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cbt);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);
        }

        setTitle(testInfo.getName());

        startCbtTest();
    }

    private void startCbtTest() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                SwatchSelectFragment.newInstance(cbtResult), "swatchSelect")
                .addToBackStack(null)
                .commit();
    }

    public void onCompartmentBagSelect(String key) {
        cbtResult = key;
    }

    @SuppressWarnings("unused")
    public void onClickMatchedButton(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                PoolTestResultFragment.newInstance(cbtResult), "resultFragment")
                .addToBackStack(null)
                .commit();
    }


    @SuppressWarnings("unused")
    public void onClickAcceptResult(View view) {

        SparseArray<String> results = new SparseArray<>();

        MpnValue mpnValue = TestConfigHelper.getMpnValueForKey(cbtResult);

        results.put(1, StringUtil.getStringResourceByName(this, mpnValue.getRiskCategory()).toString());
        results.put(2, mpnValue.getMpn());
        results.put(3, mpnValue.getConfidence());

        JSONObject resultJson = TestConfigHelper.getJsonResult(testInfo, results, null, null);

        Intent resultIntent = new Intent();
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
