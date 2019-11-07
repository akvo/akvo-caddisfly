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

package org.akvo.caddisfly.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import dagger.android.AndroidInjection
import org.akvo.caddisfly.R
import org.akvo.caddisfly.R.*
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.databinding.ActivityMainBinding
import org.akvo.caddisfly.helper.ApkHelper
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.preference.SettingsActivity
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.AnimatedColor
import org.akvo.caddisfly.util.PreferencesUtil
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import javax.inject.Inject

const val FIRST_PAGE = 0
const val INTRO_PAGE_COUNT = 2
private const val IMMEDIATE_UPDATE_REQUEST_CODE = 1867
private const val FLEXIBLE_UPDATE_REQUEST_CODE = 4244

class MainActivity : BaseActivity() {

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    @Inject
    lateinit var playServiceExecutor: Executor

    private val refreshHandler = WeakRefHandler(this)
    private var b: ActivityMainBinding? = null
    private var statusBarColors: AnimatedColor? = null

    private val listener = { state: InstallState ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        } else if (state.installStatus() == InstallStatus.FAILED) {
            popupSnackbarForRetryUpdate()
        }
        // Show module progress, log state, or install the update.
    }

    private fun checkInAppUpdate() {
        appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener(playServiceExecutor, OnSuccessListener { appUpdateInfo ->
                    when (appUpdateInfo.updateAvailability()) {
                        UpdateAvailability.UPDATE_AVAILABLE -> when {
                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> startFlexibleUpdate(
                                    appUpdateInfo
                            )
                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> startImmediateUpdate(
                                    appUpdateInfo
                            )
                            else -> {
                                // No update is allowed
                            }
                        }
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> startImmediateUpdate(
                                appUpdateInfo
                        )
                        else -> {
                            // No op
                        }
                    }
                })
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                IMMEDIATE_UPDATE_REQUEST_CODE
        )
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                this,
                FLEXIBLE_UPDATE_REQUEST_CODE
        )
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
                findViewById(id.mainLayout),
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            val textView: TextView = view.findViewById(id.snackbar_text)
//            textView.height = resources.getDimensionPixelSize(dimen.snackBarHeight)
            textView.setTextColor(Color.WHITE)
            show()
        }
    }

    private fun popupSnackbarForRetryUpdate() {
        Snackbar.make(
                findViewById(id.mainLayout),
                "Unable to download update.",
                Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RETRY") { checkInAppUpdate() }
            val textView: TextView = view.findViewById(id.snackbar_text)
//            textView.height = resources.getDimensionPixelSize(dimen.snackBarHeight)
            textView.setTextColor(Color.WHITE)
            show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        b = DataBindingUtil.setContentView<ActivityMainBinding?>(this, layout.activity_main)

        appUpdateManager.registerListener(listener)

        b!!.pageIndicator.setPageCount(INTRO_PAGE_COUNT)

        setTitle(string.appName)

        try {
            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        hideActionBar()

        b!!.viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                b!!.pageIndicator.setActiveIndex(position)
                if (position == 1) {
                    b!!.buttonNext.visibility = View.GONE
                    b!!.buttonOk.visibility = View.VISIBLE
                } else {
                    b!!.buttonNext.visibility = View.VISIBLE
                    b!!.buttonOk.visibility = View.GONE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        b!!.buttonInfo.setOnClickListener {
            val intent = Intent(baseContext, AboutActivity::class.java)
            startActivity(intent)
        }

        Handler().post { setUpViews() }

        checkInAppUpdate()
    }

    private fun hideActionBar() {
        val supportActionBar = supportActionBar
        supportActionBar?.hide()
    }

    private fun setUpViews() {
        b!!.viewPager.adapter = IntroFragmentAdapter(supportFragmentManager)
        b!!.pageIndicator.setActiveIndex(0)
        if (b!!.viewPager.currentItem == 1) {
            b!!.buttonNext.visibility = View.GONE
            b!!.buttonOk.visibility = View.VISIBLE
        } else {
            b!!.buttonNext.visibility = View.VISIBLE
            b!!.buttonOk.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            statusBarColors = if (AppPreferences.isDiagnosticMode()) {
                AnimatedColor(
                        ContextCompat.getColor(this, color.colorPrimaryDark),
                        ContextCompat.getColor(this, color.diagnostic_status))
            } else {
                AnimatedColor(
                        ContextCompat.getColor(this, color.colorPrimaryDark),
                        ContextCompat.getColor(this, color.black_main))
            }
            animateStatusBar()
        }

        CaddisflyApp.getApp().setAppLanguage(this, null, false, refreshHandler)
        if (PreferencesUtil.getBoolean(this, string.refreshKey, false)) {
            PreferencesUtil.removeKey(this, string.refreshKey)
            refreshHandler.sendEmptyMessage(0)
            return
        }

        Handler().postDelayed({ setUpViews() }, 300)

        appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener(playServiceExecutor, OnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate()
                    }
                })
    }

    fun onSettingsClick(@Suppress("UNUSED_PARAMETER") item: MenuItem?) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (AppPreferences.isDiagnosticMode()) {
            menuInflater.inflate(R.menu.menu_main_diagnostic, menu)
        } else {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    fun onNextClicked(@Suppress("UNUSED_PARAMETER") view: View?) {
        b!!.viewPager.setCurrentItem(1, true)
    }

    fun onOkClicked(@Suppress("UNUSED_PARAMETER") view: View?) {
        val intent: Intent? = packageManager
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            closeApp(1000)
        } else {
            alertDependantAppNotFound()
        }
    }

    private fun alertDependantAppNotFound() {
        val message = String.format("%s\r\n\r\n%s", "Akvo Flow is not installed.",
                "Please install the Akvo Flow app from your instance.")
        AlertUtil.showAlert(this, string.notFound, message, string.close,
                DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> closeApp(0) },
                null, null)
    }

    private fun closeApp(delay: Int) {
        Handler().postDelayed({
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                val pid = Process.myPid()
                Process.killProcess(pid)
            }
        }, delay.toLong())
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    private fun animateStatusBar() {
        val animator: ValueAnimator = ObjectAnimator.ofFloat(0f, 1f).setDuration(1000)
        animator.addUpdateListener { animation: ValueAnimator ->
            val v = animation.animatedValue as Float
            window.statusBarColor = statusBarColors!!.with(v)
        }
        animator.start()
    }

    /**
     * Handler to restart the app after language has been changed.
     */
    private class WeakRefHandler internal constructor(ref: Activity) : Handler() {
        private val ref: WeakReference<Activity> = WeakReference(ref)
        override fun handleMessage(msg: Message) {
            val f = ref.get()
            f?.recreate()
        }

    }

    override fun onBackPressed() {
        if (b!!.viewPager.currentItem > 0) {
            b!!.viewPager.currentItem = b!!.viewPager.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(listener)
    }

    internal inner class IntroFragmentAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return if (position == FIRST_PAGE) {
                Intro1Fragment.newInstance()
            } else {
                Intro2Fragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return INTRO_PAGE_COUNT
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }
}
