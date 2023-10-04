package net.micropact.aea.du.utility.mismatchedColumnTypes;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Class representing all database column metadata in the database (for instance all records in SQL Server
 * information_schema.columns).
 *
 * @author Zachary.Miller
 */
public class AllDatabaseColumnsMetadata {

    private final Collection<IDatabaseColumnMetadata> tableColumns;

    /**
     * Simple constructor.
     *
     * @param theTableColumns
     *            the table columns
     */
    public AllDatabaseColumnsMetadata(final Collection<IDatabaseColumnMetadata> theTableColumns) {
        tableColumns = theTableColumns;
    }

    /**
     * Get the metadata record for a specific table/column.
     *
     * @param tableName
     *            the table name
     * @param columnName
     *            the column name
     * @return the matching metadata
     */
    public Optional<IDatabaseColumnMetadata> getByTableColumn(final String tableName, final String columnName) {
        return tableColumns.stream()
            .filter(tableColumn -> Objects.equals(tableName, tableColumn.getTableName())
                && Objects.equals(columnName.toLowerCase(), tableColumn.getColumnName().toLowerCase()))
            .findAny();
    }
}
