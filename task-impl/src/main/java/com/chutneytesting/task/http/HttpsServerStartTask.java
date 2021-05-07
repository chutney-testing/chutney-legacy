package com.chutneytesting.task.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.math.NumberUtils;

public class HttpsServerStartTask implements Task {

    private static final int DEFAULT_HTTPS_PORT = 8443;

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final int port;
    private final String trustStorePath;
    private final String trustStorePassword;
    private final Optional<String> keyStorePath;
    private final Optional<String> keyStorePassword;

    public HttpsServerStartTask(Logger logger,
                                FinallyActionRegistry finallyActionRegistry,
                                @Input("port") String port,
                                @Input("truststore-path") String trustStorePath,
                                @Input("truststore-password") String trustStorePassword,
                                @Input("keystore-path") String keyStorePath,
                                @Input("keystore-password") String keyStorePassword) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.port = NumberUtils.toInt(port, DEFAULT_HTTPS_PORT);
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.keyStorePath = Optional.ofNullable(keyStorePath);
        this.keyStorePassword = Optional.ofNullable(keyStorePassword);
    }

    @Override
    public TaskExecutionResult execute() {
        WireMockConfiguration wireMockConfiguration = wireMockConfig()
            .dynamicPort()
            .httpsPort(port)
            .needClientAuth(true)
            .trustStorePath(trustStorePath)
            .trustStorePassword(trustStorePassword)
            .containerThreads(7)
            .asynchronousResponseThreads(1)
            .jettyAcceptors(1);
        // add keystore path and pwd if present
        keyStorePath.ifPresent(s -> wireMockConfiguration
            .keystorePath(s)
            .keystorePassword(keyStorePassword.orElse("")));
        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);
        logger.info("Try to start https server on port " + port);
        wireMockServer.start();

        wireMockServer.stubFor(any(anyUrl())
            .willReturn(aResponse().withStatus(200))
        );

        createQuitFinallyAction(wireMockServer);
        return TaskExecutionResult.ok(toOutputs(wireMockServer));
    }


    private Map<String, Object> toOutputs(WireMockServer httpsServer) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("httpsServer", httpsServer);
        return outputs;
    }

    private void createQuitFinallyAction(WireMockServer httpsServer) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("https-server-stop", HttpsServerStartTask.class.getSimpleName())
                .withInput("https-server", httpsServer)
                .build()
        );
        logger.info("HttpsServerStop finally action registered");
    }

}
