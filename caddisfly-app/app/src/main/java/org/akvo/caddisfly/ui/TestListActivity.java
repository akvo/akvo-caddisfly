/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.databinding.ActivityTestListBinding;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.ErrorMessages;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.repository.TestConfigRepository;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

public class TestListActivity extends BaseActivity
        implements TestListFragment.OnListFragmentInteractionListener {

    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private TestInfo testInfo;
    private ActivityTestListBinding b;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionsDelegate.resultGranted(grantResults)) {
            startTest();
        } else {
            String message = getString(R.string.cameraAndStoragePermissions);

            Snackbar snackbar = Snackbar
                    .make(b.mainLayout, message,
                            Snackbar.LENGTH_LONG)
                    .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(this));

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);

            snackbar.setActionTextColor(typedValue.data);
            View snackView = snackbar.getView();
            TextView textView = snackView.findViewById(R.id.snackbar_text);
            textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
            textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = DataBindingUtil.setContentView(this, R.layout.activity_test_list);

        setTitle(R.string.selectTest);

        if (getIntent().getData() != null && ApkHelper.isNonStoreVersion(this)) {
            String uuid = getIntent().getData().getQueryParameter("id");
            if (uuid != null && !uuid.isEmpty()) {
                final Intent intent = new Intent(getBaseContext(), TestActivity.class);
                final TestListViewModel viewModel =
                        ViewModelProviders.of(this).get(TestListViewModel.class);
                testInfo = viewModel.getTestInfo(uuid);
                intent.putExtra(ConstantKey.TEST_INFO, testInfo);
                startActivity(intent);
            }
            finish();
        }

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {

            TestType testType = (TestType) getIntent().getSerializableExtra(ConstantKey.TYPE);

            TestListFragment fragment = TestListFragment.newInstance(testType);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, "TestListViewModel").commit();
        }
    }

    /**
     * Shows the detail fragment.
     */
    private void navigateToTestDetails() {
        startTest();
    }

    private void startTest() {

        testInfo = (new TestConfigRepository()).getTestInfo(testInfo.getUuid());

        if (testInfo == null || testInfo.getIsGroup()) {
            return;
        }

        if (testInfo.getResults().size() == 0) {
            ErrorMessages.alertCouldNotLoadConfig(this);
            return;
        }

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(ConstantKey.TEST_INFO, testInfo);
        intent.putExtra("internal", true);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(TestInfo testInfo) {
        this.testInfo = testInfo;
        navigateToTestDetails();
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
