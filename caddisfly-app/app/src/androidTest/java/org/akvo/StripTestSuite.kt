package org.akvo

import org.akvo.caddisfly.instruction.StriptestInstructionsTest
import org.akvo.caddisfly.internal.StriptestTest
import org.akvo.caddisfly.test.StriptestSurveyTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. Commented because gradlew runs the tests twice
// https://github.com/gradle/gradle/issues/2603
//@RunWith(Suite::class)
@Suite.SuiteClasses(StriptestInstructionsTest::class, StriptestTest::class, StriptestSurveyTest::class)
class StripTestSuite
