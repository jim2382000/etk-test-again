package net.micropact.etkgit.common.rdo.scisuserprofile.roe;

import java.util.Arrays;

import com.entellitrak.ApplicationException;
import com.entellitrak.DataEventType;
import com.entellitrak.ReferenceObjectEventContext;
import com.entellitrak.dynamic.SciSourceControlSettings;
import com.entellitrak.legacy.util.StringUtility;
import com.entellitrak.tracking.ReferenceObjectEventHandler;

public class SciSourceControlSettingsRoe implements ReferenceObjectEventHandler {
	private ReferenceObjectEventContext etk;

	@Override
	public void execute(final ReferenceObjectEventContext executionContext) throws ApplicationException {
		etk = executionContext;

		if (Arrays.asList(DataEventType.CREATE, DataEventType.UPDATE).contains(etk.getDataEventType())) {
			validate();
		}
	}

	private void validate() {
		final var newObject = (SciSourceControlSettings) etk.getNewObject();

		if ("repositoryUrl".equals(newObject.getCode()) &&
				(StringUtility.isBlank(newObject.getValue()) || (!newObject.getValue().startsWith("https://")))) {
			etk.getResult().addMessage("The repositoryUrl cannot be blank and should start using the 'Clone HTTPS' string, like https://...");
			etk.getResult().cancelTransaction();
		}
	}
}