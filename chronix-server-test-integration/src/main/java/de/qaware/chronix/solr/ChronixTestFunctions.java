/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr;

import de.qaware.chronix.converter.common.DoubleList;
import de.qaware.chronix.converter.common.LongList;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Class with test functions for the integration test.
 *
 * @author f.lautenschlager
 */
public final class ChronixTestFunctions {

    public static final Function<MetricTimeSeries, String> GROUP_BY = ts -> ts.attribute("host") + "-" +
            ts.attribute("source") + "-" +
            ts.attribute("group") + "-" +
            ts.getMetric();
    public static final BinaryOperator<MetricTimeSeries> REDUCE = (t1, t2) -> {
        MetricTimeSeries.Builder reduced = new MetricTimeSeries.Builder(t1.getMetric())
                .points(concat(t1.getTimestamps(), t2.getTimestamps()),
                        concat(t1.getValues(), t2.getValues()))
                .attributes(t1.attributes());
        return reduced.build();
    };

    private ChronixTestFunctions() {
        //avoid instances
    }

    private static DoubleList concat(DoubleList first, DoubleList second) {
        first.addAll(second);
        return first;
    }

    private static LongList concat(LongList first, LongList second) {
        first.addAll(second);
        return first;
    }
}
