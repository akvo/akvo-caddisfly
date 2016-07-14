package org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.ColorimetryStripActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.camera_strip.CameraActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.instructions_strip.InstructionActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;

/**
 * An activity representing a single Instruction detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ColorimetryStripActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ColorimetryStripDetailFragment}.
 */
public class ColorimetryStripDetailActivity extends BaseActivity implements ColorimetryStripDetailFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_striptest_detail);
    }

    @Override
    public void onResume() {
        String brandName = getIntent().getStringExtra(Constant.BRAND);

        if (brandName == null) {
            Toast.makeText(this.getApplicationContext(), "Cannot proceed without brandName", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            ColorimetryStripDetailFragment fragment = ColorimetryStripDetailFragment.newInstance(brandName);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.choose_striptest_detail_container, fragment)
                    .commit();

        }

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startCameraActivity(String brandName) {

        Intent intent = new Intent(this, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.BRAND, brandName);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    public void startInstructionActivity(String brandName) {

        Intent intent = new Intent(this, InstructionActivity.class);
        intent.putExtra(Constant.BRAND, brandName);
        startActivity(intent);

    }
}
