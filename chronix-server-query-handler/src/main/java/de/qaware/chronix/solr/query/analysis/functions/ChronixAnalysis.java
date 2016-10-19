/*
 * GNU GENERAL PUBLIC LICENSE
 *                        Version 2, June 1991
 *
 *  Copyright (C) 1989, 1991 Free Software Foundation, Inc., <http://fsf.org/>
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 */
package de.qaware.chronix.solr.query.analysis.functions;

/**
 * @param <T> the type to apply the analysis on
 * @author f.lautenschlager
 */
public interface ChronixAnalysis<T> extends ChronixFunction<T> {

    /**
     * @return if the analysis needs a sub query. Default is false
     */
    default boolean needSubquery() {
        return false;
    }

    /**
     * @return the sub query of the analysis. Default is null.
     */
    default String getSubquery() {
        return null;
    }
}
