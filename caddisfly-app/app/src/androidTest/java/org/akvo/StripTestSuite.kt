package org.akvo

import org.akvo.caddisfly.internal.StriptestInternal
import org.akvo.caddisfly.test.StriptestTest
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Suite

// Runs all unit tests.
@RunWith(Enclosed::class)
@Suite.SuiteClasses(StriptestTest::class, StriptestInternal::class)
class StripTestSuite
