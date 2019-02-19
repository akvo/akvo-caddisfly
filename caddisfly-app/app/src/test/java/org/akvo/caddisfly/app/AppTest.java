package org.akvo.caddisfly.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class AppTest {

    private static final String[] EXPECTED_PERMISSIONS = {
            "android.permission.DISABLE_KEYGUARD",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WAKE_LOCK",
            "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
            "android.permission.CAMERA",
            "android.permission.BLUETOOTH",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "com.google.android.c2dm.permission.RECEIVE"
    };

    private static final String MERGED_MANIFEST =
            "build/intermediates/merged_manifests/mainExtDebug/AndroidManifest.xml";

    @Test
    public void shouldMatchPermissions() {
        AndroidManifest manifest = new AndroidManifest(
                Fs.fromUrl(MERGED_MANIFEST),
                null,
                null
        );

        assertThat(new HashSet<>(manifest.getUsedPermissions())).
                containsOnly(EXPECTED_PERMISSIONS);
    }
}

