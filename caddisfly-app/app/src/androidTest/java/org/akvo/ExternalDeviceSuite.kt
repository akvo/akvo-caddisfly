package org.akvo

import org.akvo.caddisfly.test.SensorTest
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite

// Runs all unit tests.
@RunWith(Enclosed::class)
@Suite.SuiteClasses(SensorTest::class)
class ExternalDeviceSuite
