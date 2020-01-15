package org.akvo

import org.akvo.caddisfly.internal.StriptestInternal
import org.akvo.caddisfly.test.StriptestTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(StriptestTest::class, StriptestInternal::class)
class StripTestSuite
