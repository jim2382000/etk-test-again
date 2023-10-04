package net.micropact.aea.du.page.updateLogViewerAjax;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.localization.Localizations;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.entellitrak.platform.DatabasePlatform;
import com.entellitrak.platform.PlatformInfo;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.Coercion;
import net.entellitrak.aea.gl.api.java.StringUtil;
import net.micropact.aea.core.enums.UpdateLogStatus;

/**
 * This is the controller code for a page which displays information regarding the entellitrak Apply Changes Update log.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class UpdateLogViewerAjaxController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Parameters parameters = etk.getParameters();
        final PlatformInfo platformInfo = etk.getPlatformInfo();

        final String limitParameter = StringUtil.toNonEmptyString(parameters.getSingle("limit"));
        final Long limit = Coercion.convertToLong(limitParameter);

        final TextResponse response = etk.createTextResponse();

        response.setContentType(ContentType.JSON);

		final String query = Map.of(
        		DatabasePlatform.ORACLE, "SELECT * FROM( SELECT updateLog.UPDATE_LOG_ID \"UPDATE_LOG_ID\", updateLog.STATUS \"STATUS\", updateLog.DESCRIPTION \"DESCRIPTION\", updateLog.END_TIMESTAMP \"END_TIMESTAMP\", updateLog.START_TIMESTAMP \"START_TIMESTAMP\", updateLog.IP_ADDRESS \"IP_ADDRESS\", u.USERNAME \"USERNAME\", ROW_NUMBER() OVER (ORDER BY start_timestamp DESC, update_log_id DESC) num FROM etk_update_log updateLog LEFT JOIN etk_user u ON u.user_id = updateLog.user_id ) updateLog WHERE :limit IS NULL OR updateLog.num <= :limit ORDER BY 1 DESC",
        		DatabasePlatform.SQL_SERVER, "SELECT * FROM( SELECT updateLog.UPDATE_LOG_ID \"UPDATE_LOG_ID\", updateLog.STATUS \"STATUS\", updateLog.DESCRIPTION \"DESCRIPTION\", updateLog.END_TIMESTAMP \"END_TIMESTAMP\", updateLog.START_TIMESTAMP \"START_TIMESTAMP\", updateLog.IP_ADDRESS \"IP_ADDRESS\", u.USERNAME \"USERNAME\", ROW_NUMBER() OVER (ORDER BY start_timestamp DESC, update_log_id DESC) num FROM etk_update_log updateLog LEFT JOIN etk_user u ON u.user_id = updateLog.user_id ) updateLog WHERE :limit IS NULL OR updateLog.num <= :limit ORDER BY 1 DESC",
        		DatabasePlatform.POSTGRESQL, "SELECT * FROM( SELECT updateLog.UPDATE_LOG_ID \"UPDATE_LOG_ID\", updateLog.STATUS \"STATUS\", updateLog.DESCRIPTION \"DESCRIPTION\", updateLog.END_TIMESTAMP \"END_TIMESTAMP\", updateLog.START_TIMESTAMP \"START_TIMESTAMP\", updateLog.IP_ADDRESS \"IP_ADDRESS\", u.USERNAME \"USERNAME\", ROW_NUMBER() OVER (ORDER BY start_timestamp DESC, update_log_id DESC) num FROM etk_update_log updateLog LEFT JOIN etk_user u ON u.user_id = updateLog.user_id ) updateLog WHERE :limit::NUMERIC IS NULL OR updateLog.num <= :limit ORDER BY 1 DESC"
        		).get(platformInfo.getDatabasePlatform());

        final List<Map<String, Object>> logEntries = etk.createSQL(query)
            .setParameter("limit", limit)
            .fetchList();

        logEntries.forEach(logEntry -> {
            final Date startTime = (Date) logEntry.get("START_TIMESTAMP");
            final Date endTime = (Date) logEntry.get("END_TIMESTAMP");

            logEntry.put("START_TIMESTAMP", startTime.getTime());
            logEntry.put("END_TIMESTAMP", endTime.getTime());

            logEntry.put("startTimeStampDisplay", formatTime(etk, startTime));
            logEntry.put("endTimeStampDisplay", formatTime(etk, endTime));

            logEntry.put("updateLogStatusDisplay",
                UpdateLogStatus.getFormattedString(etk, (String) logEntry.get("STATUS")));

            logEntry.put("duration", endTime.getTime() - startTime.getTime());
        });

        response.put("out", new Gson().toJson(logEntries));

        return response;
    }

    /**
     * Display a timestamp in the format preferred by the current user.
     *
     * @param etk
     *            entellitrak execution context
     * @param date
     *            date to be formatted
     * @return Localized display of the time for the current user
     */
    private static String formatTime(final ExecutionContext etk, final Date date) {
        return Localizations.toLocalTimestamp(etk.getCurrentUser().getTimeZonePreference(), date)
            .getTimestampString();
    }
}
