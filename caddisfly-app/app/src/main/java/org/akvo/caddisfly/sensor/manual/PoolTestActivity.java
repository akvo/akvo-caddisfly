package org.akvo.caddisfly.sensor.manual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.common.SensorConstants;
import org.akvo.caddisfly.helper.TestConfigHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;
import org.json.JSONObject;

public class PoolTestActivity extends BaseActivity
        implements SwatchSelectFragment.OnSwatchSelectListener {

    private FragmentManager fragmentManager;
    private float[] poolTestResults;
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

        startTest();
    }

    private void startTest() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,
                SwatchSelectFragment.newInstance(poolTestResults), "swatchSelect")
                .commit();
    }

    public void onSwatchSelect(float[] key) {
        poolTestResults = key;
        testInfo.getResults().get(0).setResultValue(key[0]);
        testInfo.getResults().get(1).setResultValue(key[1]);
    }

    @SuppressWarnings("unused")
    public void onClickMatchedButton(View view) {
        if (poolTestResults == null || poolTestResults[0] == 0 || poolTestResults[1] == 0) {

            Toast toast = Toast.makeText(this, R.string.select_colors_before_continue,
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 300);

            View view1 = toast.getView();
            if (view1 != null) {
                view1.setPadding(20, 20, 20, 20);
                view1.setBackgroundResource(R.color.error_background);
            }

            toast.show();
        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,
                    PoolTestResultFragment.newInstance(testInfo), "resultFragment")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @SuppressWarnings("unused")
    public void onClickAcceptResult(View view) {

        SparseArray<String> results = new SparseArray<>();

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
