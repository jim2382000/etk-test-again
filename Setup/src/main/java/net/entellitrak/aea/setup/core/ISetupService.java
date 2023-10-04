package net.entellitrak.aea.setup.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.ServiceBundle;
import com.entellitrak.configuration.ServiceBundleService;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;

import net.entellitrak.aea.gl.api.etk.reflection.InterfaceImplementationFinder;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.setup.utility.topologicalsort.TopologicalSorter;

/**
 * Main entry point for dealing with the setup service.
 *
 * @author Zachary.Miller
 */
public interface ISetupService {

	/**
	 * Run the setup.
	 *
	 * @param setupRequest the request
	 * @return the response
	 */
	public static ISetupResponse setup(final ISetupRequest setupRequest){
		final ExecutionContext etk = setupRequest.getExecutionContext();

		final List<ISetupHandler> unsortedSetupHandlers = InterfaceImplementationFinder.getInterfaceImplementationsInActiveWorkspace(etk, ISetupHandler.class)
				.stream()
				.map(handlerClass -> {
					try {
						return handlerClass.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new GeneralRuntimeException(e);
					}
				})
				.collect(Collectors.toList());

		final List<ISetupHandler> sortedSetupHandlers = sortSetupHandlers(etk, new SetupHandlerRequest(etk), unsortedSetupHandlers);

		final ISetupHandlerRequest setupHandlerRequest = new SetupHandlerRequest(etk);

		final List<ISetupHandlerResult> setupHandlerResults = sortedSetupHandlers.stream()
				.map(setupHandler -> {
					final ISetupHandlerResponse setupHandlerResponse = setupHandler.executeSetup(setupHandlerRequest);
					return new SetupHandlerResult(getSetupHandlerServiceBundle(etk, setupHandler), setupHandler.getClass(), setupHandlerResponse);
				})
				.collect(Collectors.toList());

		return new SetupResponse(etk, setupHandlerResults);
	}

	private static ServiceBundle getSetupHandlerServiceBundle(final ExecutionContext etk, final ISetupHandler setupHandler) {
		final WorkspaceService workspaceService = etk.getWorkspaceService();
		final Workspace activeWorkspace = workspaceService.getActiveWorkspace();
		final ServiceBundleService serviceBundleService = etk.getServiceBundleService();

		final Script script = workspaceService.getScriptByFullyQualifiedName(activeWorkspace, setupHandler.getClass().getName());
		return serviceBundleService.getServiceBundle(script, activeWorkspace);
	}

	private static List<ISetupHandler> sortSetupHandlers(final ExecutionContext etk, final ISetupHandlerRequest setupHandlerRequest, final List<ISetupHandler> setupHandlers){
		/* We sort first by handler class to make the sorting deterministic to help with debugging if dependencies aren't specified correctly. */
		final List<ISetupHandler> partiallySortedSetupHandlers = setupHandlers.stream()
				.sorted(Comparator.comparing(setupHandler -> setupHandler.getClass().getName()))
				.collect(Collectors.toList());

		return topologicalSort(etk, setupHandlerRequest, partiallySortedSetupHandlers);
	}

	private static List<ISetupHandler> topologicalSort(final ExecutionContext etk, final ISetupHandlerRequest setupHandlerRequest, final List<ISetupHandler> setupHandlers) {
		return new TopologicalSorter<>((final ISetupHandler item, final ISetupHandler potentialDependency) -> {
			final Collection<ServiceBundle> actualDependencies = item.getDependencies(setupHandlerRequest);
			final Collection<String> actualDependenciesBusinessKeys = actualDependencies.stream()
					.map(ServiceBundle::getBusinessKey)
					.collect(Collectors.toSet());

			final ServiceBundle potentialDependencyServiceBundle = getSetupHandlerServiceBundle(etk, potentialDependency);

			return actualDependenciesBusinessKeys.contains(potentialDependencyServiceBundle.getBusinessKey());
		}).sortDependencies(setupHandlers);
	}
}
