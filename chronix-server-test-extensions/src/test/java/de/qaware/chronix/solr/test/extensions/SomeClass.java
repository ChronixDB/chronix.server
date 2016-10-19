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
