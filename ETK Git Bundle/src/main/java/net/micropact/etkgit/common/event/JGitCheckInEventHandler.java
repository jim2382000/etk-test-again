package net.micropact.etkgit.common.event;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import com.entellitrak.ApplicationException;
import com.entellitrak.configuration.CheckInOperation;
import com.entellitrak.configuration.ScriptOperation;
import com.entellitrak.logging.Logger;
import com.entellitrak.system.CheckInExecutionContext;
import com.entellitrak.user.User;

import net.micropact.etkgit.common.util.JGitUtilities;
import net.micropact.etkgit.common.util.ScriptUtilities;

//Helpful documentation:
// https://download.eclipse.org/jgit/site/5.6.0.201912101111-r/apidocs/index.html
// https://wiki.eclipse.org/JGit/User_Guide
// https://www.vogella.com/tutorials/JGit/article.html

public class JGitCheckInEventHandler {
    private static final String MESSAGE = "Workspace %s checked-in by user, %s.";

	private final CheckInExecutionContext etk;
	private final Logger logger;
	private final int revisionNumber;
	private final Collection<ScriptOperation> scriptChanges;
	private final JGitUtilities jgitUtilities;
	private final Path srcDirectory;
	private final User user;

	public JGitCheckInEventHandler(final CheckInExecutionContext executionContext) throws ApplicationException {
		etk = executionContext;
		user = etk.getCurrentUser();
		logger = etk.getLogger();
		revisionNumber = etk.getRevisionNumber();
		scriptChanges = etk.getScriptOperations();
		jgitUtilities = new JGitUtilities(etk);

		//Perform a pull to make sure a pre-existing repository is up-to-date.
		logger.debug("Getting local Git...");
		try (var git = jgitUtilities.getLocalGit()) {
			logger.debug("Getting local Git...complete");

			srcDirectory = FileSystems.getDefault().getPath(git.getRepository().getDirectory().getParent());

			git.pull()
				.setTransportConfigCallback(jgitUtilities.getTransportConfigCallback())
				.call();
		} catch (final GitAPIException e) {
			throw new ApplicationException("Error performing git pull to get repository up-to-date.", e);
		}
	}

	/**
	 * Checkin script changes.
	 *
	 * TODO: Fix issue when changing bundles for a given script. Currently, the files are not removed from original location in Git repository.
	 *
	 * @throws ApplicationException
	 */
	public void execute() throws ApplicationException {

		logger.debug("Using repository: " + srcDirectory);

		try (var git = jgitUtilities.getLocalGit()) {

			for (final var scriptOperation : scriptChanges) {
				final var operation = scriptOperation.getCheckInOperation();
				final var fullScriptPath = recordScriptOperation(scriptOperation);

				var strPath = fullScriptPath.toString();
				if (strPath.contains("C:\\")) {
					strPath = strPath.replace("\\", "\\\\");
				}

				var strSrcDirectory = srcDirectory.toString();
				if (strSrcDirectory.contains("C:\\")) {
					strSrcDirectory = strSrcDirectory.replace("\\", "\\\\");
				}
				final var scriptPath = strPath.replace(strSrcDirectory, "").replace("\\\\", "/").replaceFirst("/", "");

				logger.error(String.format("Adding file pattern for operation %s and file %s", operation, scriptPath));

				if (CheckInOperation.DELETE.equals(operation)) {
					git.rm().addFilepattern(scriptPath).call();
				} else {
					git.add().addFilepattern(scriptPath).call();
				}
			}

			git.commit()
				.setAuthor(user.getAccountName(), user.getProfile().getEmailAddress())
				.setCommitter(user.getAccountName(), user.getProfile().getEmailAddress())
				.setMessage(String.format(MESSAGE, revisionNumber, user.getAccountName())).call();

			final Iterable<PushResult> results = git.push()
					.setTransportConfigCallback(jgitUtilities.getTransportConfigCallback()).setRemote("origin")
					.setPushAll().call();

			final PushResult result = results.iterator().next();
			final List<RemoteRefUpdate> failedUpdates = result.getRemoteUpdates().stream()
					.filter(remoteUpdate -> !RemoteRefUpdate.Status.OK.equals(remoteUpdate.getStatus()))
					.collect(Collectors.toList());

			if (!failedUpdates.isEmpty()) {
				failedUpdates.forEach(
						remoteUpdate -> logger.error(String.format("Error updating remote %s. Message is: %s",
								remoteUpdate.getRemoteName(), remoteUpdate.getMessage())));

				logger.error(String.format("PushResult messages: %s", result.getMessages()));

				throw new ApplicationException(
						"One or more remotes failed to update, so push failed. See logs for more details.");
			}

			logger.debug("Git checkin finished!");
		} catch (final IOException | GitAPIException e) {
			throw new ApplicationException(e);
		}
	}

	private Path recordScriptOperation(final ScriptOperation op) throws IOException, ApplicationException {
		final var operation = op.getCheckInOperation();
		final var script = op.getScript();
		final var scriptPath = ScriptUtilities.getScriptObjectFullPath(etk, srcDirectory, script);

		if (CheckInOperation.DELETE.equals(operation)) {
			etk.getLogger().debug(String.format("Attempting to delete file from path: %s", scriptPath));
			final boolean deleted = ScriptUtilities.deleteScriptFromDisk(scriptPath);
			logger.debug(String.format("File deleted: %s", deleted));
		} else {
			logger.debug(String.format("Attempting to write file to path: %s", scriptPath));
			ScriptUtilities.writeScriptToDisk(etk, script, scriptPath);
		}
		return scriptPath;
	}

}