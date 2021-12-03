package org.akvo.caddisfly.app

import android.os.Build
import org.akvo.caddisfly.BuildConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.manifest.AndroidManifest
import org.robolectric.res.Fs
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AppTest {

    @Test
    fun shouldMatchPermissions() {
        val manifest = AndroidManifest(
            Fs.fromUrl("build/intermediates/merged_manifests/" + BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE.capitalize() + "/AndroidManifest.xml"),
            null, null
        )

        assertThat(HashSet(manifest.usedPermissions)).containsOnly(*EXPECTED_PERMISSIONS)
    }

    companion object {

        private val EXPECTED_PERMISSIONS = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WAKE_LOCK",
            "android.permission.CAMERA",
            "android.permission.BLUETOOTH",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
            "com.google.android.c2dm.permission.RECEIVE"
        )
    }
}

