/*
 * Copyright (C) 2016 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.solr.test.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Helper to inject a field into a class.
 * Mainly used in tests to inject mocks (e.g., LOGGER, ...)
 *
 * @author f.lautenschlager
 */
public class ReflectionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionHelper.class);

    private ReflectionHelper() {
        //avoid instances
    }

    /**
     * Makes the field of the given class modifiable
     *
     * @param field - the field as string
     * @param clazz - the class
     * @return the modified field
     * @throws Exception if any error occurs
     */
    public static Field makeFieldModifiable(String field, Class clazz) throws IllegalAccessException {
        try {
            Field fieldInstance = getField(field, clazz);
            fieldInstance.setAccessible(true);
            int modifiers = fieldInstance.getModifiers();

            Field modifierField = fieldInstance.getClass().getDeclaredField("modifiers");
            modifiers = modifiers & ~Modifier.FINAL;
            modifierField.setAccessible(true);
            modifierField.setInt(fieldInstance, modifiers);

            return fieldInstance;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Could not access field or set value to it", e);
            throw new IllegalAccessException("Could not access field or set value to it");
        }
    }

    private static Field getField(String field, Class clazz) throws NoSuchFieldException {
        Field fieldInstance;
        try {
            fieldInstance = clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            LOGGER.debug("Class does not declare field. Try superclass.", e);
            //try superclass
            fieldInstance = clazz.getSuperclass().getDeclaredField(field);
        }
        return fieldInstance;
    }

    /**
     * Sets the new value to the given field of the given class
     *
     * @param newValue - the new value
     * @param field    - the field name as string
     * @param obj      - the object
     * @throws Exception if any error occurs
     */
    public static void setValueToFieldOfObject(Object newValue, String field, Object obj) throws IllegalAccessException {
        Field modifiable = makeFieldModifiable(field, obj.getClass());
        modifiable.set(obj, newValue);
    }
}
