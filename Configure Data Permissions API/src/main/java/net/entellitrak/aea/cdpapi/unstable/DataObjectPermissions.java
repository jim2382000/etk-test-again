package net.entellitrak.aea.cdpapi.unstable;

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.entellitrak.configuration.DataObject;
import com.entellitrak.user.Role;

/**
 * Class representing the desired permissions for a particular data object.
 * This includes the data elements of that data object.
 *
 * @author Zachary.Miller
 */
public class DataObjectPermissions {

	private final Role role;
	private final DataObject dataObject;
	private final DataPermissionsDto dataPermissions;
	private final Collection<DataElementPermissions> dataElementPermissions;

	public DataObjectPermissions(final Role theRole, final DataObject theDataObject, final DataPermissionsDto theDataPermissions,
			final Collection<DataElementPermissions> theDataElementPermissions) {
		role = theRole;
		dataObject = theDataObject;
		dataPermissions = theDataPermissions;
		dataElementPermissions = theDataElementPermissions;
	}

	public Role getRole() {
		return role;
	}

	public DataObject getDataObject() {
		return dataObject;
	}

	public DataPermissionsDto getDataPermissions() {
		return dataPermissions;
	}

	public Collection<DataElementPermissions> getDataElementPermissions() {
		return dataElementPermissions;
	}

	@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("role", role.getBusinessKey())
					.append("dataObject", dataObject.getBusinessKey())
					.append("dataPermissions", dataPermissions)
					.append("dataElementPermissions", dataElementPermissions)
					.build();
		}
}