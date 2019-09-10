package org.akvo

import org.akvo.caddisfly.instruction.StriptestInstructions
import org.akvo.caddisfly.test.StriptestTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

// Runs all unit tests.
@RunWith(Suite::class)
@Suite.SuiteClasses(StriptestTest::class, StriptestInstructions::class)
class StripTestSuite
