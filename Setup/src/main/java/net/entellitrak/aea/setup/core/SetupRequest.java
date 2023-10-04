package net.entellitrak.aea.setup.core;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupRequest}.
 *
 * @author Zachary.Miller
 */
class SetupRequest implements ISetupRequest {

	private final ExecutionContext etk;

	public SetupRequest(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return etk;
	}
}
