/**
 *
 * SciCheckInEventHandler
 *
 * administrator 05/22/2020
 **/

package net.micropact.etkgit.common.event;

import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.system.CheckInEventHandler;
import com.entellitrak.system.CheckInExecutionContext;
import com.micropact.entellitrak.config.SpringGlobalContext;
import com.micropact.entellitrak.scripting.exception.CompilationException;
import com.micropact.entellitrak.web.RequestContextHolder;
import com.micropact.entellitrak.workspace.service.WorkspaceService;

import net.micropact.etkgit.common.util.SCIUtilities;

public class SciCheckInEventHandler implements CheckInEventHandler {
    @Override
    public void execute(final CheckInExecutionContext etk) throws ApplicationException {

    	if (SCIUtilities.getRepositoryUrl(etk) == null) {
    		etk.getLogger().warn("Skipping SciCheckInEventHandler because there is not a repository configured in SciSourceControlSettings");
    		return;
    	}

    	compilationFailCheckWorkaround();

    	final var gitHandler = new JGitCheckInEventHandler(etk);
		gitHandler.execute();
    }

    private void compilationFailCheckWorkaround() throws ApplicationException {
    	try {
    		final var userContainer = RequestContextHolder.getUserContainer();
    		final var ws = SpringGlobalContext.getBean(WorkspaceService.class);

    		ws.compileUserWorkspace(userContainer);
    	} catch (final CompilationException e) {
    		final String msg = e.getCompilationErrors()
    				.stream()
    				.map(m -> "Check-in has failed because of compilation error(s)"
    				.concat(System.lineSeparator())
    				.concat(String.format("%s:%s - %s", m.getClassName(), m.getLineNumber(), m.getError())))
    				.collect(Collectors.joining(System.lineSeparator()));
    		throw new ApplicationException(msg);
    	}
    }
}