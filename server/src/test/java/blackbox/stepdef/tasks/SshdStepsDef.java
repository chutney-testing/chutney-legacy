package blackbox.stepdef.tasks;

import blackbox.restclient.RestClient;
import com.chutneytesting.design.api.environment.dto.TargetMetadataDto;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.design.domain.environment.Target;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import org.apache.http.util.TextUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

public class SshdStepsDef {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshdStepsDef.class);

    private static final String DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM = "RSA";
    private final RestClient secureRestClient;
    private int serverPort;
    private SshServer sshd;
    private String serverHost;

    private String savedTargetName = "";
    ResponseEntity<String> responseEntity;

    public SshdStepsDef(RestClient secureRestClient) {
        this.secureRestClient = secureRestClient;
    }

    @After
    public void after() throws Exception {
        if (sshd != null) {
            LOGGER.info("Stopping SSHD server from '{}' on port '{}'", sshd.getHost(), sshd.getPort());
            sshd.stop();
        }

        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
            secureRestClient.defaultRequest()
                .withUrl("/api/v2/environment/GLOBAL/target/" + savedTargetName)
                .delete();
        }
    }

    @Given("^an SSHD server is started(?: on port (d+)|)?$")
    public void an_SSHD_server_is_started(Integer port) throws Throwable {
        String sshUsername = "test";
        String sshPassword = "test";
        serverHost = getHostIPAddress();
        serverPort = port != null ? port : getFreePort();

        sshd = SshServer.setUpDefaultServer();
        sshd.setHost(serverHost);
        sshd.setPort(serverPort);

        AbstractGeneratorHostKeyProvider hostKeyProvider = prepareKeyPairProvider();
        sshd.setKeyPairProvider(hostKeyProvider);
        sshd.setPasswordAuthenticator((username, password, session) -> sshUsername.equals(username) && sshPassword.equals(password));
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        sshd.setShellFactory(new ProcessShellFactory("/bin/sh", "-i", "-l"));
        sshd.setCommandFactory(ProcessShellCommandFactory.INSTANCE);
        sshd.start();
        LOGGER.info("Starting SSHD server from '{}' on port '{}'", serverHost, serverPort);
    }

    @Given("^Target (.*) containing SSHD connection information( with wrong password)?$")
    public void target_name_containing_SSHD_connection_information(String targetName, String withWrongPassword) {
        boolean wrongPassword = false;
        if (!TextUtils.isEmpty(withWrongPassword)) {
            wrongPassword = true;
        }
        Target target = Target.builder()
            .withId(Target.TargetId.of(targetName, "GLOBAL"))
            .withUrl("ssh://" + serverHost + ":" + serverPort)
            .withSecurity(
                SecurityInfo.builder()
                    .credential(SecurityInfo.Credential.of("test", wrongPassword ? "blabla" : "test"))
                    .build())
            .build();

        try {
            responseEntity = secureRestClient.defaultRequest()
                .withUrl("/api/v2/environment/GLOBAL/target")
                .withBody(TargetMetadataDto.from(target))
                .post(String.class);

            LOGGER.info("New target saved : " + target);
            savedTargetName = targetName;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException("Unable to save target [" + target.name + "]");
        }
    }

    private static String getHostIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private static int getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static AbstractGeneratorHostKeyProvider prepareKeyPairProvider() {

        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider();
        hostKeyProvider.setAlgorithm(DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM);
        File rsaKey;
        try {
            rsaKey = File.createTempFile("test", "ssh_key_pair");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        hostKeyProvider.setPath(rsaKey.toPath());
        return hostKeyProvider;
    }
}
