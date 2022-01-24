package com.chutneytesting.task.ssh.sftp;

import java.io.IOException;

public interface ChutneySftpClient extends AutoCloseable {

    void send(String local, String remote) throws IOException;

    void receive(String remote, String local) throws IOException;

    void listDirectory(String remote) throws IOException;

}
