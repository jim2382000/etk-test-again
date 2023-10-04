package net.micropact.aea.core.setup;

import java.util.Collection;
import java.util.List;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.ServiceBundle;

import net.entellitrak.aea.setup.core.ISetupHandler;
import net.entellitrak.aea.setup.core.ISetupHandlerRequest;
import net.entellitrak.aea.setup.core.ISetupHandlerResponse;

public class AeaUtilitySetupHandler implements ISetupHandler {

	@Override
	public Collection<ServiceBundle> getDependencies(final ISetupHandlerRequest setupHandlerRequest) {
		return List.of();
	}

	@Override
	public ISetupHandlerResponse executeSetup(final ISetupHandlerRequest setupHandlerRequest) {
		final ExecutionContext etk = setupHandlerRequest.getExecutionContext();
		final List<String> messages = AeaUtilitySetupService.setupAeaUtility(etk);

		return ISetupHandlerResponse.builder(etk)
				.setMessages(messages)
				.build();
	}
}
