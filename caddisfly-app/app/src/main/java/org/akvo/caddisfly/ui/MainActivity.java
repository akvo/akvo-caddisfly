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

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.databinding.ActivityMainBinding;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity {

    private final WeakRefHandler refreshHandler = new WeakRefHandler(this);
    ActivityMainBinding b;

    private int INTRO_PAGE_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CaddisflyApp.getApp().setAppLanguage(null, false, null);

        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setTitle(R.string.appName);

        try {
            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        hideActionBar();
        setUpViews();

        b.pageIndicator.setPageCount(INTRO_PAGE_COUNT);

        b.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    b.buttonNext.setVisibility(View.GONE);
                    b.buttonOk.setVisibility(View.VISIBLE);
                } else {
                    b.buttonNext.setVisibility(View.VISIBLE);
                    b.buttonOk.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {
                b.pageIndicator.setActiveIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void hideActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    private void setStatusBackgroundColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_main));
        }
    }

    private void setUpViews() {
        IntroFragmentAdapter adapter = new IntroFragmentAdapter(
                getSupportFragmentManager());
        b.viewPager.setAdapter(adapter);
//        b.viewPager.addOnPageChangeListener(b.pagerIndicator);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setStatusBackgroundColor();

        CaddisflyApp.getApp().setAppLanguage(null, false, refreshHandler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            refreshHandler.sendEmptyMessage(0);
        }
    }

    public void onSettingsClick(@SuppressWarnings("unused") MenuItem item) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.refreshKey, false);
            this.recreate();
        }
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

    public void onNextClicked(View view) {
        b.viewPager.setCurrentItem(1, true);
    }

    public void onOkClicked(View view) {
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

    public void onAboutClick(View view) {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
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
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }

    public class IntroFragmentAdapter extends FragmentStatePagerAdapter {

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
    }
}

