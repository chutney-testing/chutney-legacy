package com.chutneytesting.task.ssh.scp;

import java.io.IOException;

public interface ScpClient {

    void upload(String local, String remote) throws IOException;

    void download(String remote, String local) throws IOException;

    void close() throws IOException;

}
