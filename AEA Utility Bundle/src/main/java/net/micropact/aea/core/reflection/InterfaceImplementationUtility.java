package net.micropact.aea.core.reflection;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;

/**
 * This class contains utility functionality for determining what Script Objects implement a particular interface.
 *
 * @author zachary.miller
 */
public final class InterfaceImplementationUtility {

	/**
	 * Utility classes do not need constructors.
	 */
	private InterfaceImplementationUtility(){}

	/**
	 * This method finds all Classes which are defined as top-level types in their Script Object files and implement a
	 * particular interface.
	 *
	 * @param <I> the interface
	 * @param etk entellitrak execution context
	 * @param theInterface the Interface which the classes must implement
	 * @return The list of classes which implement the interface
	 */
	public static <I> List<Class<? extends I>> getInterfaceImplementations(final ExecutionContext etk, final Class<I> theInterface){
		final WorkspaceService workspaceService = etk.getWorkspaceService();
		final Workspace workspace = workspaceService.getSystemWorkspace();

		final List<Class<? extends I>> implementations = new ArrayList<>();

		for(final Script script : workspaceService.getScriptsByLanguageType(workspace, LanguageType.JAVA)){
			final String name = script.getFullyQualifiedName();
			try {
				final Class<?> currentClass = Class.forName(name);

				if(doesClassImplementInterface(currentClass, theInterface)){
					@SuppressWarnings("unchecked")
					final Class<I> currentClassI = (Class<I>) currentClass;
					implementations.add(currentClassI);
				}
			} catch (final ClassNotFoundException e) {
				etk.getLogger().error(
						String.format("ClassUtility could not determine whether class \"%s\" implements interface \"%s\"",
								name,
								theInterface.getName()),
						e);
			}
		}
		return implementations;
	}

	/**
	 * This method determines whether a class implements a particular interface.
	 *
	 * @param theClass the class which might implement the interface
	 * @param theInterface the interface which must be implemented
	 * @return whether the class implements the interface
	 */
	public static boolean doesClassImplementInterface(final Class<?> theClass, final Class<?> theInterface) {
		final int modifiers = theClass.getModifiers();

		return theInterface.isAssignableFrom(theClass)
				&& !theClass.isInterface()
				&& !Modifier.isAbstract(modifiers)
				&& Modifier.isPublic(modifiers);
	}
}
