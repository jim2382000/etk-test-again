package net.entellitrak.aea.setup.core;

import java.util.List;

/**
 * Builder for {@link ISetupHandlerResponse}.
 *
 * @author Zachary.Miller
 * @see ISetupHandlerResponse#builder(com.entellitrak.ExecutionContext) to get an instance.
 */
public interface ISetupHandlerResponseBuilder {

	/**
	 * Builder the {@link ISetupHandlerResponse}.
	 *
	 * @return the setup handler response
	 */
	public ISetupHandlerResponse build();

	/**
	 * Set the messages for the builder.
	 *
	 * @param messages the messages
	 * @return a builder
	 */
	public ISetupHandlerResponseBuilder setMessages(List<String> messages);
}
