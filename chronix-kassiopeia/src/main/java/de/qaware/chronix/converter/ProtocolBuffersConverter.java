/*
 *    Copyright (C) 2015 QAware GmbH
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
package de.qaware.chronix.converter;


import de.qaware.chronix.converter.dt.ProtocolBuffers;
import de.qaware.chronix.dts.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class to easily convert the protocol buffer into Pair<Long,Double>
 *
 * @author f.lautenschlager
 */
public final class ProtocolBuffersConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolBuffersConverter.class);

    /**
     * Private constructor
     */
    private ProtocolBuffersConverter() {
        //utility class
    }

    /**
     * Returns an iterator to the points from the given byte array encoded data string
     *
     * @param points          - the input stream of points
     * @param timeSeriesStart - the start of the time series
     * @param timeSeriesEnd   - the end of the time series
     * @return an iterator to the points
     */
    public static Iterator<Pair<Long, Double>> from(final InputStream points, long timeSeriesStart, long timeSeriesEnd) {
        return new PointIterator(points, timeSeriesStart, timeSeriesEnd, -1, -1);
    }


    /**
     * Returns an iterator to the points from the given byte array encoded data string
     *
     * @param points          - the input stream of points
     * @param from            - including points from
     * @param to              - including points to
     * @param timeSeriesStart - the start of the time series  @return an iterator to the points
     */
    public static Iterator<Pair<Long, Double>> from(final InputStream points, long timeSeriesStart, long timeSeriesEnd, long from, long to) {
        if (from == -1 || to == -1) {
            throw new IllegalArgumentException("FROM or TO have to be >= 0");
        }
        return new PointIterator(points, timeSeriesStart, timeSeriesEnd, from, to);
    }

    /**
     * Converts the given list of our point class to the protocol buffer format
     *
     * @param metricDataPoints - the list with points
     * @return a protocol buffer points object
     */
    public static ProtocolBuffers.NumericPoints to(Iterator<Pair<Long, Double>> metricDataPoints) {
        return ProtocolBuffers.NumericPoints.newBuilder().addAllM(convertToProtoPoints(metricDataPoints)).build();
    }

    /**
     * Converts the given list with points to a list with protocol buffer points.
     *
     * @param metricDataPoints - the list with points
     * @return a list with protocol buffer points
     */
    private static List<ProtocolBuffers.NumericPoint> convertToProtoPoints(Iterator<Pair<Long, Double>> metricDataPoints) {
        List<ProtocolBuffers.NumericPoint> protoPoints = new ArrayList<>();

        long previousDate = 0;
        long previousOffset = 0;

        while (metricDataPoints.hasNext()) {


            Pair<Long, Double> p = metricDataPoints.next();

            if (p == null) {
                LOGGER.debug("Skipping 'null' point.");
                continue;
            }

            //the first point marks -oo, we ignore it
            if (p.getFirst() == null || p.getSecond() == null) {
                LOGGER.debug("Skipping point {} due to null values for timestamp or value", p);
                continue;
            }

            long offset;
            if (previousDate == 0) {
                offset = 0;
            } else {
                offset = p.getFirst() - previousDate;
            }

            if (almostEquals(previousOffset, offset)) {
                ProtocolBuffers.NumericPoint protoPoint = ProtocolBuffers.NumericPoint.newBuilder()
                        .setV(p.getSecond())
                        .build();
                protoPoints.add(protoPoint);
            } else {
                ProtocolBuffers.NumericPoint protoPoint = ProtocolBuffers.NumericPoint.newBuilder()
                        .setT(offset)
                        .setV(p.getSecond())
                        .build();
                protoPoints.add(protoPoint);

            }

            //set current as former previous date
            previousOffset = offset;
            previousDate = p.getFirst();
        }

        return protoPoints;
    }

    private static boolean almostEquals(long previousOffset, long offset) {
        //TODO: Make the diff configurable
        double diff = Math.abs(offset - previousOffset);
        return (diff < 1);
    }


    /**
     * Iterator that returns Pair<Long,Double> from the decompressed input stream
     */
    private static class PointIterator implements Iterator<Pair<Long, Double>> {

        private int size;
        private int current = 0;
        private ProtocolBuffers.NumericPoints protocolBufferPoints;
        private long lastOffset = 0;
        private long lastDate;
        private long timeSeriesEnd;
        private long timeSeriesStart;

        private long from;
        private long to;

        /**
         * Constructs a point iterator
         *
         * @param pointStream     - the points
         * @param from            - including points from
         * @param to              - including points to
         * @param timeSeriesStart - start of the time series
         */
        public PointIterator(final InputStream pointStream, long timeSeriesStart, long timeSeriesEnd, long from, long to) {
            try {
                protocolBufferPoints = ProtocolBuffers.NumericPoints.parseFrom(pointStream);
                size = protocolBufferPoints.getMCount();

                this.timeSeriesStart = timeSeriesStart;
                this.timeSeriesEnd = timeSeriesEnd;
                this.from = from;
                this.to = to;

                this.lastDate = timeSeriesStart;

            } catch (Exception e) {
                throw new IllegalStateException("Could not decode data to protocol buffer points.", e);
            }
        }

        /**
         * return true if the iterator points to more points
         */
        @Override
        public boolean hasNext() {

            //check if the given time range is valid
            if (from != -1 & to != -1) {
                //if to is left of the time series, we have no points to return
                if (to < timeSeriesStart) {
                    return false;
                }
                //if from is greater  to, we have nothing to return
                if (from > to) {
                    return false;
                }
                //if from is right of the time series we have nothing to return
                if (from > timeSeriesEnd) {
                    return false;
                }

                //check if the last date is greater than to
                if (lastDate >= to) {
                    return false;
                }

            }

            //otherwise we check the current index position
            return size > current;
        }

        /**
         * @return the next point
         */
        @Override
        public Pair<Long, Double> next() {

            if (!hasNext()) {
                throw new NoSuchElementException("No more elements.");
            }

            Pair<Long, Double> currentPoint = nextPoint();

            while (lastDate < from) {
                currentPoint = nextPoint();
            }

            return currentPoint;
        }

        private Pair<Long, Double> nextPoint() {
            Pair<Long, Double> p = convertToPoint(protocolBufferPoints.getM(current), lastDate);
            lastDate = p.getFirst();
            current++;
            return p;
        }

        private Pair<Long, Double> convertToPoint(ProtocolBuffers.NumericPoint m, long timeSeriesStart) {
            long offset = m.getT();
            if (offset != 0) {
                lastOffset = offset;
            }
            return Pair.pairOf(timeSeriesStart + lastOffset, m.getV());
        }

        @Override
        public void remove() {
            //does not make sense
        }
    }
}
