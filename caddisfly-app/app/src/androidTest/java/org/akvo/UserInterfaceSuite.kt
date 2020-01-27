package org.akvo

import org.akvo.caddisfly.misc.MiscTest
import org.akvo.caddisfly.test.CbtSurveyTest
import org.akvo.caddisfly.test.TesterSwatchSurveyTest
import org.akvo.caddisfly.ui.IntroTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(MiscTest::class, CbtSurveyTest::class, TesterSwatchSurveyTest::class, IntroTest::class)
class UserInterfaceSuite
