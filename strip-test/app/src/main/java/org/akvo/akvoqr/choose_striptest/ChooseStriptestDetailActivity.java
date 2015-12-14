package org.akvo.akvoqr.choose_striptest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import org.akvo.akvoqr.R;
import org.akvo.akvoqr.util.Constant;

/**
 * An activity representing a single Instruction detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ChooseStriptestListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ChooseStripTestDetailFragment}.
 */
public class ChooseStriptestDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_striptest_detail);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onResume()
    {
        String brandname = getIntent().getStringExtra(Constant.BRAND);

        System.out.println("*** ChooseStripTestDetailActivity onResume called with brandname: " + brandname);

        if(brandname==null)
        {
            Toast.makeText(this.getApplicationContext(), "Cannot proceed without brandname", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            ChooseStripTestDetailFragment fragment = ChooseStripTestDetailFragment.newInstance(brandname);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.choose_striptest_detail_container, fragment)
                    .commit();

        }

        super.onResume();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        System.out.println("***Expecting a call when FLAG_ACTIVITY_CLEAR_TOP is passed in the intent");

        super.onNewIntent(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ChooseStriptestListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
