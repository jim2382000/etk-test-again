/**
 *
 * TransportConfigCallbackImpl
 *
 * administrator 05/22/2020
 **/

package net.micropact.etkgit.common.util;

import java.nio.file.Path;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TransportConfigCallbackImpl implements TransportConfigCallback {
	private Path userKeyLocation;
	private CredentialsProvider credentialsProvider;
	private SshSessionFactory sshSessionFactory;

	public TransportConfigCallbackImpl(final CredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
	}

	public TransportConfigCallbackImpl(final Path keyLocation) {
		this.userKeyLocation = keyLocation;
		this.sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(final OpenSshConfig.Host hc, final Session session) {
				session.setConfig("StrictHostKeyChecking", "no");
			}

			@Override
			protected JSch createDefaultJSch(final FS fs) throws JSchException {
				final var jSch = super.createDefaultJSch(fs);
				JSch.setConfig("StrictHostKeyChecking", "no");
				jSch.addIdentity(userKeyLocation.toString());

				return jSch;
			}

			@Override
			public String getType() {
				return "EtkGitBundleJschConfigSessionFactory";
			}
		};
	}

	@Override
	public void configure(final Transport transport) {
		if (SshTransport.class.isAssignableFrom(transport.getClass())) {
			final var sshTransport = (SshTransport) transport;
			sshTransport.setSshSessionFactory(sshSessionFactory);
		} else if (HttpTransport.class.isAssignableFrom(transport.getClass())) {
			final var httpTransport = (HttpTransport) transport;
			httpTransport.setCredentialsProvider(credentialsProvider);
		}
	}
}