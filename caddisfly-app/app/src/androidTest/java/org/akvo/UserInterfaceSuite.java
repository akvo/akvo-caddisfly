package org.akvo;

import org.akvo.caddisfly.diagnostic.DiagnosticTest;
import org.akvo.caddisfly.test.CbtTest;
import org.akvo.caddisfly.ui.IntroTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({DiagnosticTest.class,
        CbtTest.class,
        IntroTest.class})
public class UserInterfaceSuite {
}
