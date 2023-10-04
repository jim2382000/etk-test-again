/**
 *
 * ScriptUtilities
 *
 * administrator 05/26/2020
 **/

package net.micropact.etkgit.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.ServiceBundle;
import com.entellitrak.configuration.Workspace;

public class ScriptUtilities {
	/**
	 * Private constructor to hide.
	 */
	private ScriptUtilities() {

	}

	public static Path getScriptObjectFullPath(final ExecutionContext etk, final Path startingDirectory, final Script script) throws ApplicationException {
		//Check-in event fires after database changes have been made, so Service Bundle may be null for a deleted script.
		ServiceBundle serviceBundle;

		try {
			serviceBundle = getScriptServiceBundle(etk, script);
		} catch (final NullPointerException e) {
			etk.getLogger().error(String.format("NullPointerException caught when looking up Service Bundle for script %s. Will try scanning local repository for location.",
					script.getFullyQualifiedName()), e);
			serviceBundle = null;
		}
		final String fullyQualifiedScript = getFullyQualifiedScriptPath(script);

		if (serviceBundle != null) {
			final var scriptPathOnDisk = startingDirectory.resolve(serviceBundle.getName())
					.resolve(fullyQualifiedScript);

			//comment

			//If it's already on disk in another bundle, remove it.
			etk.getServiceBundleService().getServiceBundles().stream()
				.map(bundle -> startingDirectory.resolve(bundle.getName()).resolve(fullyQualifiedScript))
				.filter(fileToFind -> Files.exists(fileToFind) && !scriptPathOnDisk.equals(fileToFind))
				.forEach(fileToFind -> {
					try {
						etk.getLogger().debug(String.format("Script appears to be moved. Deleting script from its old bundle at %s",
								fileToFind));
						Files.deleteIfExists(fileToFind);
					} catch (final IOException e) {
						etk.getLogger().error(String.format("Unable to delete script from %s. Possible duplication issue in repository",
								fileToFind));
					}
				});

			return scriptPathOnDisk;
		} else {
			//Couldn't find the script bundle in database; likely dealing with a delete, so scan the disk and see if we can find it.
			etk.getLogger().debug("Couldn't find the script bundle in database; likely dealing with a delete, so scan the disk and see if we can find it in directory: " + startingDirectory);
			final String ownerBundleName = etk.getServiceBundleService().getServiceBundles().stream().filter(bundle -> {

				final Path fileToFind = startingDirectory.resolve(bundle.getName())
						.resolve(fullyQualifiedScript);

				return Files.exists(fileToFind);
			})
			.map(ServiceBundle::getName)
			.findFirst().orElseThrow(() -> new NullPointerException(String.format("Problem encountered finding path for script %s", script.getName())));

			return startingDirectory.resolve(ownerBundleName).resolve(fullyQualifiedScript);
		}
	}

	private static ServiceBundle getScriptServiceBundle(final ExecutionContext etk, final Script script) {
		try {
			return etk.getServiceBundleService().getServiceBundle(script);
		} catch (final NullPointerException e) {
			try {
				final var bundleKey = etk.createSQL("select distinct eb.BUSINESS_KEY from ETK_BUNDLE_SCRIPT_OBJECT ebso join ETK_BUNDLE eb on ebso.BUNDLE_ID = eb.BUNDLE_ID where ebso.BUSINESS_KEY = :businessKey")
						.setParameter("businessKey", script.getBusinessKey())
						.fetchString();

				return etk.getServiceBundleService().getServiceBundleByBusinessKey(bundleKey);
			} catch (final IncorrectResultSizeDataAccessException e1) {
				throw new NullPointerException("Couldn't find bundleKey for script with business key: " + script.getBusinessKey());
			}
		}
	}

	public static String getFullyQualifiedScriptPath(final Script script) throws ApplicationException {
		String scriptPath;
		if (LanguageType.JAVA.equals(script.getLanguageType())) {
			scriptPath = "src/main/java/";
		} else {
			scriptPath = "src/main/static/";
		}
		return scriptPath + script.getFullyQualifiedName().replace('.', '/') + getScriptExtension(script);
	}

	public static String getScriptExtension(final Script script) throws ApplicationException {
		switch (script.getLanguageType()) {
			case BEANSHELL:
				return ".bsh";
			case CSS:
				return ".css";
			case HTML:
				return ".html";
			case JAVA:
				return ".java";
			case JAVASCRIPT:
				return ".js";
			case SQL:
				return ".sql";
			default:
				throw new ApplicationException("Unsupported LanguageType detected: " + script.getLanguageType());
		}
	}

	public static void writeScriptToDisk(final ExecutionContext etk, final Script script, final Path scriptPath) throws IOException {
		writeScriptToDisk(etk, script, scriptPath, false);
	}

	public static void writeScriptToDisk(final ExecutionContext etk, final Script script, final Path scriptPath, final boolean forceSystem) throws IOException {
		Workspace workspace;
		if (forceSystem) {
			workspace = etk.getWorkspaceService().getSystemWorkspace();
		} else {
			workspace = etk.getWorkspaceService().getWorkspace(etk.getCurrentUser().getAccountName());
		}
		final String code = etk.getWorkspaceService().getCode(workspace, script);

		Files.createDirectories(scriptPath.getParent());
		Files.deleteIfExists(scriptPath);
		Files.writeString(scriptPath, code, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}

	public static boolean deleteScriptFromDisk(final Path scriptPath) throws IOException {
		return Files.deleteIfExists(scriptPath);
	}
}