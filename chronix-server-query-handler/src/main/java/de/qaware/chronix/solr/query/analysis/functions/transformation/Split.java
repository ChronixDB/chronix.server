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

import de.qaware.chronix.solr.query.analysis.functions.ChronixListTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.solr.query.analysis.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.StraceTimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Split operation for the strace time series (experimental)
 *
 * @author f.lautenschlager
 */
public final class Split implements ChronixListTransformation<StraceTimeSeries> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Split.class);

    /*@Override
    public List<StraceTimeSeries> exeute(StraceTimeSeries timeSeries, FunctionValueMap analysisAndValues) {
        return new ArrayList<>(timeSeries.split());
    }
    */

    @Override
    public FunctionType getType() {
        return FunctionType.SPLIT;
    }

    @Override
    public void execute(StraceTimeSeries timeSeries, FunctionValueMap functionValueMap) {
        LOGGER.info("Split is currently not implemented");
    }

}
