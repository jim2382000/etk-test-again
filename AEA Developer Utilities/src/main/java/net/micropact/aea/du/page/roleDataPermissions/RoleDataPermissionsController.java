package net.micropact.aea.du.page.roleDataPermissions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.NavigationService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.permission.DataPermissionType;
import com.entellitrak.permission.DataPermissions;
import com.entellitrak.user.Role;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.entellitrak.aea.gl.api.java.map.MapBuilder;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * <p>
 * This page serves as the controller code for a page which can be used to display the Role Data Permissions for Data
 * Objects in the system.
 * </p>
 *
 * <p>
 * Note: Before entellitrak 3.21.0.0.0 (Role-base data element permissions) this page used to be able to update existing
 * permissions. With that release the page was temporarily disabled, and then later it was changed to just display Data
 * Object permissions (and not support update).
 * </p>
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class RoleDataPermissionsController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final RoleService roleService = etk.getRoleService();
		final Parameters parameters = etk.getParameters();
		final DataObjectService dataObjectService = etk.getDataObjectService();

		/*
		 * We need to know whether this is the first time they are viewing this page, or whether they have hit the
		 * refresh button. This is because we want to select all of the permissions on new, but want to only take
		 * the submitted ones on updated.
		 */
		final boolean isCreate = parameters.getSingle("update") == null;
		final List<String> selectedRoles = Optional.ofNullable(parameters.getField("roles"))
				.orElse(new ArrayList<>());
		final List<String> selectedDataObjects = Optional.ofNullable(parameters.getField("dataObjects"))
				.orElse(new ArrayList<>());
		final List<String> selectedPermissions = Optional.ofNullable(parameters.getField("permissionTypes"))
				.orElse(new ArrayList<>());

		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Role Data Permissions",
								"page.request.do?page=du.page.roleDataPermissions")));

		// Roles
		final List<Map<String, Object>> allRoles = roleService.getRoles()
				.stream()
				.sorted(Comparator.comparing(Role::getName))
				.map(role -> {
					final Map<String, Object> map = new HashMap<>();

					map.put("ROLE_ID", role.getId());
					map.put("NAME", role.getName());

					return map;
				})
				.collect(Collectors.toList());
		addEnabled(allRoles, "ROLE_ID", selectedRoles);

		final List<Map<String, Object>> allDataObjects = dataObjectService.getDataObjects()
				.stream()
				.sorted(Comparator.comparing((final DataObject dataObject) -> getListOrder(etk, dataObject))
						.thenComparing(DataObject::getLabel))
				.map((final DataObject dataObject) -> new MapBuilder<String, Object>()
						.put("DATA_OBJECT_BUSINESS_KEY", dataObject.getBusinessKey())
						.put("PARENT_OBJECT_BUSINESS_KEY", Optional.ofNullable(dataObjectService.getParent(dataObject)).map(DataObject::getBusinessKey).orElse(null))
						.put("LABEL", dataObject.getLabel())
						.put("dataObjectType", dataObject.getObjectType())
						.build())
				.collect(Collectors.toList());
		addEnabled(allDataObjects, "DATA_OBJECT_BUSINESS_KEY", selectedDataObjects);

		final List<Map<Object, Object>> dataObjectPermissions = roleService.getRoles()
				.stream()
				.flatMap(role -> dataObjectService.getDataObjects()
						.stream()
						.map(dataObject -> {
							final DataPermissions dataPermissions = roleService.getDataObjectPermissions(role, dataObject);

							dataPermissions.getAccessLevel(DataPermissionType.CREATE);

							return new MapBuilder<>()
									.put("DATA_OBJECT_TYPE", dataObject.getBusinessKey())
									.put("ROLE_ID", role.getId())
									.put("CREATE_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.CREATE))
									.put("READ_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.READ))
									.put("UPDATE_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.UPDATE))
									.put("DELETE_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.DELETE))
									.put("ASSIGN_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.ASSIGN))
									.put("SEARCHING_ACCESS_LEVEL", dataPermissions.getAccessLevel(DataPermissionType.SEARCH))
									.build();
						}))
				.collect(Collectors.toList());

		final Gson gson = new GsonBuilder().serializeNulls().create();
		response.put("roles", gson.toJson(allRoles));
		response.put("dataObjects", gson.toJson(allDataObjects));
		response.put("selectedPermissions", gson.toJson(selectedPermissions));
		response.put("dataObjectPermissions", gson.toJson(dataObjectPermissions));
		response.put("isCreate", gson.toJson(isCreate));

		return response;
	}

	private static long getListOrder(final ExecutionContext etk, final DataObject dataObject) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final NavigationService navigationService = etk.getNavigationService();

		final DataObject parentObject = dataObjectService.getParent(dataObject);
		if(parentObject == null) {
			return 0;
		} else {
			return navigationService.getOrderedChildObjects(parentObject).indexOf(dataObject);
		}
	}

	/**
	 * This method adds the enabled property to multiselects so that they can be persisted across page refreshes. Each
	 * map in allValues will be given an enabled property. The property will be true, if the identifier's value (found
	 * via the idPoperty) matches a value in selectedValues (after converting it to a String).
	 *
	 * @param allValues
	 *            the raw database maps to add the enabled property to
	 * @param idProperty
	 *            the key which is used as the identifier for the values
	 * @param selectedValues
	 *            The String representations of the selected values
	 */
	private static void addEnabled(final List<Map<String, Object>> allValues,
			final String idProperty,
			final List<String> selectedValues) {
		allValues.stream().forEachOrdered(
				allValue -> allValue.put("enabled", selectedValues.contains(allValue.get(idProperty).toString())));
	}
}
