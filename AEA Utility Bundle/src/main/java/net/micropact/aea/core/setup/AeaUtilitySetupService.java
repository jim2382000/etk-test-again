package net.micropact.aea.core.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.entellitrak.ExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.dynamic.AeaCoreConfiguration;
import com.entellitrak.permission.PermissionAccessLevelType;
import com.entellitrak.user.Role;

import net.entellitrak.aea.cdpapi.unstable.ConfigureDataPermissionsService;
import net.entellitrak.aea.cdpapi.unstable.DataElementPermissions;
import net.entellitrak.aea.cdpapi.unstable.DataObjectPermissions;
import net.entellitrak.aea.cdpapi.unstable.DataPermissionsDto;
import net.entellitrak.aea.core.CoreServiceFactory;
import net.entellitrak.aea.gl.api.etk.DataObjectUtil;

public class AeaUtilitySetupService {

	private AeaUtilitySetupService() {}

	public static List<String> setupAeaUtility(final ExecutionContext etk) {
		final List<String> messages = new ArrayList<>();

		messages.add("Setting up Role Data Permissions");
		ConfigureDataPermissionsService.updateDataPermissions(etk, getDesiredPermissions(etk, messages));

		messages.add(CoreServiceFactory
				.getDeploymentService(etk)
				.runComponentSetup()
				.getSummaryString());

		return messages;
	}

	private static Collection<DataObjectPermissions> getDesiredPermissions(final ExecutionContext etk, final List<String> messageBuilder) {
		final RoleService roleService = etk.getRoleService();
		final DataElementService dataElementService = etk.getDataElementService();

		final String administratorRoleBusinessKey = "role.administration";
		final Role role = roleService.getRoleByBusinessKey(administratorRoleBusinessKey);

		if(role == null) {
			messageBuilder.add(String.format("Role %s not found. Will skip", administratorRoleBusinessKey));
			return Collections.emptyList();
		} else {
			return List.of(
					new DataObjectPermissions(
							role,
							DataObjectUtil.getDataObjectByDynamicClass(etk, AeaCoreConfiguration.class),
							new DataPermissionsDto(
									true,
									PermissionAccessLevelType.NONE,
									PermissionAccessLevelType.GLOBAL,
									PermissionAccessLevelType.GLOBAL,
									PermissionAccessLevelType.NONE,
									PermissionAccessLevelType.NONE,
									PermissionAccessLevelType.NONE,
									PermissionAccessLevelType.NONE),
							List.of(
									new DataElementPermissions(dataElementService.getDataElementByBusinessKey("object.aeaCoreConfiguration.element.code"),
											new DataPermissionsDto(
													false,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE)),
									new DataElementPermissions(dataElementService.getDataElementByBusinessKey("object.aeaCoreConfiguration.element.description"),
											new DataPermissionsDto(
													false,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE)),
									new DataElementPermissions(dataElementService.getDataElementByBusinessKey("object.aeaCoreConfiguration.element.file"),
											new DataPermissionsDto(
													false,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.GLOBAL,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.NONE,
													PermissionAccessLevelType.GLOBAL)))));
		}
	}
}
