package org.akvo

import org.akvo.caddisfly.instruction.CbtInstructionsTest
import org.akvo.caddisfly.test.CbtSurveyTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(CbtInstructionsTest::class, CbtSurveyTest::class)
class CbtTestSuite
