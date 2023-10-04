package net.entellitrak.aea.setup.core;

import java.util.List;

import com.entellitrak.ExecutionContext;

/**
 * Simple implementation of {@link ISetupHandlerResponseBuilder}
 *
 * @author Zachary.Miller
 */
class SetupHandlerResponseBuilder implements ISetupHandlerResponseBuilder {

	private final ExecutionContext etk;
	private final List<String> messages;

	public SetupHandlerResponseBuilder(final ExecutionContext executionContext, final List<String> theMessages) {
		etk = executionContext;
		messages = theMessages;
	}

	@Override
	public ISetupHandlerResponseBuilder setMessages(final List<String> theMessages) {
		return new SetupHandlerResponseBuilder(etk, theMessages);
	}

	@Override
	public ISetupHandlerResponse build() {
		return new SetupHandlerResponse(etk, messages);
	}
}
