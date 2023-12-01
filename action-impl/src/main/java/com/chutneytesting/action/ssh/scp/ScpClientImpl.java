/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.ssh.scp;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.ssh.SshClientFactory;
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
