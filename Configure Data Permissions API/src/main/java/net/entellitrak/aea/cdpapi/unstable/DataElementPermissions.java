package net.entellitrak.aea.cdpapi.unstable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.entellitrak.configuration.DataElement;

/**
 * Class representing the desired permissions for a particular data element.
 *
 * @author Zachary.Miller
 */
public class DataElementPermissions {

	private final DataElement dataElement;
	private final DataPermissionsDto dataPermissions;

	public DataElementPermissions(final DataElement theDataElement, final DataPermissionsDto theDataPermissions) {
		dataElement = theDataElement;
		dataPermissions = theDataPermissions;
	}

	public DataElement getDataElement() {
		return dataElement;
	}

	public DataPermissionsDto getDataPermissions() {
		return dataPermissions;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("dataElement", dataElement.getBusinessKey())
				.append("dataPermissions", dataPermissions)
				.build();
	}
}