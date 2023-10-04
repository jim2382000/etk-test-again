package net.micropact.aea.du.utility.mismatchedColumnTypes.postgres;

import java.util.Objects;

import net.micropact.aea.du.utility.mismatchedColumnTypes.IDatabaseColumnMetadata;

/**
 * Class representing a single record in Postgres' information_schema.columns table.
 *
 */
public class PostgresColumnMetadata implements IDatabaseColumnMetadata {

    private final String tableName;
    private final String columnName;
    private final String dataType;
    private final Long characterMaximumLength;
    private final Long numericPrecision;
    private final Long numericScale;
    private final Long datetimePrecision;

    /**
     * Simple constructor.
     *
     * @param theTableName
     *            TABLE_NAME
     * @param theColumnName
     *            COLUMN_NAME
     * @param theDataType
     *            DATA_TYPE
     * @param theCharacterMaximumLength
     *            CHARACTER_MAXIMUM_LENGTH
     * @param theNumericPrecision
     *            NUMERIC_PRECISION
     * @param theNumericScale
     *            NUMERIC_SCALE
     * @param theDatetimePrecision
     *            DATETIME_PRECISION
     */
    public PostgresColumnMetadata(final String theTableName, final String theColumnName, final String theDataType,
        final Long theCharacterMaximumLength, final Long theNumericPrecision, final Long theNumericScale,
        final Long theDatetimePrecision) {
        tableName = theTableName;
        columnName = theColumnName;
        dataType = theDataType;
        characterMaximumLength = theCharacterMaximumLength;
        numericPrecision = theNumericPrecision;
        numericScale = theNumericScale;
        datetimePrecision = theDatetimePrecision;
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
     * Get the character maximum length.
     *
     * @return the character maximum length
     */
    public Long getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    /**
     * Get the numeric precision.
     *
     * @return the numeric precision
     */
    public Long getNumericPrecision() {
        return numericPrecision;
    }

    /**
     * Get the numeric scale.
     *
     * @return the numeric scale
     */
    public Long getNumericScale() {
        return numericScale;
    }

    /**
     * Get the date time precision.
     *
     * @return the date time precision
     */
    public Long getDatetimePrecision() {
        return datetimePrecision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getTableName(),
            getColumnName(),
            getDataType(),
            getCharacterMaximumLength(),
            getNumericPrecision(),
            getNumericScale(),
            getDatetimePrecision());
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        } else {
            final PostgresColumnMetadata otherImpl = (PostgresColumnMetadata) other;
            return Objects.equals(getTableName(), otherImpl.getTableName())
                && Objects.equals(getColumnName(), otherImpl.getColumnName())
                && Objects.equals(getDataType(), otherImpl.getDataType())
                && Objects.equals(getCharacterMaximumLength(), otherImpl.getCharacterMaximumLength())
                && Objects.equals(getNumericPrecision(), otherImpl.getNumericPrecision())
                && Objects.equals(getNumericScale(), otherImpl.getNumericScale())
                && Objects.equals(getDatetimePrecision(), otherImpl.getDatetimePrecision());
        }
    }

    @Override
    public String toString() {
        return String.format(
            "TableName: %s, ColumnName: %s, DataType: %s, CharacterMaximumLength: %s, NumericPrecision: %s, NumericScale: %s, DatetimePrecision: %s",
            getTableName(),
            getColumnName(),
            getDataType(),
            getCharacterMaximumLength(),
            getNumericPrecision(),
            getNumericScale(),
            getDatetimePrecision());
    }
}
