package net.micropact.aea.du.utility.mismatchedColumnTypes.oracle;

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
 * Oracle-specific information for the column mismatch finding utility.
 *
 * @author Zachary.Miller
 */
public class OracleMismatchFinderConfig implements IMismatchFinderPlatformConfig {

    /* All these constants are defined up here to make checkstyle happy, however it may actually hurt readability */

    private static final Long CURRENCY_LENGTH = 22L;
    private static final Long CURRENCY_PRECISION = 18L;
    private static final Long CURRENCY_SCALE = 2L;

    private static final Long DATE_LENGTH = 7L;

    private static final Long FILE_LENGTH = 22L;
    private static final Long FILE_PRECISION = 19L;

    private static final Long CLOB_LENGTH = 4000L;

    private static final Long NUMBER_LENGTH = 22L;
    private static final Long NUMBER_PRECISION = 10L;

    private static final Long LONG_LENGTH = 22L;
    private static final Long LONG_PRECISION = 19L;

    private static final Long PASSWORD_HASH_LENGTH = 255L;
    private static final Long PASSWORD_HASH_LENGTH_2 = 1020L;

    private static final Long PASSWORD_USER_LENGTH = 22L;
    private static final Long PASSWORD_USER_PRECISION = 19L;

    private static final Long TIMESTAMP_LENGTH = 11L;
    private static final Long TIMESTAMP_SCALE = 6L;

    private static final Long STATE_LENGTH = 22L;
    private static final Long STATE_PRECISION = 19L;

    private static final Long YES_NO_LENGTH = 22L;
    private static final Long YES_NO_PRECISION = 1L;

    @Override
    public Stream<ElementAcceptableColumns> getExpectedElementColumns(final DataElement dataElement) {
        final String tableName = EtkDataUtils.getActualTableName(dataElement);
        final String columnName = dataElement.getColumnName();
        final DataType dataType = dataElement.getDataType();

        final Collection<ElementAcceptableColumns> returnValue;

        switch (dataType) {
            case CURRENCY:
				final long numberOfDecimalPlaces = MismatchedColumnTypesUtil.findNumberOfDecimalPlaces(dataElement);

				final long expectedPrecision = CURRENCY_PRECISION + numberOfDecimalPlaces;

				returnValue = Arrays.asList(
	                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
	                    	// Legacy columns were hardcoded to this value
	                        new OracleColumnMetadata(tableName, columnName, "NUMBER", CURRENCY_LENGTH, CURRENCY_PRECISION, null, 0L, null, CURRENCY_SCALE),
	                        // New columns are dynamic, based on the numberOfDigits
	                        new OracleColumnMetadata(tableName, columnName, "NUMBER", CURRENCY_LENGTH, expectedPrecision, null, 0L, null, numberOfDecimalPlaces))));
                break;
            case DATE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "DATE", DATE_LENGTH, null, null, 0L, null,
                            null))));
                break;
            case FILE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "NUMBER", FILE_LENGTH, FILE_PRECISION, null, 0L,
                            null, 0L))));
                break;
            case LONG:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "NUMBER", LONG_LENGTH, LONG_PRECISION, null, 0L,
                            null, 0L))));
                break;
            case LONG_TEXT:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "CLOB", CLOB_LENGTH, null, CLOB_LENGTH, 0L,
                            null, null))));
                break;
            case NONE:
                returnValue = Collections.emptyList();
                break;
            case NUMBER:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "NUMBER", NUMBER_LENGTH, NUMBER_PRECISION, null,
                            0L, null, 0L))));
                break;
            case PASSWORD:
                final String dateColumnName = String.format("%s_DTS", columnName);
                final String userIdColumnName = String.format("%s_UID", columnName);

                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", PASSWORD_HASH_LENGTH, null,
                            PASSWORD_HASH_LENGTH, PASSWORD_HASH_LENGTH, "B", null),
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", PASSWORD_HASH_LENGTH, null,
                            PASSWORD_HASH_LENGTH, PASSWORD_HASH_LENGTH, "C", null),
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", PASSWORD_HASH_LENGTH_2, null,
                            PASSWORD_HASH_LENGTH_2, PASSWORD_HASH_LENGTH, "C", null))),

                    new ElementAcceptableColumns(dataElement, tableName, dateColumnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, dateColumnName, "DATE", DATE_LENGTH, null, null, 0L, null,
                            null),
                        new OracleColumnMetadata(tableName, dateColumnName, "TIMESTAMP(6)", TIMESTAMP_LENGTH, null,
                            null, 0L, null, TIMESTAMP_SCALE))),
                    new ElementAcceptableColumns(dataElement, tableName, userIdColumnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, userIdColumnName, "NUMBER", PASSWORD_USER_LENGTH,
                            PASSWORD_USER_PRECISION, null, 0L, null, 0L))));
                break;
            case STATE:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, "ID_WORKFLOW", Arrays.asList(
                        new OracleColumnMetadata(tableName, "ID_WORKFLOW", "NUMBER", STATE_LENGTH, STATE_PRECISION,
                            null, 0L, null, 0L))));
                break;
            case TEXT:
                final Long elementSize = dataElement.getDataSize().longValue();

                final long largeCharDeclLength = Math.min(elementSize * 4, 4000);

                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", elementSize, null, elementSize,
                            elementSize, "B", null),
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", elementSize, null, elementSize,
                            elementSize, "C", null),
                        new OracleColumnMetadata(tableName, columnName, "VARCHAR2", largeCharDeclLength, null,
                            largeCharDeclLength, elementSize, "C", null))));
                break;
            case TIMESTAMP:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "DATE", DATE_LENGTH, null, null, 0L, null,
                            null),
                        new OracleColumnMetadata(tableName, columnName, "TIMESTAMP(6)", TIMESTAMP_LENGTH, null, null,
                            0L, null, TIMESTAMP_SCALE))));
                break;
            case YES_NO:
                returnValue = Arrays.asList(
                    new ElementAcceptableColumns(dataElement, tableName, columnName, Arrays.asList(
                        new OracleColumnMetadata(tableName, columnName, "NUMBER", YES_NO_LENGTH, YES_NO_PRECISION, null,
                            0L, null, 0L))));
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
            "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, CHAR_COL_DECL_LENGTH, CHAR_LENGTH, CHAR_USED, DATA_SCALE FROM user_tab_cols")
            .fetchList()
            .stream()
            .map(queryResultRow -> new OracleColumnMetadata(
                (String) queryResultRow.get("TABLE_NAME"),
                (String) queryResultRow.get("COLUMN_NAME"),
                (String) queryResultRow.get("DATA_TYPE"),
                ((Number) queryResultRow.get("DATA_LENGTH")).longValue(),
                Optional.ofNullable((Number) queryResultRow.get("DATA_PRECISION")).map(Number::longValue).orElse(null),
                Optional.ofNullable((Number) queryResultRow.get("CHAR_COL_DECL_LENGTH")).map(Number::longValue)
                    .orElse(null),
                ((Number) queryResultRow.get("CHAR_LENGTH")).longValue(),
                (String) queryResultRow.get("CHAR_USED"),
                Optional.ofNullable((Number) queryResultRow.get("DATA_SCALE")).map(Number::longValue).orElse(null)))
            .collect(Collectors.toList());
        return new AllDatabaseColumnsMetadata(tableColumns);
    }
}
