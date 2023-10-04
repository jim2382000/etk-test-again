package net.entellitrak.aea.setup.core;

/**
 * Builder for {@link ISetupRequest}.
 *
 * @author Zachary.Miller
 * @see ISetupRequest#builder(com.entellitrak.ExecutionContext) to get an instance
 */
public interface ISetupRequestBuilder {

	/**
	 * Get the {@link ISetupRequest} instance.
	 *
	 * @return the setup request
	 */
	ISetupRequest build();
}
