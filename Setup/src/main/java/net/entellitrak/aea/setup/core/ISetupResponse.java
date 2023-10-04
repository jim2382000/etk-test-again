package net.entellitrak.aea.setup.core;

import java.util.List;

/**
 * Interface representing the response of {@link ISetupService#setup(ISetupRequest)}.
 *
 * @author Zachary.Miller
 */
public interface ISetupResponse {

	/**
	 * Get a list of the setup results from individual setup handlers that were run.
	 *
	 * @return the setup handler results
	 */
	List<ISetupHandlerResult> getSetupHandlerResults();
}
