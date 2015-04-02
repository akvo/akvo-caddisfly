package org.akvo.caddisfly.util;

import junit.framework.TestCase;

public class ClassUtilsTest extends TestCase {

    public void testAssertUtilityClassWellDefined() throws Exception {
        ClassUtils.assertUtilityClassWellDefined(ClassUtils.class);
    }
}