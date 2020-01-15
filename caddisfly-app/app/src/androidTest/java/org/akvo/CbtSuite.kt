package org.akvo

import org.akvo.caddisfly.instruction.CbtInstructions
import org.akvo.caddisfly.test.CbtTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(CbtInstructions::class, CbtTest::class)
class CbtSuite
