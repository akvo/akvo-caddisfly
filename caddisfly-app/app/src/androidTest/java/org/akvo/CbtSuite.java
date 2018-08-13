package org.akvo;

import org.akvo.caddisfly.instruction.CbtInstructions;
import org.akvo.caddisfly.test.CbtTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({CbtInstructions.class, CbtTest.class})
public class CbtSuite {
}
