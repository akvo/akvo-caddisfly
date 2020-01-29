package org.akvo

import org.akvo.caddisfly.misc.MiscTest
import org.akvo.caddisfly.test.CbtSurveyTest
import org.akvo.caddisfly.test.TesterSwatchSurveyTest
import org.akvo.caddisfly.ui.IntroTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. Commented because gradlew runs the tests twice
// https://github.com/gradle/gradle/issues/2603
//@RunWith(Suite::class)
@Suite.SuiteClasses(MiscTest::class, CbtSurveyTest::class, TesterSwatchSurveyTest::class, IntroTest::class)
class UserInterfaceSuite
