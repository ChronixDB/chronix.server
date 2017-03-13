package de.qaware.chronix.solr.plugin;


import de.qaware.chronix.server.functions.ChronixFunction;
import de.qaware.chronix.server.functions.FunctionValueMap;
import de.qaware.chronix.timeseries.MetricTimeSeries;

/**
 * A simple function for testing purposes
 *
 * @author f.lautenschlager
 */
public class NothingFunction implements ChronixFunction<MetricTimeSeries> {


    @Override
    public void execute(MetricTimeSeries timeSeries, FunctionValueMap functionValueMap) {

    }

    public String getQueryName() {
        return "nothing";
    }

    @Override
    public String getTimeSeriesType() {
        return "metric";
    }

    public String getType() {
        return null;
    }
}
