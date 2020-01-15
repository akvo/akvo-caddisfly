package org.akvo

import org.akvo.caddisfly.misc.MiscTest
import org.akvo.caddisfly.test.CbtTest
import org.akvo.caddisfly.test.SwatchSelectTest
import org.akvo.caddisfly.ui.IntroTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(MiscTest::class, CbtTest::class, SwatchSelectTest::class, IntroTest::class)
class UserInterfaceSuite
