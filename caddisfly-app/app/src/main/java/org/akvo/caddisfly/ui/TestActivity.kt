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

import android.Manifest.permission
import android.R.id
import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.*
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.analytics.FirebaseAnalytics.Param
import org.akvo.caddisfly.BuildConfig
import org.akvo.caddisfly.R
import org.akvo.caddisfly.R.*
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.AppConfig
import org.akvo.caddisfly.common.ConstantKey
import org.akvo.caddisfly.common.Constants
import org.akvo.caddisfly.common.SensorConstants
import org.akvo.caddisfly.helper.ApkHelper
import org.akvo.caddisfly.helper.CameraHelper
import org.akvo.caddisfly.helper.ErrorMessages
import org.akvo.caddisfly.helper.FileHelper.cleanResultImagesFolder
import org.akvo.caddisfly.helper.PermissionsDelegate
import org.akvo.caddisfly.model.TestInfo
import org.akvo.caddisfly.model.TestType
import org.akvo.caddisfly.model.TestType.CBT
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.sensor.bluetooth.DeviceControlActivity
import org.akvo.caddisfly.sensor.bluetooth.DeviceScanActivity
import org.akvo.caddisfly.sensor.cbt.CbtActivity
import org.akvo.caddisfly.sensor.manual.ManualTestActivity
import org.akvo.caddisfly.sensor.manual.SwatchSelectTestActivity
import org.akvo.caddisfly.sensor.striptest.ui.StripTestActivity
import org.akvo.caddisfly.sensor.usb.SensorActivity
import org.akvo.caddisfly.util.AlertUtil
import org.akvo.caddisfly.util.ApiUtil
import org.akvo.caddisfly.util.PreferencesUtil
import org.akvo.caddisfly.viewmodel.TestListViewModel
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class TestActivity : AppUpdateActivity() {
    companion object {
        private const val REQUEST_TEST = 1
        private const val MESSAGE_TWO_LINE_FORMAT = "%s%n%n%s"
        private const val SNACK_BAR_LINE_SPACING = 1.4f

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val handler = WeakRefHandler(this)
    private val permissionsDelegate = PermissionsDelegate(this)
    private val permissions = arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    private val bluetoothPermissions = arrayOf(permission.ACCESS_COARSE_LOCATION)
    private var testInfo: TestInfo? = null
    private var cameraIsOk = false
    private var mainLayout: LinearLayout? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    // Tests like CBT has two test phases
    private var testPhase = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layout.activity_test)

        val intent: Intent = intent

        // Stop if the app version has expired
        if (ApkHelper.isAppVersionExpired(this)) {
            return
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val fragmentManager: FragmentManager = supportFragmentManager
        mainLayout = findViewById<LinearLayout?>(R.id.mainLayout)
        if (savedInstanceState != null) {
            testInfo = savedInstanceState.getParcelable(ConstantKey.TEST_INFO)
            testPhase = savedInstanceState.getInt(ConstantKey.TEST_PHASE)
        }
        if (testInfo == null) {
            testInfo = intent.getParcelableExtra(ConstantKey.TEST_INFO)
        }
        setTitle(string.appName)
        if (testInfo == null) {
            val type = intent.type
            if ("text/plain" == type
                    && AppConfig.EXTERNAL_APP_ACTION == intent.action) {
                getTestSelectedByExternalApp(fragmentManager, intent)
            }
        }
        if (testInfo == null) {
            return
        }

        // Add list fragment if this is first creation
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TestInfoFragment.getInstance(testInfo),
                            TestActivity::class.java.simpleName).commit()
        }
        if (testInfo != null && testInfo!!.subtype == TestType.SENSOR &&
                !this.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            ErrorMessages.alertFeatureNotSupported(this, true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ConstantKey.TEST_INFO, testInfo)
        outState.putInt(ConstantKey.TEST_PHASE, testPhase)
        super.onSaveInstanceState(outState)
    }

    private fun getTestSelectedByExternalApp(fragmentManager: FragmentManager, intent: Intent) {
        CaddisflyApp.getApp().setAppLanguage(this,
                intent.getStringExtra(SensorConstants.LANGUAGE), true, handler)
        if (AppPreferences.getShowDebugInfo()) {
            Toast.makeText(this, "Language: " + intent.getStringExtra(SensorConstants.LANGUAGE),
                    Toast.LENGTH_LONG).show()
        }
        val questionTitle: String = intent.getStringExtra(SensorConstants.QUESTION_TITLE)!!
        val uuid: String? = intent.getStringExtra(SensorConstants.RESOURCE_ID)
        if (uuid != null) {
            //Get the test config by uuid
            val viewModel = ViewModelProviders.of(this).get(TestListViewModel::class.java)
            testInfo = viewModel.getTestInfo(uuid)
        }
        if (testInfo == null) {
            title = getTestName(questionTitle)
            alertTestTypeNotSupported()
        } else {
            val fragment: TestInfoFragment? = TestInfoFragment.getInstance(testInfo)
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment!!, TestActivity::class.java.simpleName).commit()
        }
    }

    override fun onStart() {
        super.onStart()
        if (testInfo != null) {
            title = if (testInfo!!.subtype == TestType.BLUETOOTH) {
                String.format("%s. %s", testInfo!!.md610Id, testInfo!!.name)
            } else {
                testInfo!!.name
            }
        }
    }

    /**
     * Start the test.
     *
     * @param view the View
     */
    fun onStartTestClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        testPhase = 1
        var checkPermissions = permissions
        when (testInfo!!.subtype) {
            TestType.SENSOR -> {
                startTest()
                return
            }
            TestType.MANUAL, TestType.MANUAL_COLOR_SELECT -> if (!testInfo!!.hasImage) {
                startTest()
                return
            }
            TestType.BLUETOOTH -> checkPermissions = bluetoothPermissions
            else -> {
            }
        }
        if (permissionsDelegate.hasPermissions(checkPermissions)) {
            startTest()
        } else {
            permissionsDelegate.requestPermissions(checkPermissions)
        }
    }

    private fun startTest() {
        when (testInfo!!.subtype) {
            TestType.BLUETOOTH -> startBluetoothTest()
            CBT -> startCbtTest()
            TestType.MANUAL -> startManualTest()
            TestType.MANUAL_COLOR_SELECT -> startSwatchSelectTest()
            TestType.SENSOR -> startSensorTest()
            TestType.STRIP_TEST -> if (cameraIsOk) {
                startStripTest()
            } else {
                checkCameraMegaPixel()
            }
            else -> {
            }
        }
    }

    private fun startSwatchSelectTest() {
        val intent = Intent(this, SwatchSelectTestActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    private fun startBluetoothTest() {
        val intent: Intent = if (AppPreferences.isTestMode() || AppConfig.SKIP_BLUETOOTH_SCAN) {
            Intent(this, DeviceControlActivity::class.java)
        } else {
            Intent(this, DeviceScanActivity::class.java)
        }
        // skip scanning for device in testing mode


        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    private fun startCbtTest() {
        cleanResultImagesFolder()
        val intent = Intent(this, CbtActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        intent.putExtra(ConstantKey.TEST_PHASE, testPhase)
        startActivityForResult(intent, REQUEST_TEST)
    }

    private fun startManualTest() {
        cleanResultImagesFolder()
        val intent = Intent(this, ManualTestActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    private fun startSensorTest() {
        //Only start the sensor activity if the device supports 'On The Go'(OTG) feature

        val hasOtg = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
        if (hasOtg) {
            val sensorIntent = Intent(this, SensorActivity::class.java)
            sensorIntent.putExtra(ConstantKey.TEST_INFO, testInfo)
            startActivityForResult(sensorIntent, REQUEST_TEST)
        } else {
            ErrorMessages.alertFeatureNotSupported(this, true)
        }
    }

    private fun startStripTest() {
        val intent = Intent(this, StripTestActivity::class.java)
        intent.putExtra(ConstantKey.TEST_INFO, testInfo)
        startActivityForResult(intent, REQUEST_TEST)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TEST && resultCode == Activity.RESULT_OK) {
            //return the test result to the external app

            val intent = Intent(data)
            @Suppress("ConstantConditionIf")
            if (!BuildConfig.DEBUG && !AppConfig.STOP_ANALYTICS) {
                val bundle = Bundle()
                bundle.putString(Param.ITEM_ID, testInfo!!.uuid)
                bundle.putString(Param.ITEM_NAME, testInfo!!.name)
                bundle.putString("Brand", testInfo!!.brand)
                bundle.putString("Type", testInfo!!.subtype.toString().toLowerCase(Locale.US))
                bundle.putString("Range", testInfo!!.ranges)
                val instanceName: String? = getIntent().getStringExtra(SensorConstants.FLOW_INSTANCE_NAME)
                if (instanceName != null && instanceName.isNotEmpty()) {
                    bundle.putString("Instance", instanceName)
                    bundle.putString("InstanceTest", instanceName + "," + testInfo!!.name + "," + testInfo!!.uuid)
                }
                bundle.putString(Param.CONTENT_TYPE, "test")
                mFirebaseAnalytics!!.logEvent(Event.SELECT_CONTENT, bundle)
            }
            this.setResult(Activity.RESULT_OK, intent)
            finish()
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            finish()
        }
    }

    /**
     * Show Instructions for the test.
     *
     * @param view the View
     */
    fun onInstructionsClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        testPhase = 2
        if (testInfo!!.subtype == CBT) {
            val checkPermissions = permissions
            if (permissionsDelegate.hasPermissions(checkPermissions)) {
                startTest()
            } else {
                permissionsDelegate.requestPermissions(checkPermissions)
            }
        }
    }

    /**
     * Navigate to clicked link.
     *
     * @param view the View
     */
    fun onSiteLinkClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        var url: String? = testInfo!!.brandUrl
        if (url != null) {
            if (!url.contains("http://")) {
                url = "http://$url"
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }

    private fun getTestName(title: String): String {
        var tempTitle = title

        //ensure we have short name to display as title
        if (title.isNotEmpty()) {
            if (title.length > 30) {
                tempTitle = title.substring(0, 30)
            }
        } else {
            tempTitle = getString(string.error)
        }
        return tempTitle
    }

    private fun checkCameraMegaPixel() {
        cameraIsOk = true
        if (PreferencesUtil.getBoolean(this, string.showMinMegaPixelDialogKey, true)) {
            try {
                if (CameraHelper.getMaxSupportedMegaPixelsByCamera(this) < Constants.MIN_CAMERA_MEGA_PIXELS) {
                    window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
                    val checkBoxView: View = View.inflate(this, layout.dialog_message, null)
                    val checkBox: CheckBox = checkBoxView.findViewById(R.id.checkbox)
                    checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                        PreferencesUtil.setBoolean(baseContext,
                                string.showMinMegaPixelDialogKey, !isChecked)
                    }
                    val builder = Builder(this)
                    builder.setTitle(string.warning)
                    builder.setMessage(string.camera_not_good)
                            .setView(checkBoxView)
                            .setCancelable(false)
                            .setPositiveButton(string.continue_anyway) { _: DialogInterface?, _: Int -> startTest() }
                            .setNegativeButton(string.stop_test) { dialog: DialogInterface, _: Int ->
                                dialog.dismiss()
                                cameraIsOk = false
                                finish()
                            }.show()
                } else {
                    startTest()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        } else {
            startTest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest()
        } else {
            val message: String = if (testInfo!!.subtype == TestType.BLUETOOTH) {
                getString(string.location_permission)
            } else {
                getString(string.cameraAndStoragePermissions)
            }
            val snackbar = Snackbar
                    .make(mainLayout!!, message,
                            Snackbar.LENGTH_LONG)
                    .setAction("SETTINGS") { ApiUtil.startInstalledAppDetailsActivity(this) }
            val typedValue = TypedValue()
            theme.resolveAttribute(attr.colorPrimary, typedValue, true)
            snackbar.setActionTextColor(typedValue.data)
            val snackView = snackbar.view
            val textView: TextView = snackView.findViewById(R.id.snackbar_text)
            textView.height = resources.getDimensionPixelSize(dimen.snackBarHeight)
            textView.setLineSpacing(0f, SNACK_BAR_LINE_SPACING)
            textView.setTextColor(Color.WHITE)
            snackbar.show()
        }
    }

    /**
     * Alert displayed when an unsupported contaminant test type was requested.
     */
    private fun alertTestTypeNotSupported() {
        var message = getString(string.errorTestNotAvailable)
        message = String.format(MESSAGE_TWO_LINE_FORMAT, message, getString(string.pleaseContactSupport))
        AlertUtil.showAlert(this, string.cannotStartTest, message,
                string.ok,
                OnClickListener { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    finish()
                }, null,
                OnCancelListener { dialogInterface: DialogInterface ->
                    dialogInterface.dismiss()
                    finish()
                }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
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
}