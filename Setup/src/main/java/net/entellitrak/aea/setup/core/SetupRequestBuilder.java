package net.entellitrak.aea.setup.core;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupRequestBuilder}.
 *
 * @author Zachary.Miller
 */
class SetupRequestBuilder implements ISetupRequestBuilder {

	private final ExecutionContext etk;

	public SetupRequestBuilder(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public ISetupRequest build() {
		return new SetupRequest(etk);
	}
}
