package net.micropact.aea.du.utility.mismatchedColumnTypes;

import java.util.List;

import com.entellitrak.configuration.DataElement;

/**
 * Class representing acceptable metadata column configuration for a particular column of a data element.
 * In the case that a data element has multiple fields (such as password), this class only represents a single one of
 * those columns.
 *
 * @author Zachary.Miller
 */
public class ElementAcceptableColumns {

    private final DataElement dataElement;
    private final String tableName;
    private final String columnName;
    private final List<IDatabaseColumnMetadata> acceptableColumns;

    /**
     * Simple constructor.
     *
     * @param theDataElement the data element
     * @param theTableName the table name
     * @param theColumnName the column name
     * @param theAcceptableColumns the acceptable columns
     */
    public ElementAcceptableColumns(
            final DataElement theDataElement,
            final String theTableName,
            final String theColumnName,
            final List<IDatabaseColumnMetadata> theAcceptableColumns) {
        dataElement = theDataElement;
        tableName = theTableName;
        columnName = theColumnName;
        acceptableColumns = theAcceptableColumns;
    }

    /**
     * Get the data element.
     *
     * @return the data element
     */
    public DataElement getDataElement(){
        return dataElement;
    }

    /**
     * Get the table name.
     *
     * @return the table name
     */
    public String getTableName(){
        return tableName;
    }

    /**
     * Get the column name.
     *
     * @return the column name
     */
    public String getColumnName(){
        return columnName;
    }

    /**
     * Get the acceptable metadata column values for this particular data element/column.
     *
     * @return the acceptable columns
     */
    public List<IDatabaseColumnMetadata> getAcceptableColumns(){
        return acceptableColumns;
    }
}
