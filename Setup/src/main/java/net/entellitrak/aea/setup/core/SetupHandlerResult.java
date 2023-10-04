package net.entellitrak.aea.setup.core;

import com.entellitrak.configuration.ServiceBundle;

/**
 * Simple implementation of {@link ISetupHandlerResult}.
 *
 * @author Zachary.Miller
 */
class SetupHandlerResult implements ISetupHandlerResult {

	private final ServiceBundle serviceBundle;
	private final Class<? extends ISetupHandler> setupHandlerClass;
	private final ISetupHandlerResponse setupHandlerResponse;

	public SetupHandlerResult(final ServiceBundle theServiceBundle, final Class<? extends ISetupHandler> theSetupHandlerClass, final ISetupHandlerResponse theSetupHandlerResponse) {
		serviceBundle = theServiceBundle;
		setupHandlerClass = theSetupHandlerClass;
		setupHandlerResponse = theSetupHandlerResponse;
	}

	@Override
	public ServiceBundle getServiceBundle() {
		return serviceBundle;
	}

	@Override
	public Class<? extends ISetupHandler> getSetupHandlerClass() {
		return setupHandlerClass;
	}

	@Override
	public ISetupHandlerResponse getSetupHandlerResponse() {
		return setupHandlerResponse;
	}
}
