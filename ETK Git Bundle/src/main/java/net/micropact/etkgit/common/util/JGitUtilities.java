package net.micropact.etkgit.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.dynamic.SciUserProfile;
import com.entellitrak.user.User;

import net.micropact.etkgit.common.dao.SciUserProfileDao;

public final class JGitUtilities {
	private static final String LOCAL_GIT_REPO_RESOURCE = "gitRepository";
	private final ExecutionContext etk;
	private final User user;
	private final SciUserProfile userProfile;

	/**
	 * Creates a JGitUtilities for the current user.
	 * @throws ApplicationException Error loading user profile
	 */
	public JGitUtilities(final ExecutionContext executionContext) throws ApplicationException {
		etk = executionContext;
		user = etk.getCurrentUser();
		userProfile = SciUserProfileDao.getSciUserProfile(etk, user);
	}

	/**
	 * Open local Git repository or create new if one does not already exist.
	 *
	 * @return {@link Git} instance
	 * @throws ApplicationException
	 */
	public Git getLocalGit() throws ApplicationException {
		try {
			final String gitUrl = SCIUtilities.getRepositoryUrl(etk);
			final var entellitrakConfigDirectory = etk.getResourceService().getResource(".")
					.getParentFile().getParentFile().getParentFile();
			final var localGitRepositoryPath = Paths.get(entellitrakConfigDirectory.getAbsolutePath()).resolve(LOCAL_GIT_REPO_RESOURCE);
			final var localGitRepository = localGitRepositoryPath.toFile();

			if (!localGitRepository.exists() || localGitRepository.listFiles().length == 0) {
				return createNewRepository(gitUrl, localGitRepository);
			} else {
				return Git.open(localGitRepository);
			}
		} catch (final IOException e) {
			throw new ApplicationException("Error getting local git repository from app-deps", e);
		}
	}

	/**
	 * Clone Git repository.
	 *
	 * @param urlToClone
	 * @param directoryToUse
	 * @return
	 * @throws ApplicationException
	 */
	public Git createNewRepository(final String urlToClone, final File directoryToUse) throws ApplicationException {
		try {
			return Git.cloneRepository()
				.setURI(urlToClone)
				.setDirectory(directoryToUse)
				.setCloneAllBranches(true)
				.setTransportConfigCallback(getTransportConfigCallback())
				.call();
		} catch (final GitAPIException e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Get appropriate {@link TransportConfigCallback} based on configured SciSourceControlSettings repositoryUrl.
	 *
	 * Configure {@link UsernamePasswordCredentialsProvider} for "https" transport.
	 *
	 * @return
	 * @throws ApplicationException If configured SciSourceControlSettings repositortUrl is not "https".
	 */
	public void createCommitAndPush(final Git git, final String commitMessage) throws ApplicationException {
		try {
			git.add().addFilepattern(".").call();
			git.add().setUpdate(true).addFilepattern(".").call();

			final var status = git.status().call();
			if (status.hasUncommittedChanges()) {
				git.commit()
					.setAuthor(user.getAccountName(), user.getProfile().getEmailAddress())
					.setCommitter(user.getAccountName(), user.getProfile().getEmailAddress())
					.setMessage(commitMessage)
					.call();

				final Iterable<PushResult> results = git.push()
					.setTransportConfigCallback(getTransportConfigCallback())
					.setRemote("origin")
					.setPushAll()
					.call();

				final PushResult result = results.iterator().next();
				final List<RemoteRefUpdate> failedUpdates = result.getRemoteUpdates().stream()
						.filter(remoteUpdate -> !RemoteRefUpdate.Status.OK.equals(remoteUpdate.getStatus()))
						.collect(Collectors.toList());

				if (!failedUpdates.isEmpty()) {

					failedUpdates.forEach(remoteUpdate -> etk.getLogger().error(
							String.format("Error updating remote %s. Message is: %s",
									remoteUpdate.getRemoteName(),
									remoteUpdate.getMessage()
									)));

					etk.getLogger().error(String.format("PushResult messages: %s", result.getMessages()));

					throw new ApplicationException("One or more remotes failed to update, so push failed. See logs for more details.");
				}
			} else {
				throw new ApplicationException("No changes detected. Unable to create commit.");
			}
		} catch (final GitAPIException e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Get appropriate {@link TransportConfigCallback} based on configured SciSourceControlSettings repositoryUrl.
	 *
	 * If "https..." then configure {@link UsernamePasswordCredentialsProvider}.
	 *
	 * @return
	 * @throws ApplicationException If configured SciSourceControlSettings repositortUrl is not "https".
	 */
	public TransportConfigCallback getTransportConfigCallback() throws ApplicationException {
		final String gitUrl = SCIUtilities.getRepositoryUrl(etk);
		if (gitUrl.startsWith("https")) {
			try {
				final String gitToken = decrypt(userProfile.getGitAccessToken());
				return new TransportConfigCallbackImpl(new UsernamePasswordCredentialsProvider(userProfile.getGitUserName(), gitToken));
			}catch(final Exception e) {
				throw new ApplicationException("Problem decrypting SCI password", e);
			}
		} else {
			throw new ApplicationException("Don't know how to handle TransportConfigCallback for Git url:"+gitUrl);
		}
	}

	/**
	 * For encrypting user Git token
	 *
	 * @param message
	 * @return
	 * @throws ApplicationException
	 */
	public static String encrypt(final String message) throws ApplicationException {
		final byte[] encrypted = encryptDecrypt(message.getBytes(), Cipher.ENCRYPT_MODE);
		return Base64.getEncoder().encodeToString(encrypted);
	}

	/**
	 * For decrypting user Git token
	 * @param message
	 * @return
	 * @throws ApplicationException
	 */
	public static String decrypt(final String message) throws ApplicationException {
		final byte[] decrypted = encryptDecrypt(Base64.getDecoder().decode(message.getBytes()), Cipher.DECRYPT_MODE);
		return new String(decrypted);
	}

	//Externalize to system preference?
	private static final byte[] SECRET_KEY_MATERIAL = {
			(byte)0xc1, (byte)0xbe, (byte)0x32, (byte)0x7b,
			(byte)0x7c, (byte)0x0f, (byte)0x50, (byte)0x92,
			(byte)0xcb, (byte)0x57, (byte)0x1f, (byte)0x0f,
			(byte)0x12, (byte)0x7a, (byte)0x20, (byte)0x79
		};

	private static byte[] encryptDecrypt(final byte[] message, final int cipherMode)
			throws ApplicationException {
		try {
			final var cipher = Cipher.getInstance("AES/GCM/NoPadding");
	        final var gcmParameterSpec = new GCMParameterSpec(128, new byte[12]);
	        final var keySpec = new SecretKeySpec(SECRET_KEY_MATERIAL, "AES");
			cipher.init(cipherMode, keySpec, gcmParameterSpec);

			return cipher.doFinal(message);

		} catch (final Exception e) {
			throw new ApplicationException(e);
		}
	}
}