package org.akvo

import org.akvo.caddisfly.instruction.CbtInstructionsTest
import org.akvo.caddisfly.test.CbtSurveyTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. Commented because gradlew runs the tests twice
// https://github.com/gradle/gradle/issues/2603
//@RunWith(Suite::class)
@Suite.SuiteClasses(CbtInstructionsTest::class, CbtSurveyTest::class)
class CbtTestSuite
