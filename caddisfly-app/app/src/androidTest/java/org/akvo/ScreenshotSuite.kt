package org.akvo

import org.akvo.caddisfly.instruction.*
import org.akvo.caddisfly.internal.NavigationTest
import org.akvo.caddisfly.internal.StriptestInternal
import org.akvo.caddisfly.update.UpdateTest
import org.junit.runners.Suite

/**
 * Runs only tests that generate screenshots
 */

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(
        BluetoothInstructions::class, CbtInstructions::class,
        ManualInstructions::class, PhotometerInstructions::class,
        SensorInstructions::class, StriptestInternal::class, TesterInstructions::class,
        NavigationTest::class, UpdateTest::class)
class ScreenshotSuite
