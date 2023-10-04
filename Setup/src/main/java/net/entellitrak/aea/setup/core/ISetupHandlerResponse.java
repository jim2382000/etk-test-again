package net.entellitrak.aea.setup.core;

import java.util.Collections;
import java.util.List;

import com.entellitrak.ExecutionContext;

/**
 * Interface representing the return value of {@link ISetupHandler#executeSetup(ISetupHandlerRequest)}.
 *
 * @author Zachary.Miller
 */
public interface ISetupHandlerResponse {

	/**
	 * Get messages returned by the setup handler.
	 *
	 * @return the messages
	 */
	List<String> getMessages();

	/**
	 * Get a convenient builder for this interface.
	 *
	 * @param etk execution context
	 * @return a builder
	 */
	static ISetupHandlerResponseBuilder builder(final ExecutionContext etk) {
		return new SetupHandlerResponseBuilder(etk, Collections.emptyList());
	}
}
