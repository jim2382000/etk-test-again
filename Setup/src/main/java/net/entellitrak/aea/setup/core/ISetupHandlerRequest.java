package net.entellitrak.aea.setup.core;

import com.entellitrak.ExecutionContext;

/**
 * Interface representing data which will be passed to {@link ISetupHandler}.
 *
 * @author Zachary.Miller
 */
public interface ISetupHandlerRequest {

	/**
	 * Get the entellitrak execution context.
	 *
	 * @return the execution context
	 */
	ExecutionContext getExecutionContext();
}
