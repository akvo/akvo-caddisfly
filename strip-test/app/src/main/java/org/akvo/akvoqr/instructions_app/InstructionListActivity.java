package org.akvo.akvoqr.instructions_app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import org.akvo.akvoqr.CameraActivity;
import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.Constant;


/**
 * An activity representing a list of Instructions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link InstructionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link InstructionListFragment} and the item details
 * (if present) is a {@link InstructionDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link InstructionListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class InstructionListActivity extends FragmentActivity
        implements InstructionListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_list);

        if (findViewById(R.id.instruction_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((InstructionListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.instruction_list))
                    .setActivateOnItemClick(true);
        }

        Intent intent = getIntent();
        final String brandname = intent.getStringExtra(Constant.BRAND);

        Button startButton = (Button) findViewById(R.id.activity_instruction_listStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                Intent intent = new Intent(InstructionListActivity.this, CameraActivity.class);
                intent.putExtra(Constant.BRAND, brandname);
                startActivity(intent);
            }
        });

    }

    /**
     * Callback method from {@link InstructionListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(InstructionDetailFragment.ARG_ITEM_ID, id);
            InstructionDetailFragment fragment = new InstructionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.instruction_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, InstructionDetailActivity.class);
            detailIntent.putExtra(InstructionDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
