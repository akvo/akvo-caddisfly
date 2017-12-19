package org.akvo;

import org.akvo.caddisfly.test.ECTest;
import org.akvo.caddisfly.test.SensorTest;
import org.akvo.caddisfly.test.SurveySensorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({ECTest.class,
        SurveySensorTest.class,
        SensorTest.class})
public class ExternalDeviceSuite {
}
