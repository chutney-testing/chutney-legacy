package com.chutneytesting.admin.infra.gitbackup;

import com.chutneytesting.admin.domain.gitbackup.GitClient;
import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GitClientImpl implements GitClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitClientImpl.class);

    @Override
    public boolean hasAccess(RemoteRepository remote) {
        boolean isAccessible = false;

        try {
            LsRemoteCommand lsRemoteCommand = Git.lsRemoteRepository()
                .setRemote(remote.url)
                .setTags(true)
                .setHeads(true)
                .setTimeout(5)
                .setTransportConfigCallback(getTransportConfigCallback(remote));

            Collection<Ref> remoteRefs = lsRemoteCommand.call();

            isAccessible = remoteRefs.stream().anyMatch(ref -> ref.getName().contains(remote.branch));
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }

        return isAccessible;
    }

    private TransportConfigCallback getTransportConfigCallback(RemoteRepository remote) {
        return transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    if (Files.exists(Paths.get(remote.privateKeyPath))) {
                        defaultJSch.addIdentity(remote.privateKeyPath, remote.privateKeyPassphrase);
                    }
                    return defaultJSch;
                }

            });
        };
    }
}
