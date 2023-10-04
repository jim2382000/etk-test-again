package net.entellitrak.aea.setup.core;

import java.util.Collection;

import com.entellitrak.configuration.ServiceBundle;

/**
 * Interface to be implemented by individual bundles which contains
 * information necessary to run their setup code.
 *
 * @author Zachary.Miller
 */
public interface ISetupHandler {

	/**
	 * Get the service bundles which must have their setup handlers
	 * run before this bundle's setup handler can run.
	 *
	 * @param setupHandlerRequest the setup handler request
	 * @return the dependencies
	 */
	Collection<ServiceBundle> getDependencies(ISetupHandlerRequest setupHandlerRequest);

	/**
	 * Run the setup code for this bundle.
	 *
	 * @param setupHandlerRequest the setup handler request
	 * @return a response
	 */
	ISetupHandlerResponse executeSetup(ISetupHandlerRequest setupHandlerRequest);
}
