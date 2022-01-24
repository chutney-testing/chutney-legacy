package com.chutneytesting.task.ssh.scp;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.SshClientFactory;
import java.io.IOException;
import java.util.Collections;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.CloseableScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;

public class ScpClientImpl implements ScpClient {

    private final ClientSession session;
    private final CloseableScpClient scpClient;

    private ScpClientImpl(ClientSession session, CloseableScpClient scpClient) {
        this.session = session;
        this.scpClient = scpClient;
    }

    @Override
    public void upload(String local, String remote) throws IOException {
        scpClient.upload(local, remote, Collections.emptyList());
    }

    @Override
    public void download(String remote, String local) throws IOException {
        scpClient.download(remote, local, Collections.emptyList());
    }

    @Override
    public void close() throws Exception {
        session.close();
        scpClient.close();
    }

    public static ScpClient buildFor(Target target, long timeout) throws IOException {
        ClientSession session = SshClientFactory.buildSSHClientSession(target, timeout);
        return new ScpClientImpl(session, closeableScpClient(session));
    }

    private static CloseableScpClient closeableScpClient(ClientSession session) {
        ScpClientCreator creator = ScpClientCreator.instance();
        return CloseableScpClient.singleSessionInstance(
            creator.createScpClient(session)
        );
    }

}
