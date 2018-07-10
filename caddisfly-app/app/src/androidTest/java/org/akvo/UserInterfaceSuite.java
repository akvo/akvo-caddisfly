package org.akvo;

import org.akvo.caddisfly.diagnostic.DiagnosticTest;
import org.akvo.caddisfly.misc.LanguageTest;
import org.akvo.caddisfly.misc.MiscTest;
import org.akvo.caddisfly.navigation.NavigationTest;
import org.akvo.caddisfly.navigation.StripTestNavigation;
import org.akvo.caddisfly.ui.SensorsUiTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({DiagnosticTest.class,
        LanguageTest.class,
        MiscTest.class,
        NavigationTest.class,
        SensorsUiTest.class,
        StripTestNavigation.class})
public class UserInterfaceSuite {
}
