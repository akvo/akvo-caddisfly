package org.akvo.caddisfly.common;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.TestActivity;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.util.List;

/**
 * A utility class that handles navigation.
 */
public class NavigationController {

    private final Context context;

    public NavigationController(Context context) {
        this.context = context;
    }

    public void navigateToTestType(TestType testType) {

        final TestListViewModel viewModel =
                ViewModelProviders.of((FragmentActivity) context).get(TestListViewModel.class);

        Intent intent;
        List<TestInfo> tests = viewModel.getTests(testType);
        if (tests.size() == 1) {
            intent = new Intent(context, TestActivity.class);
            intent.putExtra(ConstantKey.TEST_INFO, tests.get(0));
        } else {
            intent = new Intent(context, TestListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ConstantKey.TYPE, testType);
        }

        intent.putExtra("internal", true);
        context.startActivity(intent);

    }
}
