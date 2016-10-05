/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.FunctionValueMap;
import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.Lsof;
import de.qaware.chronix.timeseries.LsofTimeSeries;

import java.util.*;

/**
 * Group transformation for the lsof time series (experimental)
 *
 * @author f.lautenschlager
 */
public final class Group implements ChronixTransformation<LsofTimeSeries> {


    private final Set<String> filterValues;
    private final String field;

    public Group(String field, String[] values) {
        this.field = field;
        this.filterValues = new HashSet<>(Arrays.asList(values));
    }

    @Override
    public void execute(LsofTimeSeries timeSeries, FunctionValueMap functionValueMap) {

        Map<String, List<Lsof>> values = timeSeries.groupBy(field);

        for (Map.Entry<String, List<Lsof>> entry : values.entrySet()) {

            //Wildcard
            if (filterValues.contains("*")) {
                timeSeries.getAttributesReference().put(entry.getKey(), entry.getValue().size());
            } else if (filterValues.contains(entry.getKey())) {
                timeSeries.getAttributesReference().put(entry.getKey(), entry.getValue().size());
            }
        }
        functionValueMap.add(this);

    }

    @Override
    public FunctionType getType() {
        return FunctionType.GROUP;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"field=" + field, "filters=" + filterValues};
    }
}
