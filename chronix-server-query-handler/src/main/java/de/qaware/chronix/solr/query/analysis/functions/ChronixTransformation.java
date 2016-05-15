package de.qaware.chronix.solr.query.analysis.functions;

/**
 * The transformation interface
 *
 * @param <T> defines the type of the time series
 * @author f.lautenschlager
 */
public interface ChronixTransformation<T> {

    /**
     * Transforms a time series by changing it inital values
     *
     * @param timeSeries the time series that is transformed
     * @return the transformed time series
     */
    T transform(T timeSeries);

    /**
     * @return the type of the transformation
     */
    ChronixTransformationType getType();

}
