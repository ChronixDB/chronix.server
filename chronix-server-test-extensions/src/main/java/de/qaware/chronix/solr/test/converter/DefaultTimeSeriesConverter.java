/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.test.converter;

import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.TimeSeriesConverter;

/**
 * The default time series converter, that does nothing.
 * Only for test purposes.
 *
 * @author f.lautenschlager
 */
public class DefaultTimeSeriesConverter implements TimeSeriesConverter<BinaryTimeSeries> {


    @Override
    public BinaryTimeSeries from(BinaryTimeSeries binaryStorageDocument, long queryStart, long queryEnd) {
        return binaryStorageDocument;
    }

    @Override
    public BinaryTimeSeries to(BinaryTimeSeries timeSeries) {
        return timeSeries;
    }
}

