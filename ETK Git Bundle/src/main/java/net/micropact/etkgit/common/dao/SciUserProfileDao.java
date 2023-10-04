/**
 *
 * SciUserProfileDao
 *
 * administrator 05/22/2020
 **/

package net.micropact.etkgit.common.dao;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.dynamic.SciUserProfile;
import com.entellitrak.user.User;

public class SciUserProfileDao {
    /**
	 * Private constructor to hide.
	 */
	private SciUserProfileDao() { }

	public static SciUserProfile getSciUserProfile(final ExecutionContext etk, final User user) throws ApplicationException {
		final var userDataElement = etk.getDataElementService().getDataElementByBusinessKey("object.sciUserProfile.element.user");
		final var criteria = etk.createCriteria();
		criteria.add(criteria.equals(userDataElement, user.getId()));

		return etk.getDynamicObjectService().get(SciUserProfile.class, criteria).stream()
				.findFirst()
				.orElseThrow(() -> new ApplicationException(String.format("No SCI User Profile found for user %s",
						etk.getCurrentUser().getAccountName())));
	}
}
