package net.micropact.aea.core.query;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Enum for dealing with entellitrak database sequences.
 *
 * @author Zachary.Miller
 */
public enum DatabaseSequence {

    /**
     * HIBERNATE_SEQUENCE.
     */
    HIBERNATE_SEQUENCE("HIBERNATE_SEQUENCE"),
    /**
     * OBJECT_ID.
     */
    OBJECT_ID("OBJECT_ID");

    private final String oracleSequenceName;

    /**
     * Simple constructor.
     *
     * @param theOracleSequenceName
     *            the name of the sequence in oracle
     */
    DatabaseSequence(final String theOracleSequenceName) {
        oracleSequenceName = theOracleSequenceName;
    }

    /**
     * Get the next value from the sequence.
     *
     * @param etk
     *            entellitrak execution context
     * @return the next value
     */
    public long getNextVal(final ExecutionContext etk) {
        try {
            return etk.createSQL(String.format("SELECT %s.nextval FROM DUAL",
                oracleSequenceName))
                .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

}
