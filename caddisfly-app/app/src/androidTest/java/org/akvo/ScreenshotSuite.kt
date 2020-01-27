package org.akvo

import org.akvo.caddisfly.instruction.CbtInstructionsTest
import org.akvo.caddisfly.instruction.ManualInstructionsTest
import org.akvo.caddisfly.instruction.SensorInstructionsTest
import org.akvo.caddisfly.instruction.TesterInstructionsTest
import org.akvo.caddisfly.internal.AppNavigationTest
import org.akvo.caddisfly.internal.MD610Test
import org.akvo.caddisfly.internal.StriptestTest
import org.akvo.caddisfly.update.UpdateTest
import org.junit.runners.Suite

/**
 * Runs only tests that generate screenshots
 */

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(
        CbtInstructionsTest::class, ManualInstructionsTest::class, MD610Test::class,
        SensorInstructionsTest::class, StriptestTest::class, TesterInstructionsTest::class,
        AppNavigationTest::class, UpdateTest::class)
class ScreenshotSuite
