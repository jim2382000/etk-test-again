package net.entellitrak.aea.cdpapi.unstable;

import java.util.Collection;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.permission.DataPermissionType;
import com.entellitrak.permission.DataPermissions;
import com.entellitrak.permission.DataPermissionsFeatureType;
import com.entellitrak.permission.DataPermissionsService;
import com.entellitrak.user.Role;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Main entry point for configuring the data permissions.
 *
 * @author Zachary.Miller
 */
public class ConfigureDataPermissionsService {

	private ConfigureDataPermissionsService() {
	}

	/**
	 * Update the permissions for the specified object/elements.
	 *
	 * The way this method behaves is to set the object permissions, and cascade them to the elements.
	 * It then overrides any specified data elements.
	 * This means that element permissions only need to be specified if they do not match the data object's permissions.
	 *
	 * @param etk execution context
	 * @param desiredDataObjectPermissions the desired permissions
	 */
	public static void updateDataPermissions(final ExecutionContext etk, final Collection<DataObjectPermissions> desiredDataObjectPermissions) {
		final RoleService roleService = etk.getRoleService();
		final DataPermissionsService dataPermissionsService = etk.getDataPermissionsService();

		desiredDataObjectPermissions.forEach(desiredPermission -> {
			try {
				final Role role = desiredPermission.getRole();

				final DataObject dataObject = desiredPermission.getDataObject();

				final DataPermissionsDto desiredObjectPermissions = desiredPermission.getDataPermissions();

				final DataPermissions dataObjectPermissions = dataPermissionsService.createDataPermissions();

				if(desiredPermission.getDataPermissions().getInboxEnabled()) {
					dataObjectPermissions.enableDataPermissionsFeatureType(DataPermissionsFeatureType.INBOX);
				}
				dataObjectPermissions.setAccessLevel(DataPermissionType.CREATE, desiredObjectPermissions.getCreatePermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.READ, desiredObjectPermissions.getReadPermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.UPDATE, desiredObjectPermissions.getUpdatePermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.DELETE, desiredObjectPermissions.getDeletePermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.ASSIGN, desiredObjectPermissions.getAssignPermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.SEARCH, desiredObjectPermissions.getSearchPermission());
				dataObjectPermissions.setAccessLevel(DataPermissionType.READ_CONTENT, desiredObjectPermissions.getReadContentPermission());

				roleService.saveDataObjectPermissions(role, dataObject, dataObjectPermissions, true);

				desiredPermission.getDataElementPermissions()
				.forEach(desiredElementPermission -> {
					try {
						final DataElement dataElement = desiredElementPermission.getDataElement();
						final DataPermissionsDto desiredDataElementPermissons = desiredElementPermission.getDataPermissions();

						final DataPermissions elementDataPermissions = dataPermissionsService.createDataPermissions();
						elementDataPermissions.setAccessLevel(DataPermissionType.CREATE, desiredDataElementPermissons.getCreatePermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.READ, desiredDataElementPermissons.getReadPermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.UPDATE, desiredDataElementPermissons.getUpdatePermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.DELETE, desiredDataElementPermissons.getDeletePermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.ASSIGN, desiredDataElementPermissons.getAssignPermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.SEARCH, desiredDataElementPermissons.getSearchPermission());
						elementDataPermissions.setAccessLevel(DataPermissionType.READ_CONTENT, desiredDataElementPermissons.getReadContentPermission());

						roleService.saveDataElementPermissions(role, dataElement, elementDataPermissions);
					} catch (final ApplicationException e) {
						throw new GeneralRuntimeException(
								String.format("Problem updating data element permissions %s",
										desiredElementPermission),
								e);
					}
				});
			} catch (final ApplicationException e) {
				throw new GeneralRuntimeException(
						String.format("Problem updating permissions for object %s",
								desiredPermission),
						e);
			}
		});
	}
}
