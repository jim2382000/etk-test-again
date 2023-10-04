package net.entellitrak.aea.setup.core;

import java.util.List;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupHandlerResponse}.
 *
 * @author Zachary.Miller
 */
class SetupHandlerResponse implements ISetupHandlerResponse {

	@SuppressWarnings("unused")
	private final ExecutionContext etk;
	private final List<String> messages;

	public SetupHandlerResponse(final ExecutionContext executionContext, final List<String> theMessages) {
		etk = executionContext;
		messages = theMessages;
	}

	@Override
	public List<String> getMessages() {
		return messages;
	}
}
