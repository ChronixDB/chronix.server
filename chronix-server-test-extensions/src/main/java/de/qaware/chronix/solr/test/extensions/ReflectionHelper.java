/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
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
public final class ReflectionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionHelper.class);

    private ReflectionHelper() {
        //avoid instances
    }

    /**
     * Makes the field of the given class modifiable
     *
     * @param field the field as string
     * @param clazz the class
     * @return the modified field
     * @throws NoSuchFieldException   if the field could not be found
     * @throws IllegalAccessException if the field is not accessible
     */
    public static Field makeFieldModifiable(String field, Class clazz) throws NoSuchFieldException, IllegalAccessException {
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
            throw e;
        }
    }

    /**
     * Returns the field object for the given class.
     * Also checks the superclass.
     *
     * @param field the field name
     * @param clazz the class holding the field
     * @return the field instance
     * @throws NoSuchFieldException if the class or the superclass do not contain the field
     */
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
     * @param newValue the new value that is placed to the fields value
     * @param field    the field name as string of the object
     * @param obj      the object holding the field
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field is not accessible
     */
    public static void setValueToFieldOfObject(Object newValue, String field, Object obj) throws NoSuchFieldException, IllegalAccessException {
        Field modifiable = makeFieldModifiable(field, obj.getClass());
        modifiable.set(obj, newValue);
    }
}
