package net.entellitrak.aea.setup.core;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupHandlerRequest}.
 *
 * @author Zachary.Miller
 */
class SetupHandlerRequest implements ISetupHandlerRequest {

	private final ExecutionContext etk;

	public SetupHandlerRequest(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return etk;
	}
}
