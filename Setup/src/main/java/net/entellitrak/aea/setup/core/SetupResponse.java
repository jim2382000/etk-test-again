package net.entellitrak.aea.setup.core;

import java.util.List;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupResponse}.
 *
 * @author Zachary.Miller
 */
class SetupResponse implements ISetupResponse {

	@SuppressWarnings("unused")
	private final ExecutionContext etk;
	private final List<ISetupHandlerResult> setupHandlerResults;

	public SetupResponse(final ExecutionContext executionContext, final List<ISetupHandlerResult> theSetupHandlerResults) {
		etk = executionContext;
		setupHandlerResults = theSetupHandlerResults;
	}

	@Override
	public List<ISetupHandlerResult> getSetupHandlerResults() {
		return setupHandlerResults;
	}
}
