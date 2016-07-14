package org.akvo.caddisfly.sensor.colorimetry.strip;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.ColorimetryStripDetailActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.ColorimetryStripDetailFragment;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.ColorimetryStripListFragment;
import org.akvo.caddisfly.sensor.colorimetry.strip.colorimetry_strip.StripTest;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.ui.BaseActivity;


/**
 * An activity representing a list of Instructions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ColorimetryStripDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ColorimetryStripListFragment} and the item details
 * (if present) is a {@link ColorimetryStripDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ColorimetryStripListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ColorimetryStripActivity extends BaseActivity
        implements ColorimetryStripListFragment.Callbacks, BaseActivity.ResultListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ColorimetryStripListFragment chooseStripTestListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_striptest_list);

        setTitle(R.string.selectTest);

        //set result listener
        BaseActivity.setResultListener(this);

        if (savedInstanceState == null) {
            if (chooseStripTestListFragment == null) {
                chooseStripTestListFragment = new ColorimetryStripListFragment();
            }
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.activity_choose_striptestFragmentPlaceholder, chooseStripTestListFragment)
                    .commit();
        }

        if (findViewById(R.id.choose_striptest_detail_container) != null) {
            // The detail conStainer view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-w600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }
    }

    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String cadUuid = null;
        if (intent.hasExtra("caddisflyResourceUuid")) {
            cadUuid = intent.getStringExtra("caddisflyResourceUuid");
        }
        if (intent.getBooleanExtra(SensorConstants.FINISH, false)) {
            finish();
        } else if (cadUuid != null && cadUuid.length() > 0) {
            // when we get back here, we want to go straight back to the FLOW app
            intent.putExtra(SensorConstants.FINISH, true);

            // find brand which goes with this test uuid
            // and if found, go there immediately.
            StripTest stripTest = new StripTest();
            String brand = stripTest.matchUuidToBrand(cadUuid);
            if (brand != null && brand.length() > 0)
                onItemSelected(brand);
        }
    }

    /**
     * Callback method from {@link ColorimetryStripListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {

            // In two-pane mode, list items should be given the 'activated' state when touched.
            if (chooseStripTestListFragment != null)
                chooseStripTestListFragment.setActivateOnItemClick(true);

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            ColorimetryStripDetailFragment fragment = ColorimetryStripDetailFragment.newInstance(id);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.choose_striptest_detail_container, fragment)
                    .commit();

        } else {

            // In single-pane mode, simply start the detail activity for the selected item ID.
            Intent detailIntent = new Intent(this, ColorimetryStripDetailActivity.class);
            detailIntent.putExtra(Constant.BRAND, id);
            detailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(detailIntent);
        }
    }

    @Override
    public void onResult(String result, String imagePath) {

        Intent intent = new Intent(getIntent());
        intent.putExtra("response", result);
        intent.putExtra("image", imagePath);
        setResult(Activity.RESULT_OK, intent);

        finish();
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
}
