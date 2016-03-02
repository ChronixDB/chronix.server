/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.test.converter

import de.qaware.chronix.converter.BinaryTimeSeries
import spock.lang.Specification

/**
 * Simple test for the default document converter used in several tests
 * @author f.lautenschlager
 */
class DefaultTimeSeriesConverterTest extends Specification {
    def "test default document converter from and to"() {
        given:
        def binaryDocument = new BinaryTimeSeries.Builder()
                .id("4711-5896-7578")
                .build()
        def converter = new DefaultTimeSeriesConverter()

        when:
        def convertedDocument = converter.from(binaryDocument, 0, 0)
        def reconvertedDocument = converter.to(convertedDocument)

        then:
        reconvertedDocument.id == binaryDocument.id
    }

}
