package net.micropact.aea.du.utility.mismatchedColumnTypes;

/**
 * Interface representing information which MUST exist for a database column metadata.
 * This interface is implemented by platform-specific classes which add the platform-specific fields.
 *
 * @author Zachary.Miller
 */
public interface IDatabaseColumnMetadata {

    /**
     * Get the table name.
     *
     * @return the table name
     */
    String getTableName();

    /**
     * Get the column name.
     *
     * @return the column name
     */
    String getColumnName();
}
