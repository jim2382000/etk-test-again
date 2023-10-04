package net.entellitrak.aea.cdpapi.unstable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.entellitrak.permission.PermissionAccessLevelType;

/**
 * Class representing a row of the ETK_DATA_PERMISSION table,
 * used by both data objects and data elements.
 *
 * @author Zachary.Miller
 */
public class DataPermissionsDto {

	private final boolean inboxEnabled;
	private final PermissionAccessLevelType createPermission;
	private final PermissionAccessLevelType readPermission;
	private final PermissionAccessLevelType updatePermission;
	private final PermissionAccessLevelType deletePermission;
	private final PermissionAccessLevelType assignPermission;
	private final PermissionAccessLevelType searchPermission;
	private final PermissionAccessLevelType readContentPermission;

	// Suppress warning about too many parameters. We would rather the class be immutable.
	@SuppressWarnings("java:S107")
	public DataPermissionsDto(
			final boolean theInboxEnabled,
			final PermissionAccessLevelType theCreatePermission,
			final PermissionAccessLevelType theReadPermission,
			final PermissionAccessLevelType theUpdatePermission,
			final PermissionAccessLevelType theDeletePermission,
			final PermissionAccessLevelType theAssignPermission,
			final PermissionAccessLevelType theSearchPermission,
			final PermissionAccessLevelType theReadContentPermission) {
		inboxEnabled = theInboxEnabled;
		createPermission = theCreatePermission;
		readPermission = theReadPermission;
		updatePermission = theUpdatePermission;
		deletePermission = theDeletePermission;
		assignPermission = theAssignPermission;
		searchPermission = theSearchPermission;
		readContentPermission = theReadContentPermission;
	}

	public boolean getInboxEnabled() {
		return inboxEnabled;
	}

	public PermissionAccessLevelType getCreatePermission() {
		return createPermission;
	}

	public PermissionAccessLevelType getReadPermission() {
		return readPermission;
	}

	public PermissionAccessLevelType getUpdatePermission() {
		return updatePermission;
	}

	public PermissionAccessLevelType getDeletePermission() {
		return deletePermission;
	}

	public PermissionAccessLevelType getAssignPermission() {
		return assignPermission;
	}

	public PermissionAccessLevelType getSearchPermission() {
		return searchPermission;
	}

	public PermissionAccessLevelType getReadContentPermission() {
		return readContentPermission;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}