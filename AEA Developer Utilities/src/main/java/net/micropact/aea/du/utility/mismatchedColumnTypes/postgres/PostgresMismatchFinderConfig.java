package net.micropact.aea.du.utility.mismatchedColumnTypes.postgres;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataType;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.du.utility.mismatchedColumnTypes.AllDatabaseColumnsMetadata;
import net.micropact.aea.du.utility.mismatchedColumnTypes.ElementAcceptableColumns;
import net.micropact.aea.du.utility.mismatchedColumnTypes.IDatabaseColumnMetadata;
import net.micropact.aea.du.utility.mismatchedColumnTypes.IMismatchFinderPlatformConfig;
import net.micropact.aea.du.utility.mismatchedColumnTypes.MismatchedColumnTypesUtil;

/**
 * Postgres-specific information for the column mismatch finding utility.
 *
 */
public class PostgresMismatchFinderConfig implements IMismatchFinderPlatformConfig {

    /*
     * These are defined at the top of the file to make checkstyle happy, however it may actually hurt readability
     */

    private static final Long CURRENCY_NUMERIC_BASE_PRECISION = 18L;
    private static final Long CURRENCY_NUMERIC_SCALE = 2L;
    private static final Long DATE_DATETIME_PRECISION = 0L;
    private static final Long TIMESTAMP_DATETIME_PRECISION = 6L;
    private static final Long FILE_NUMERIC_PRECISION = 64L;
    private static final Long NUMBER_NUMERIC_PRECISION = 32L;
    private static final Long LONG_NUMERIC_PRECISION = 64L;
    private static final Long PASSWORD_HASH_CHARACTER_MAXIMUM_LENGTH = 255L;
    private static final Long PASSWORD_USER_NUMERIC_PRECISION = 64L;
    private static final Long STATE_NUMERIC_PRECISION = 64L;
    private static final Long YES_NO_NUMERIC_PRECISION = 16L;

    @Override
    public Stream<ElementAcceptableColumns> getExpectedElementColumns(final DataElement dataElement) {
        final String tableName = EtkDataUtils.getActualTableName(dataElement);
        final String columnName = dataElement.getColumnName();
        final DataType dataType = dataElement.getDataType();

        final Collection<ElementAcceptableColumns> returnValue;

        switch (dataType) {
            case CURRENCY:
				final long numberOfDecimalPlaces = MismatchedColumnTypesUtil.findNumberOfDecimalPlaces(dataElement);

				final long expectedPrecision = CURRENCY_NUMERIC_BASE_PRECISION + numberOfDecimalPlaces;

                returnValue = Arrays.asList(
            		new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
            				/* Legacy Behavior was hardcoded to this */
                            new PostgresColumnMetadata(tableName, columnName, "numeric", null,
                                CURRENCY_NUMERIC_BASE_PRECISION, CURRENCY_NUMERIC_SCALE, null),
                            /* New Behavior */
                            new PostgresColumnMetadata(tableName, columnName, "numeric", null,
                                    expectedPrecision, numberOfDecimalPlaces, null))));
                break;
            case DATE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "date", null, null,
                            null,
                            DATE_DATETIME_PRECISION))));
                break;
            case FILE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "bigint", null, FILE_NUMERIC_PRECISION,
                            0L, null))));
                break;
            case LONG:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "bigint", null, LONG_NUMERIC_PRECISION,
                            0L, null))));
                break;
            case LONG_TEXT:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "text",
                            null, null, null, null))));
                break;
            case NONE:
                returnValue = Collections.emptyList();
                break;
            case NUMBER:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "integer", null, NUMBER_NUMERIC_PRECISION,
                            0L, null))));
                break;
            case PASSWORD:
                final String dateColumnName = String.format("%s_DTS", columnName);
                final String userIdColumnName = String.format("%s_UID", columnName);

                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "character varying",
                            PASSWORD_HASH_CHARACTER_MAXIMUM_LENGTH, null, null, null))),
                    new ElementAcceptableColumns(dataElement, tableName, dateColumnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, dateColumnName, "timestamp without time zone", null,
                            null, null,
                            TIMESTAMP_DATETIME_PRECISION))),
                    new ElementAcceptableColumns(dataElement, tableName, userIdColumnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, userIdColumnName, "bigint", null,
                            PASSWORD_USER_NUMERIC_PRECISION, 0L, null))));
                break;
            case STATE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, "ID_WORKFLOW", Arrays.asList(
                        new PostgresColumnMetadata(tableName, "ID_WORKFLOW", "bigint", null,
                            STATE_NUMERIC_PRECISION, 0L, null))));
                break;
            case TEXT:
                final Long elementSize = dataElement.getDataSize().longValue();

                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "character varying", elementSize, null, null,
                            null))));
                break;
            case TIMESTAMP:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "timestamp without time zone", null, null,
                            null,
                            TIMESTAMP_DATETIME_PRECISION))));
                break;
            case YES_NO:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new PostgresColumnMetadata(tableName, columnName, "smallint", null,
                            YES_NO_NUMERIC_PRECISION, 0L, null))));
                break;
            default:
                throw new GeneralRuntimeException(
                    String.format("Mismatch finder does not know how to handle type type %s of data element %s",
                        dataType, dataElement));
        }
        return returnValue.stream();
    }

    @Override
    public AllDatabaseColumnsMetadata loadAllTablesColumns(final ExecutionContext etk) {
        final Collection<IDatabaseColumnMetadata> tableColumns = etk.createSQL(
            "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, DATETIME_PRECISION FROM information_schema.columns")
            .fetchList()
            .stream()
            .map(queryResultRow -> new PostgresColumnMetadata(
                ((String) queryResultRow.get("TABLE_NAME")).toUpperCase(),
                ((String) queryResultRow.get("COLUMN_NAME")).toUpperCase(),
                (String) queryResultRow.get("DATA_TYPE"),
                Optional.ofNullable((Number) queryResultRow.get("CHARACTER_MAXIMUM_LENGTH"))
                    .map(Number::longValue).orElse(null),
                Optional.ofNullable((Number) queryResultRow.get("NUMERIC_PRECISION")).map(Number::longValue)
                    .orElse(null),
                Optional.ofNullable((Number) queryResultRow.get("NUMERIC_SCALE")).map(Number::longValue)
                    .orElse(null),
                Optional.ofNullable((Number) queryResultRow.get("DATETIME_PRECISION")).map(Number::longValue)
                    .orElse(null)))
            .collect(Collectors.toList());
        return new AllDatabaseColumnsMetadata(tableColumns);
    }
}
