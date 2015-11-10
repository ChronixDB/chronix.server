/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.converter;


/**
 * The minimal required schema of Chronix.
 *
 * @author f.lautenschlager
 */
public class Schema {

    /**
     * The Constant IDX_ID
     */
    public static final String ID = "id";
    /**
     * The Constant IDX_DATA.
     */
    public static final String DATA = "data";
    /**
     * The start as long milliseconds since 1970
     */
    public static final String START = "start";

    /**
     * The end as long milliseconds since 1970
     */
    public static final String END = "end";

    /**
     * Private constructor
     */
    private Schema() {

    }

    /**
     * Checks if a given field is user-defined or not.
     *
     * @param field - the field name
     * @return true if the field is not one of the four required fields, otherwise false.
     */
    public static boolean isUserDefined(String field) {
        return !(field.equals(ID) || field.equals(DATA) || field.equals(START) || field.equals(END));
    }

}
