package org.akvo.akvoqr.choose_striptest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.Constant;


/**
 * An activity representing a list of Instructions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ChooseStriptestDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ChooseStriptestListFragment} and the item details
 * (if present) is a {@link ChooseStripTestDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ChooseStriptestListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ChooseStriptestListActivity extends AppCompatActivity
        implements ChooseStriptestListFragmentII.OnFragmentInteractionListener{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Fragment chooseStripTestListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_striptest_list);

        final int memClass = ((ActivityManager) this.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
        System.out.println("***Available memory: " + memClass);

        if(savedInstanceState==null) {
            if(chooseStripTestListFragment==null) {
                chooseStripTestListFragment = new ChooseStriptestListFragmentII();
            }
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.activity_choose_striptestFragmentPlaceholder, chooseStripTestListFragment)
                    .commit();
        }

//        if (findViewById(R.id.choose_striptest_detail_container) != null) {
//            // The detail conStainer view will be present only in the
//            // large-screen layouts (res/values-large and
//            // res/values-sw600dp). If this view is present, then the
//            // activity should be in two-pane mode.
//            mTwoPane = true;
//            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            // In two-pane mode, list items should be given the
//            // 'activated' state when touched.
//            ((ChooseStriptestListFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.choose_striptest_list))
//                    .setActivateOnItemClick(true);
//        }

    }

    /**
     * Callback method from {@link ChooseStriptestListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */

    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            ChooseStripTestDetailFragment fragment = ChooseStripTestDetailFragment.newInstance(id);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.choose_striptest_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply qualityChecksOK the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ChooseStriptestDetailActivity.class);
            detailIntent.putExtra(Constant.BRAND, id);
            startActivity(detailIntent);

        }
    }

    @Override
    public void onFragmentInteraction(String brandname) {
        Intent detailIntent = new Intent(this, ChooseStriptestDetailActivity.class);
        detailIntent.putExtra(Constant.BRAND, brandname);
        startActivity(detailIntent);
    }
}
