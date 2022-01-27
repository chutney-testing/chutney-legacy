package com.chutneytesting.task.ssh.sftp;

import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.SshClientFactory;
import java.io.IOException;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory;

public class SftpClientImpl implements ChutneySftpClient {

    private final ClientSession session;
    private final SftpClient sftpClient;

    private SftpClientImpl(ClientSession session, SftpClient sftpClient) {
        this.session = session;
        this.sftpClient = sftpClient;
    }

    @Override
    public void send(String local, String remote) throws IOException {
        // todo
    }

    @Override
    public void receive(String remote, String local) throws IOException {
        // todo
    }

    @Override
    public List<String> listDirectory(String directory) throws IOException {
        SftpClient.Handle handle = sftpClient.openDir(directory);
        Iterable<SftpClient.DirEntry> files = sftpClient.listDir(handle);

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(files.iterator(), Spliterator.ORDERED), false)
            .map(SftpClient.DirEntry::getFilename)
            .filter(f -> !".".equals(f) && !"..".equals(f))
            .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        sftpClient.close();
        session.close();
    }

    public static ChutneySftpClient buildFor(Target target, long timeout, Logger logger) throws IOException {
        ClientSession session = SshClientFactory.buildSSHClientSession(target, timeout);
        return new SftpClientImpl(session, buildSftpClient(session, logger));
    }

    private static SftpClient buildSftpClient(ClientSession session, Logger logger) throws IOException {
        SftpClientFactory factory = DefaultSftpClientFactory.INSTANCE;
        SftpClient client = factory.createSftpClient(session, new TaskSftpErrorDataHandler(logger));
        return client.singleSessionInstance();
    }

    /*
     * According to SFTP version 4 - section 3.1 the server MAY send error data through the STDERR pipeline.
     * By default, the code ignores such data.
     * However, users may register a SftpErrorDataHandler that will be invoked whenever such data is received from the server.
     * */
    private static class TaskSftpErrorDataHandler implements SftpErrorDataHandler {

        private final Logger logger;

        public TaskSftpErrorDataHandler(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void errorData(byte[] buf, int start, int len) {
            logger.error("TODO");
        }

    }
}
