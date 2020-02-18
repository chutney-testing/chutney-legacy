package com.chutneytesting.task.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;

public class HttpsListenerTask implements Task {

    private static final int DEFAULT_MESSAGE_COUNT = 1;

    private final Logger logger;

    private WireMockServer httpsServer;

    private final String uri;

    private final String verb;

    private final int expectedMessageCount;

    public HttpsListenerTask(Logger logger,
                             @Input("https-server") WireMockServer httpsServer,
                             @Input("uri") String uri,
                             @Input("verb") String verb,
                             @Input("expected-message-count") String expectedMessageCount) {
        this.logger = logger;
        this.httpsServer = httpsServer;
        this.uri = uri;
        this.verb = verb;
        this.expectedMessageCount = NumberUtils.toInt(expectedMessageCount, DEFAULT_MESSAGE_COUNT);
    }

    @Override
    public TaskExecutionResult execute() {
        FindRequestsResult result = httpsServer.findRequestsMatching(
            RequestPatternBuilder.newRequestPattern(RequestMethod.fromString(verb), WireMock.urlMatching(uri)).build());

        int requestReceived = result.getRequests().size();
        if (requestReceived < expectedMessageCount) {
            logger.error("Expected " + expectedMessageCount + " message(s). Receive only " + requestReceived + " message(s)");
            return TaskExecutionResult.ko();
        }

        httpsServer.resetRequests();
        return TaskExecutionResult.ok(toOutputs(result.getRequests()));
    }

    private static Map<String, Object> toOutputs(List<LoggedRequest> request) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("requests", request);
        return outputs;
    }

}
