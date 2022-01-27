package com.chutneytesting.task.ssh.sftp;

import java.io.IOException;
import java.util.List;

public interface ChutneySftpClient extends AutoCloseable {

    void send(String local, String remote) throws IOException;

    void receive(String remote, String local) throws IOException;

    List<String> listDirectory(String directory) throws IOException;

}
