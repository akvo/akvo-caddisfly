/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import junit.framework.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
final class ClassUtil {

    private ClassUtil() {
    }

    /**
     * Verifies that a utility class is well defined.
     * https://github.com/trajano/maven-jee6/tree/master/maven-jee6-test
     *
     * @param clazz utility class to verify.
     * @throws NoSuchMethodException if method does not exist
     * @throws InvocationTargetException a checked exception
     * @throws InstantiationException if unable to instantiate
     * @throws IllegalAccessException if unable to access
     */
    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Assert.assertTrue("class must be final",
                Modifier.isFinal(clazz.getModifiers()));
        Assert.assertEquals("There must be only one constructor", 1,
                clazz.getDeclaredConstructors().length);
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible() ||
                !Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                Assert.fail("there exists a non-static method:" + method);
            }
        }
    }

}
