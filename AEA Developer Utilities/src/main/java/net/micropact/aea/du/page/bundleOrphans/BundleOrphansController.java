package net.micropact.aea.du.page.bundleOrphans;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.group.Group;
import com.entellitrak.group.GroupService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.job.Job;
import com.entellitrak.job.JobService;
import com.entellitrak.legacy.report.ReportService;
import com.entellitrak.page.Page;
import com.entellitrak.page.PageController;
import com.entellitrak.page.PageService;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.query.Query;
import com.entellitrak.query.QueryService;
import com.entellitrak.report.Report;
import com.entellitrak.system.SystemEventListener;
import com.entellitrak.system.SystemEventListenerService;
import com.entellitrak.user.Role;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.etk.BundleComponentType;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.set.SetUtil;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * Controller code for page which identifies objects which are not in a bundle, or records which are supposedly in
 * bundles, but cannot be found in the system.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class BundleOrphansController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Bundle Orphans",
								"page.request.do?page=du.page.bundleOrphans")));

		final List<Discrepancy> bundleOrphans = Arrays.stream(BundleComponentType.values())
				.flatMap(bundleComponentType -> BundleOrphansController.getBundleComponentTypeDiscrepencies(etk, bundleComponentType))
				.sorted(Comparator.comparing(Discrepancy::geDiscrepancyType)
						.thenComparing(Discrepancy::getBundleComponentType)
						.thenComparing(Discrepancy::getComponentBusinessKey))
				.collect(Collectors.toList());

		response.put("bundleOrphans", new Gson().toJson(bundleOrphans
				.stream()
				.map(bundleOrphan -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
					{"discrepancyType", bundleOrphan.geDiscrepancyType().getDisplay()},
					{"bundleComponentType", bundleOrphan.getBundleComponentType().getEntellitrakKey()},
					{"componentBusinessKey", bundleOrphan.getComponentBusinessKey()},
				})).collect(Collectors.toList())));

		return response;
	}

	/**
	 * Get the discrepancies for a particular bundle component type.
	 *
	 * @param etk entellitrak execution context
	 * @param bundleComponentType the bundle component type
	 * @return the discrepancies
	 */
	private static Stream<Discrepancy> getBundleComponentTypeDiscrepencies(final ExecutionContext etk, final BundleComponentType bundleComponentType) {
		try {
			final GroupService groupService = etk.getGroupService();
			final JobService jobService = etk.getJobService();
			final PageService pageService = etk.getPageService();
			final QueryService queryService = etk.getQueryService();
			final ReportService reportService = etk.getReportService();
			final RoleService roleService = etk.getRoleService();
			final SystemEventListenerService systemEventListenerService = etk.getSystemEventListenerService();
			final WorkspaceService workspaceService = etk.getWorkspaceService();

			final Collection<String> allComponents;
			final Collection<String> allMappings;

			if(bundleComponentType == BundleComponentType.SCRIPT_OBJECT){
				allMappings = QueryUtility.toSimpleList(etk.createSQL("SELECT business_key FROM etk_bundle_script_object WHERE workspace_id = :workspaceId")
						.setParameter("workspaceId", Utility.getSystemRepositoryWorkspaceId(etk))
						.fetchList());
			} else {
				allMappings = QueryUtility.toSimpleList(etk.createSQL("SELECT component_business_key FROM etk_bundle_mapping WHERE component_type = :componentType")
						.setParameter("componentType", bundleComponentType.getEntellitrakKey())
						.fetchList());
			}

			switch(bundleComponentType) {
			case DATA_OBJECT:
				allComponents = QueryUtility.toSimpleList(etk.createSQL("SELECT business_key FROM etk_data_object WHERE tracking_config_id = :trackingConfigId")
						.setParameter("trackingConfigId", Utility.getTrackingConfigIdNext(etk))
						.fetchList());
				break;
			case GROUP:
				allComponents = groupService
				.getGroups()
				.stream()
				.map(Group::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case JOB:
				allComponents = jobService
				.getJobs()
				.stream()
				.map(Job::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case LOOKUP_DEFINITION:
				allComponents = QueryUtility.toSimpleList(etk.createSQL("SELECT business_key FROM etk_lookup_definition WHERE tracking_config_id = :trackingConfigId")
						.setParameter("trackingConfigId", Utility.getTrackingConfigIdNext(etk))
						.fetchList());
				break;
			case PACKAGE:
				allComponents = Collections.emptyList();
				break;
			case PAGE:
				allComponents = pageService
				.getPages()
				.stream()
				.map(Page::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case PLUG_IN:
				allComponents = QueryUtility.toSimpleList(etk.createSQL("SELECT business_key FROM etk_plugin_registration WHERE tracking_config_id = :trackingConfigId")
						.setParameter("trackingConfigId", Utility.getTrackingConfigIdNext(etk))
						.fetchList());
				break;
			case QUERY:
				allComponents = queryService
				.getQueries()
				.stream()
				.map(Query::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case REPORT:
				allComponents = reportService
				.getReports()
				.stream()
				.map(Report::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case ROLE:
				allComponents = roleService
				.getRoles()
				.stream()
				.map(Role::getBusinessKey)
				.collect(Collectors.toList());
				break;
			case SCRIPT_OBJECT:
				final Workspace systemWorkspace = workspaceService.getSystemWorkspace();
				allComponents = workspaceService.getScripts(systemWorkspace)
						.stream()
						.map(Script::getBusinessKey)
						.collect(Collectors.toList());
				break;
			case SYSTEM_EVENT_LISTENER:
				allComponents = systemEventListenerService
				.getSystemEventListeners()
				.stream()
				.map(SystemEventListener::getBusinessKey)
				.collect(Collectors.toList());
				break;
			default:
				throw new GeneralRuntimeException(String.format("Do not know how to get all components of type %s", bundleComponentType));
			}


			final Set<String> allMappingsSet = new HashSet<>(allMappings);
			final Set<String> allComponentsSet = new HashSet<>(allComponents);

			final Stream<Discrepancy> extraMappings = SetUtil.setDifference(allMappingsSet, allComponentsSet)
					.stream()
					.map(item -> new Discrepancy(DiscrepancyType.EXTRA_MAPPING, bundleComponentType, item));

			final Stream<Discrepancy> extraComponents = SetUtil.setDifference(allComponentsSet, allMappingsSet)
					.stream()
					.map(item -> new Discrepancy(DiscrepancyType.MISSING_MAPPING, bundleComponentType, item));

			return Stream.concat(extraMappings, extraComponents);
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	/**
	 * A single bundle component discrepancy.
	 *
	 * @author Zachary.Miller
	 */
	public static class Discrepancy {

		private final DiscrepancyType discrepancyType;
		private final BundleComponentType bundleComponentType;
		private final String componentBusinessKey;

		/**
		 * Simple constructor.
		 *
		 * @param theDiscrepancyType the discrepancy type
		 * @param theBundleComponentType the bundle component type
		 * @param theComponentBusinessKey the component business key
		 */
		public Discrepancy(final DiscrepancyType theDiscrepancyType, final BundleComponentType theBundleComponentType, final String theComponentBusinessKey) {
			discrepancyType = theDiscrepancyType;
			bundleComponentType = theBundleComponentType;
			componentBusinessKey = theComponentBusinessKey;
		}

		/**
		 * Get the discrepancy type.
		 *
		 * @return the discrepancy type
		 */
		public DiscrepancyType geDiscrepancyType() {
			return discrepancyType;
		}

		/**
		 * Get the bundle component type.
		 *
		 * @return the bundle component type
		 */
		public BundleComponentType getBundleComponentType() {
			return bundleComponentType;
		}

		/**
		 * Get the component business key.
		 *
		 * @return the component business key
		 */
		public String getComponentBusinessKey() {
			return componentBusinessKey;
		}
	}

	/**
	 * The type of a bundle discrepancy.
	 *
	 * @author Zachary.Miller
	 */
	public enum DiscrepancyType {

		/**
		 * A record exists in entellitrak, but no bundle mapping entry was found.
		 */
		MISSING_MAPPING("Missing Mapping"),
		/**
		 * A mapping entry exists, but the component was not found in entellitrak.
		 */
		EXTRA_MAPPING("Extra Mapping");

		private final String display;

		/**
		 * Simple constructor.
		 *
		 * @param theDisplay the display value
		 */
		DiscrepancyType(final String theDisplay) {
			display = theDisplay;
		}

		/**
		 * Get the user-friendly representation of the discrepancy type.
		 *
		 * @return the display value
		 */
		public String getDisplay() {
			return display;
		}
	}
}
