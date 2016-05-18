package de.qaware.chronix.solr.query.analysis.functions.transformation;

import de.qaware.chronix.solr.query.analysis.functions.ChronixTransformation;
import de.qaware.chronix.solr.query.analysis.functions.FunctionType;
import de.qaware.chronix.timeseries.MetricTimeSeries;

import java.time.temporal.ChronoUnit;

/**
 * The moving average transformation
 *
 * @author f.lautenschlager
 */
public class MovingAverage implements ChronixTransformation<MetricTimeSeries> {

    private final long timeSpan;
    private final ChronoUnit unit;

    public MovingAverage(long timeSpan, ChronoUnit unit) {
        this.timeSpan = timeSpan;
        this.unit = unit;
    }

    @Override
    public MetricTimeSeries transform(MetricTimeSeries timeSeries) {
        return null;


    }

    @Override
    public FunctionType getType() {
        return FunctionType.MOVAVG;
    }

    @Override
    public String[] getArguments() {
        return new String[]{"time span=" + timeSpan, "unit=" + unit.name()};
    }
}
