package org.akvo.caddisfly.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.manifest.AndroidManifest
import org.robolectric.res.Fs
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppTest {

    @Test
    fun shouldMatchPermissions() {
        val manifest = AndroidManifest(
                Fs.fromUrl(MERGED_MANIFEST),
                null, null
        )

        assertThat(HashSet(manifest.usedPermissions)).containsOnly(*EXPECTED_PERMISSIONS)
    }

    companion object {

        private val EXPECTED_PERMISSIONS = arrayOf("android.permission.DISABLE_KEYGUARD", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH_ADMIN", "android.permission.WAKE_LOCK", "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE", "android.permission.CAMERA", "android.permission.BLUETOOTH", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "com.google.android.c2dm.permission.RECEIVE")

        private val MERGED_MANIFEST = "build/intermediates/merged_manifests/mainExtDebug/AndroidManifest.xml"
    }
}

