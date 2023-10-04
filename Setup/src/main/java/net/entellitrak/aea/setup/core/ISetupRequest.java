package net.entellitrak.aea.setup.core;

import com.entellitrak.ExecutionContext;

/**
 * Interface for defining inputs to {@link ISetupService#setup(ISetupRequest)}.
 *
 * @author Zachary.Miller
 */
public interface ISetupRequest {

	/**
	 * Create a convenient builder for this interface.
	 *
	 * @param executionContext the execution context
	 * @return the builder
	 */
	static ISetupRequestBuilder builder(final ExecutionContext executionContext) {
		return new SetupRequestBuilder(executionContext);
	}

	/**
	 * Get the execution context.
	 *
	 * @return the execution context
	 */
	ExecutionContext getExecutionContext();
}
