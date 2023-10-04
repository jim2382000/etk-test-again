package net.micropact.aea.core.utility;

import java.util.concurrent.atomic.AtomicLong;

import com.entellitrak.ExecutionContext;

import net.micropact.aea.utility.Utility;

/**
 * This class contains SQL-related utilities.
 *
 * @author Zachary.Miller
 */
public final class SqlUtils {

    /**
     * All created procedures will have this String prepended to them in order to avoid any naming conflicts with other
     * database objects.
     */
    private static final String PROCEDURE_PREFIX = "AEA_TEMP_PROCEDURE_";

    /**
     * Each procedure which is executed will have a different auto-incremented number appended to the end. This variable
     * keeps track of the next number to be appended.
     */
    private static final AtomicLong PROCEDURE_EXECUTION_NUMBER = new AtomicLong();

    /**
     * Utility classes do not need public constructors.
     */
    private SqlUtils() {
    }

    /**
     * Executes a valid SQL procedure body. Creates a procedure with sql as the body, executes the procedure, and drops
     * the procedure.
     *
     * <strong> This method relies on a static variable for generating names and is therefore not safe to call from
     * multiple classloaders. It is intended to be used in extremely targeted scenarios such as RDO Import. This method
     * will also leave the procedure behind if the container is restarted while this method is executing. </strong>
     *
     * @param etk
     *            entellitrak execution context
     * @param sql
     *            a valid procedure body to execute
     */
    public static void executeSqlProdecureFromString(final ExecutionContext etk, final String sql) {
        final long procedureExecutionNumber = PROCEDURE_EXECUTION_NUMBER.getAndIncrement();
        final String procedureName = String.format("%s%s", PROCEDURE_PREFIX, procedureExecutionNumber);
        try {
            etk.getLogger().error(String.format("Creating procedure %s",
                procedureName));

            if (Utility.isPostgreSQL(etk)) {
                etk.createSQL(String.format("CREATE PROCEDURE %s()%nLANGUAGE PLPGSQL%nAS $$%n%s%n$$;",
                    procedureName,
                    sql))
                    .execute();
            } else {
                etk.createSQL(String.format("CREATE PROCEDURE %s AS %s",
                    procedureName,
                    sql))
                    .execute();
            }

            etk.createProcedureCall(procedureName)
                .execute();
        } finally {
        	etk.getLogger().error(String.format("Cleaning up (dropping) procedure %s",
                procedureName));

            if (Utility.isPostgreSQL(etk)) {
                etk.createSQL(String.format("DROP PROCEDURE %s()", procedureName))
                    .execute();
            } else {
                etk.createSQL(String.format("DROP PROCEDURE %s", procedureName))
                    .execute();
            }
        }
    }
}
