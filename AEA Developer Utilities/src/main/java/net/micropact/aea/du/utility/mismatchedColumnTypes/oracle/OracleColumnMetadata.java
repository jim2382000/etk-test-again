package net.micropact.aea.du.utility.mismatchedColumnTypes.oracle;

import java.util.Objects;

import net.micropact.aea.du.utility.mismatchedColumnTypes.IDatabaseColumnMetadata;

/**
 * Class representing column metadata from oracle's user_tab_cols table.
 *
 * @author Zachary.Miller
 */
public class OracleColumnMetadata implements IDatabaseColumnMetadata{

    private final String tableName;
    private final String columnName;
    private final String dataType;
    private final Long dataLength;
    private final Long dataPrecision;
    private final Long charColDeclLength;
    private final Long charLength;
    private final String charUsed;
    private final Long dataScale;

    /**
     * Simple Constructor.
     *
     * @param theTableName TABLE_NAME
     * @param theColumnName COLUMN_NAME
     * @param theDataType DATA_TYPE
     * @param theDataLength DATA_LENGTH
     * @param theDataPrecision DATA_PRECISION
     * @param theCharColDeclLength CHAR_COL_DECL_LENGTH
     * @param theCharLength CHAR_LENGTH
     * @param theCharUsed CHAR_USED
     * @param theDataScale DATA_SCALE
     */
    // Suppress warning about too many parameters. We want the class to be immutable.
    @SuppressWarnings("java:S107")
    public OracleColumnMetadata(final String theTableName,
            final String theColumnName,
            final String theDataType,
            final Long theDataLength,
            final Long theDataPrecision,
            final Long theCharColDeclLength,
            final Long theCharLength,
            final String theCharUsed,
            final Long theDataScale) {
        tableName = theTableName;
        columnName = theColumnName;
        dataType = theDataType;
        dataLength = theDataLength;
        dataPrecision = theDataPrecision;
        charColDeclLength = theCharColDeclLength;
        charLength = theCharLength;
        charUsed = theCharUsed;
        dataScale = theDataScale;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    /**
     * Get the data type.
     *
     * @return the data type
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Get the data length.
     *
     * @return the data length
     */
    public long getDataLength() {
        return dataLength;
    }

    /**
     * Get the data precision.
     *
     * @return the data precision
     */
    public Long getDataPrecision() {
        return dataPrecision;
    }

    /**
     * Get the char col decl length.
     *
     * @return the char col decl length
     */
    public Long getCharColDeclLength() {
        return charColDeclLength;
    }

    /**
     * Get the char length.
     *
     * @return the char length
     */
    public Long getCharLength() {
        return charLength;
    }

    /**
     * Get the char used.
     *
     * @return the char used
     */
    public String getCharUsed() {
        return charUsed;
    }

    /**
     * Get the data scale.
     *
     * @return the data scale
     */
    public Long getDataScale() {
        return dataScale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getTableName(),
                getColumnName(),
                getDataType(),
                getDataLength(),
                getDataPrecision(),
                getCharColDeclLength(),
                getCharLength(),
                getCharUsed(),
                getDataScale());
    }

    @Override
    public boolean equals(final Object other) {
        if(other == null || getClass() != other.getClass()){
            return false;
        }else{
            final OracleColumnMetadata otherImpl = (OracleColumnMetadata) other;
            return Objects.equals(getTableName(), otherImpl.getTableName())
                    && Objects.equals(getColumnName(), otherImpl.getColumnName())
                    && Objects.equals(getDataType(), otherImpl.getDataType())
                    && Objects.equals(getDataLength(), otherImpl.getDataLength())
                    && Objects.equals(getDataPrecision(), otherImpl.getDataPrecision())
                    && Objects.equals(getCharColDeclLength(), otherImpl.getCharColDeclLength())
                    && Objects.equals(getCharLength(), otherImpl.getCharLength())
                    && Objects.equals(getCharUsed(), otherImpl.getCharUsed())
                    && Objects.equals(getDataScale(), otherImpl.getDataScale());
        }
    }

    @Override
    public String toString() {
        return String.format("TableName: %s, ColumnName: %s, DataType: %s, DataLength: %s, DataPrecision: %s, CharColDeclLength: %s, CharLength: %s, CharUsed: %s, DataScale: %s",
                getTableName(),
                getColumnName(),
                getDataType(),
                getDataLength(),
                getDataPrecision(),
                getCharColDeclLength(),
                getCharLength(),
                getCharUsed(),
                getDataScale());
    }
}
