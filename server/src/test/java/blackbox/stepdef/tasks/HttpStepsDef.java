package blackbox.stepdef.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import blackbox.stepdef.TestContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;

public class HttpStepsDef {

    private TestContext testContext;

    public HttpStepsDef(TestContext testContext) {
        this.testContext = testContext;
    }

    @After
    public void stopWiremockServer() {
        WireMockServer server = testContext.getMockServer();
        if (server != null) {
            server.stop();
        }
    }

    @Given("^an app providing an http endpoint such as (.+) on uri (.+)$")
    public void an_SSHD_server_is_started(String verb, String uri) {
        WireMockConfiguration wireMockConfiguration = wireMockConfig()
            .dynamicPort()
            .containerThreads(7)
            .asynchronousResponseThreads(1)
            .jettyAcceptors(1);
        WireMockServer wireMockServer = new WireMockServer(wireMockConfiguration);
        testContext.putMockServer(wireMockServer);
        wireMockServer.start();

        wireMockServer.stubFor(request(verb, urlEqualTo(uri))
            .willReturn(aResponse().withStatus(200).withBody("I was here"))
        );
    }
}
