/**
 * SciGitEndpoint
 *
 * @author administrator
 * Created on: 05/26/2020
 */

package net.micropact.etkgit.common.endpoint;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;

import com.entellitrak.ApplicationException;
import com.entellitrak.configuration.Script;
import com.entellitrak.endpoints.AbstractEndpoint;

import net.micropact.etkgit.common.util.JGitUtilities;
import net.micropact.etkgit.common.util.ScriptUtilities;

@Path("/SciGitEndpoint")
public class SciGitEndpoint extends AbstractEndpoint {
    @POST
    @Path("/rebuildLocalRepository")
    public Response rebuildLocalRepository() throws IOException, ApplicationException {
    	final var etk = getExecutionContext();
		final var entellitrakConfigDirectory = etk.getResourceService().getResource(".")
				.getParentFile().getParentFile().getParentFile();
		final var localGitRepositoryPath = Paths.get(entellitrakConfigDirectory.getAbsolutePath()).resolve("gitRepository");

		if (Files.exists(localGitRepositoryPath)) {
			FileUtils.deleteDirectory(localGitRepositoryPath.toFile());
		}

    	final var jgitUtilities = new JGitUtilities(etk);

    	try (final var git = jgitUtilities.getLocalGit()) {
    		etk.getLogger().debug(String.format("Repository re-cloned and in state %s",
    				git.getRepository().getRepositoryState()));
    	}

    	return Response.ok(Map.of("error", "", "detail", "Local repository successfully rebuilt.")).build();
    }

    @POST
    @Path("/syncSystemRepository")
    public Response syncSystemRepository() throws ApplicationException, IOException {
    	final var etk = getExecutionContext();
    	final var jgitUtilities = new JGitUtilities(etk);

    	try (final var git = jgitUtilities.getLocalGit()) {
    		final var srcDirectory = FileSystems.getDefault().getPath(git.getRepository().getDirectory().getParent());
    		final var systemWorkspace = etk.getWorkspaceService().getSystemWorkspace();
    		final var systemWorkspaceScripts = etk.getWorkspaceService().getScripts(systemWorkspace);

    		//Purge [bundle]/src/main/java and [bundle]/src/main/static directories if it exists.
    		etk.getServiceBundleService().getServiceBundles().stream().forEach(bundle -> {
    			final var javaSrcPath = srcDirectory.resolve(bundle.getName()).resolve("src/main/java");
    			final var staticSrcPath = srcDirectory.resolve(bundle.getName()).resolve("src/main/static");

    			if (Files.exists(javaSrcPath)) {
    				FileUtils.deleteQuietly(javaSrcPath.toFile());
    			}

    			if (Files.exists(staticSrcPath)) {
    				FileUtils.deleteQuietly(staticSrcPath.toFile());
    			}
    		});

    		//Write all the scripts to disk
    		for (final Script script : systemWorkspaceScripts) {
    			final var scriptPath = ScriptUtilities.getScriptObjectFullPath(etk, srcDirectory, script);

    			ScriptUtilities.writeScriptToDisk(etk, script, scriptPath, true);
    		}

    		//Add and push if needed
    		jgitUtilities.createCommitAndPush(git, String.format(
    				"Repository rebuilt from system by %s",
    				etk.getCurrentUser().getAccountName()));
    	}

    	etk.getLogger().debug("Repository successfully rebuilt and pushed to Git.");
    	return Response.ok(Map.of("error", "", "detail", "Local repository successfully pushed to Git.")).build();
    }
}