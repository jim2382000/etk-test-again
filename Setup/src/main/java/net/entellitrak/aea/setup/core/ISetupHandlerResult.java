package net.entellitrak.aea.setup.core;

import com.entellitrak.configuration.ServiceBundle;

/**
 * Interface representing results of a particular setup handler run by {@link ISetupService#setup(ISetupRequest)}.
 *
 * @author Zachary.Miller
 */
public interface ISetupHandlerResult {

	/**
	 * Get the service bundle the setup handler belongs to.
	 *
	 * @return the service bundle
	 */
	ServiceBundle getServiceBundle();

	/**
	 * Get the class which defines the setup handler.
	 *
	 * @return the class
	 */
	Class<? extends ISetupHandler> getSetupHandlerClass();

	/**
	 * Get the return value of the {@link ISetupHandler#executeSetup(ISetupHandlerRequest)}.
	 *
	 * @return the setup handler response
	 */
	ISetupHandlerResponse getSetupHandlerResponse();
}