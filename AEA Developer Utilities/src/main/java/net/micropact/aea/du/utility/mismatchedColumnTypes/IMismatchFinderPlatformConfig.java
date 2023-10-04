package net.micropact.aea.du.utility.mismatchedColumnTypes;

import java.util.stream.Stream;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;

/**
 * Interface representing the information needed for the mismatched column types utility to work across different
 * databases such as SQL Server and Oracle.
 *
 * @author Zachary.Miller
 */
public interface IMismatchFinderPlatformConfig {

    /**
     * Loads metadata for all of the columns actually in the database. For instance from information_schema.columns
     * in SQL Server or user_tab_cols in Oracle.
     *
     * @param etk entellitrak execution context
     * @return the database columns metadata
     */
    AllDatabaseColumnsMetadata loadAllTablesColumns(ExecutionContext etk);

    /**
     * For a given data element returns the expected acceptable columns expected to be in the database.
     * Each element in this stream represents a single column (password fields for instance expect 3 columns) and
     * each {@link ElementAcceptableColumns} contains the acceptable column values for each of those columns.
     *
     * @param dataElement the data element
     * @return the acceptable columns
     */
    Stream<ElementAcceptableColumns> getExpectedElementColumns(DataElement dataElement);
}
