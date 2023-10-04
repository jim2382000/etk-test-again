package net.micropact.aea.du.page.viewBadDates;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataType;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This class serves as the controller code for a page which can be used to view Date fields which have a timestamp
 * portion.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class ViewBadDatesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("Bad Dates",
                    "page.request.do?page=du.page.viewBadDates")));

        response.put("badRecords", new Gson().toJson(
            EtkDataUtils.getAllDataElements(etk)
                .filter(dataElement -> DataType.DATE == dataElement.getDataType())
                .sorted(Comparator.comparing(DataElement::getBusinessKey))
                .flatMap(dataElement -> {
                    try {
                        final Stream<Map<String, Object>> returnValue;

                        final String tableName = dataElement.getDataObject().getTableName();
                        final String columnName = dataElement.getColumnName();

                        Long total = 0L;

                        if (Utility.isSqlServer(etk)) {
                            total = etk
                                .createSQL(
                                    String.format("SELECT COUNT(*) \"TOTAL\" FROM %s WHERE %s != CAST(%s AS DATE)",
                                        tableName,
                                        columnName,
                                        columnName))
                                .fetchLong();
                        } else if (Utility.isPostgreSQL(etk)) {
                            total = etk
                                .createSQL(
                                    String.format("SELECT COUNT(*) \"TOTAL\" FROM %s WHERE %s != %s::date",
                                        tableName,
                                        columnName,
                                        columnName))
                                .fetchLong();
                        } else {
                            total = etk
                                .createSQL(String.format("SELECT COUNT(*) \"TOTAL\" FROM %s WHERE %s != trunc(%s)",
                                    tableName,
                                    columnName,
                                    columnName))
                                .fetchLong();
                        }

                        if (total > 0) {
                            returnValue = Stream.of(Utility.arrayToMap(String.class, Object.class, new Object[][] {
                                { "elementBusinessKey", dataElement.getBusinessKey() },
                                { "total", total },
                                { "query", generateRecordsQuery(etk, tableName, columnName) },
                            }));
                        } else {
                            returnValue = Stream.empty();
                        }

                        return returnValue;
                    } catch (final IncorrectResultSizeDataAccessException e) {
                        throw new GeneralRuntimeException(e);
                    }
                }).collect(Collectors.toList())));

        return response;
    }

    /**
     * Generates a SQL query which will return the specific records which have an issue with a specific date field.
     *
     * @param etk
     *            entellitrak execution context
     * @param tableName
     *            table name of the table containing the date field
     * @param columnName
     *            the column which holds the date field
     * @return the SQL query which will return the records.
     */
    private static String generateRecordsQuery(
        final PageExecutionContext etk,
        final String tableName,
        final String columnName) {
        return String.format(Utility.isSqlServer(etk) ? "SELECT * FROM %s WHERE %s != CAST(%s AS DATE)"
            : "SELECT * FROM %s WHERE %s != TRUNC(%s) ORDER BY id",
            tableName,
            columnName,
            columnName);
    }
}
