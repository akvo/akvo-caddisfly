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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.databinding.ActivityMainBinding;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.AnimatedColor;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity {

    private static int APP_UPDATE_REQUEST = 101;
    private AppUpdateManager appUpdateManager;

    private final WeakRefHandler refreshHandler = new WeakRefHandler(this);
    private ActivityMainBinding b;
    private AnimatedColor statusBarColors;
    private int INTRO_PAGE_COUNT = 2;


    private void checkForUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        InstallStateUpdatedListener listener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        };
        appUpdateManager.registerListener(listener);

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            APP_UPDATE_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.mainLayout),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
//        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        View snackView = snackbar.getView();
        TextView textView = snackView.findViewById(R.id.snackbar_text);
        textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        appUpdateManager = AppUpdateManagerFactory.create(this);

        b.pageIndicator.setPageCount(INTRO_PAGE_COUNT);

        setTitle(R.string.appName);

        try {
            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        hideActionBar();

        b.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                b.pageIndicator.setActiveIndex(position);
                if (position == 1) {
                    b.buttonNext.setVisibility(View.GONE);
                    b.buttonOk.setVisibility(View.VISIBLE);
                } else {
                    b.buttonNext.setVisibility(View.VISIBLE);
                    b.buttonOk.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        b.buttonInfo.setOnClickListener(view -> {
            final Intent intent = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(intent);
        });

        (new Handler()).post(this::setUpViews);

        checkForUpdate();
    }

    private void hideActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    private void setUpViews() {
        b.viewPager.setAdapter(new IntroFragmentAdapter(getSupportFragmentManager()));
        b.pageIndicator.setActiveIndex(0);
        if (b.viewPager.getCurrentItem() == 1) {
            b.buttonNext.setVisibility(View.GONE);
            b.buttonOk.setVisibility(View.VISIBLE);
        } else {
            b.buttonNext.setVisibility(View.VISIBLE);
            b.buttonOk.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (AppPreferences.isDiagnosticMode()) {
                statusBarColors = new AnimatedColor(
                        ContextCompat.getColor(this, R.color.colorPrimaryDark),
                        ContextCompat.getColor(this, R.color.diagnostic_status));
            } else {
                statusBarColors = new AnimatedColor(
                        ContextCompat.getColor(this, R.color.colorPrimaryDark),
                        ContextCompat.getColor(this, R.color.black_main));
            }
            animateStatusBar();
        }

        CaddisflyApp.getApp().setAppLanguage(this, null, false, refreshHandler);

        if (PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.removeKey(this, R.string.refreshKey);
            refreshHandler.sendEmptyMessage(0);
            return;
        }

        (new Handler()).postDelayed(this::setUpViews, 300);
    }

    public void onSettingsClick(@SuppressWarnings("unused") MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_main_diagnostic, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    public void onNextClicked(@SuppressWarnings("unused") View view) {
        b.viewPager.setCurrentItem(1, true);
    }

    public void onOkClicked(@SuppressWarnings("unused") View view) {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            closeApp(1000);
        } else {
            alertDependantAppNotFound();
        }
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s\r\n\r\n%s", "Akvo Flow is not installed.",
                "Please install the Akvo Flow app from your instance.");

        AlertUtil.showAlert(this, R.string.notFound, message, R.string.close,
                (dialogInterface, i) -> closeApp(0),
                null, null);
    }

    private void closeApp(int delay) {
        (new Handler()).postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        }, delay);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void animateStatusBar() {
        ValueAnimator animator = ObjectAnimator.ofFloat(0f, 1f).setDuration(1000);
        animator.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
            getWindow().setStatusBarColor(statusBarColors.with(v));
        });
        animator.start();
    }

    /**
     * Handler to restart the app after language has been changed.
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (b.viewPager.getCurrentItem() > 0) {
            b.viewPager.setCurrentItem(b.viewPager.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }

    class IntroFragmentAdapter extends FragmentPagerAdapter {

        private static final int FIRST_PAGE = 0;

        IntroFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == FIRST_PAGE) {
                return Intro1Fragment.newInstance();
            } else {
                return Intro2Fragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return INTRO_PAGE_COUNT;
        }

        public int getItemPosition(@NotNull Object object) {
            return POSITION_NONE;
        }
    }
}

