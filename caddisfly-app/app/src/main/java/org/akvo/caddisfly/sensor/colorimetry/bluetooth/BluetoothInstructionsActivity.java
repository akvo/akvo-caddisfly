package org.akvo.caddisfly.sensor.colorimetry.bluetooth;

import android.os.Bundle;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.BaseActivity;

public class BluetoothInstructionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_instructions);

        setTitle("Instructions");
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
