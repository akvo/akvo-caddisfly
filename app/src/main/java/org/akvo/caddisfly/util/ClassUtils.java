package org.akvo.caddisfly.util;

import junit.framework.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
final class ClassUtils {

    private ClassUtils() {
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
