/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.util

import org.assertj.core.api.Fail.fail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

//http://stackoverflow.com/questions/4520216/how-to-add-test-coverage-to-a-private-constructor
internal object ClassUtil {

    /**
     * Verifies that a utility class is well defined.
     * https://github.com/trajano/maven-jee6/tree/master/maven-jee6-test
     *
     * @param clazz utility class to verify.
     * @throws NoSuchMethodException     if method does not exist
     * @throws InvocationTargetException a checked exception
     * @throws InstantiationException    if unable to instantiate
     * @throws IllegalAccessException    if unable to access
     */
    @Throws(NoSuchMethodException::class, InvocationTargetException::class,
            InstantiationException::class, IllegalAccessException::class)
    fun assertUtilityClassWellDefined(clazz: Class<*>) {
        assertTrue("class must be final",
                Modifier.isFinal(clazz.modifiers))
        assertEquals("There must be only one constructor", 1,
                clazz.declaredConstructors.size)
        val constructor = clazz.getDeclaredConstructor()
        if (constructor.isAccessible || !Modifier.isPrivate(constructor.modifiers)) {
            fail("constructor is not private")
        }
        constructor.isAccessible = true
        constructor.newInstance()
        constructor.isAccessible = false
        for (method in clazz.methods) {
            if (!Modifier.isStatic(method.modifiers) && method.declaringClass == clazz) {
                fail("there exists a non-static method:$method")
            }
        }
    }

}
