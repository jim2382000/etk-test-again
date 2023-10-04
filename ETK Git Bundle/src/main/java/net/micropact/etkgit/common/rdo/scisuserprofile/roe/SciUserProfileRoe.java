package net.micropact.etkgit.common.rdo.scisuserprofile.roe;

import com.entellitrak.ApplicationException;
import com.entellitrak.DataEventType;
import com.entellitrak.InputValidationException;
import com.entellitrak.ReferenceObjectEventContext;
import com.entellitrak.dynamic.SciUserProfile;
import com.entellitrak.legacy.util.StringUtility;
import com.entellitrak.tracking.ReferenceObjectEventHandler;

import net.micropact.etkgit.common.util.JGitUtilities;
import net.micropact.etkgit.common.util.SCIUtilities;

public class SciUserProfileRoe implements ReferenceObjectEventHandler {

	@Override
	public void execute(final ReferenceObjectEventContext etk) throws ApplicationException {
		final var currentEventType = etk.getDataEventType();
		final var newObject = (SciUserProfile) etk.getNewObject();
		final var currentUserId = etk.getCurrentUser().getId();

		if (DataEventType.READ == currentEventType && !currentUserId.equals(newObject.getUser())) {
			throw new ApplicationException("Cannot view SCI User Profile of another user.");
		}

		try {
			validate(etk);
		} catch (final InputValidationException e) {
			throw new ApplicationException(e);
		}
	}

	private void validate(final ReferenceObjectEventContext etk) throws ApplicationException, InputValidationException {
		final var currentEventType = etk.getDataEventType();
		final var newObject = (SciUserProfile) etk.getNewObject();


		if (DataEventType.CREATE == currentEventType || DataEventType.UPDATE == currentEventType) {

			final var gitUrl = SCIUtilities.getRepositoryUrl(etk);

			if (gitUrl.startsWith("https")) {
				if (StringUtility.isBlank(newObject.getGitAccessToken()) || StringUtility.isBlank(newObject.getGitUserName())) {
					etk.getResult().addMessage("The SCI Source Control Settings repositoryUrl is configured using HTTPS so you must enter both Git User Name and Git Access Token");
					etk.getResult().cancelTransaction();
				} else {
					//UI does not permit updating of access token
					if (DataEventType.CREATE == currentEventType) {
						final var token = JGitUtilities.encrypt(newObject.getGitAccessToken());
						newObject.setGitAccessToken(token);
						etk.getDynamicObjectService().createSaveOperation(newObject).setExecuteEvents(false).save();
					}
				}
			}
		}
	}
}