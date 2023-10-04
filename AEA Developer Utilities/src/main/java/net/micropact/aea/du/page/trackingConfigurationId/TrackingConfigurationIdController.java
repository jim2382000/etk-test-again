package net.micropact.aea.du.page.trackingConfigurationId;

import com.entellitrak.ApplicationException;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This page is for displaying the queries for the current and next tracking configuration ids.
 * When developing frameworks it is very common to need one of these ids because you need them to be able to interact
 * with most ETK_ tables.
 * The currently deployed id is the id that users experience and is what is reflect in all tabs except the
 * configuration tab.
 * The next deployed id is the id that is shown in the configuration tab and will become the currently deployed id
 * once somebody Applies Changes
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class TrackingConfigurationIdController implements PageController {

    private static final String CURRENTLY_DEPLOYED_TRACKING_CONFIG_QUERY = "SELECT MAX (tracking_config_id) FROM etk_tracking_config_archive";
    private static final String NEXT_DEPLOYED_TRACKING_CONFIG_QUERY = "SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)";

    @Override
    public Response execute(final PageExecutionContext etk)
            throws ApplicationException {
        try {
            final TextResponse response = etk.createTextResponse();

            BreadcrumbUtility.setBreadcrumbAndTitle(response,
                    BreadcrumbUtility.addLastChildFluent(
                            DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                            new SimpleBreadcrumb("Tracking Configuration Id",
                                    "page.request.do?page=du.page.trackingConfigurationId")));

            final String currentlyDeployedVersion = etk.createSQL(CURRENTLY_DEPLOYED_TRACKING_CONFIG_QUERY).fetchString();
            final String nextDeployedVersion = etk.createSQL(NEXT_DEPLOYED_TRACKING_CONFIG_QUERY).fetchString();

            final Gson gson = new Gson();
            response.put("currentlyDeployedQuery", gson.toJson(CURRENTLY_DEPLOYED_TRACKING_CONFIG_QUERY));
            response.put("nextDeployedQuery", gson.toJson(NEXT_DEPLOYED_TRACKING_CONFIG_QUERY));
            response.put("currentlyDeployedVersion", gson.toJson(currentlyDeployedVersion));
            response.put("nextDeployedVersion", gson.toJson(nextDeployedVersion));

            return response;
        } catch (final RuntimeException | IncorrectResultSizeDataAccessException e) {
            throw new ApplicationException(e);
        }
    }
}
