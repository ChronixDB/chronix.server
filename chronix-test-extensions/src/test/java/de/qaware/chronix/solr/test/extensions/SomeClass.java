/*
 * Copyright (C) 2015 QAware GmbH
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

/**
 * A test class with a static final string field
 *
 * @author f.lautenschlager
 */
public class SomeClass extends SuperClass {

    private final String name;

    /**
     * Default Constructor for our test class
     */
    public SomeClass() {
        this.name = "Chronix";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the father
     */
    public String getFather() {
        return super.father;
    }
}
