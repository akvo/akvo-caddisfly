package org.akvo;

import org.akvo.caddisfly.survey.SurveySensorTest;
import org.akvo.caddisfly.ui.CalibrationTest;
import org.akvo.caddisfly.ui.DiagnosticTest;
import org.akvo.caddisfly.ui.LanguageTest;
import org.akvo.caddisfly.ui.MiscTest;
import org.akvo.caddisfly.ui.NavigationTest;
import org.akvo.caddisfly.ui.SensorsTest;
import org.akvo.caddisfly.ui.StripTestNavigation;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({CalibrationTest.class,
        DiagnosticTest.class,
        LanguageTest.class,
        MiscTest.class,
        NavigationTest.class,
        SensorsTest.class,
        StripTestNavigation.class,
        SurveySensorTest.class})
public class UserInterfaceSuite {
}
