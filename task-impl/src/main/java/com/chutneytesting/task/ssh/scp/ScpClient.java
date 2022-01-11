package com.chutneytesting.task.ssh.scp;

import java.io.IOException;

public interface ScpClient extends AutoCloseable {

    String DEFAULT_TIMEOUT = "5 s";

    void upload(String local, String remote) throws IOException;

    void download(String remote, String local) throws IOException;

}
