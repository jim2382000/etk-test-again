package net.entellitrak.aea.gl.api.etk.sql;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility for dealing specifically with entellitrak Oracle databases.
 *
 * @author Zachary.Miller
 */
public final class OracleUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private OracleUtil() {
    }

    /**
     * Get HIBERNATE_SEQUENCE.NEXTVAL.
     *
     * @param etk entellitrak execution context
     * @return the next value
     */
    public static long getHibernateSequenceNextVal(final ExecutionContext etk) {
        try {
            return etk.createSQL("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL")
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
