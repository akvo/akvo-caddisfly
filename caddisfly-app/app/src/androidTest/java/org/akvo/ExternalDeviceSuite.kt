package org.akvo

import org.akvo.caddisfly.test.SensorTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(SensorTest::class)
class ExternalDeviceSuite
