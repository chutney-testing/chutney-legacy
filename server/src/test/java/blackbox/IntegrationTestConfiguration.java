package blackbox;

import blackbox.restclient.RestClient;
import blackbox.stepdef.TestContext;
import blackbox.util.LaxRedirectStrategy;
import com.google.common.io.Resources;
import com.chutneytesting.ServerConfiguration;
import com.chutneytesting.design.infra.storage.scenario.git.GitClient;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
@Import({ServerConfiguration.class, DBConfiguration.class})
public class IntegrationTestConfiguration {

    @Bean
    TestContext scenarioContext() {
        return new TestContext();
    }

    @Bean
    RestClient secureRestClient(@Value("${server.port}") int port, SSLContext SSLContext) {
        return new RestClient(restTemplate(SSLContext).rootUri("https://localhost:" + port).build());
    }

    @Bean
    RestClient restClient(@Value("${server.http.port}") int port, SSLContext SSLContext) {
        return new RestClient(restTemplate(SSLContext).rootUri("http://localhost:" + port).build());
    }

    private RestTemplateBuilder restTemplate(SSLContext SSLContext) {
        CloseableHttpClient httpClient = HttpClients.custom()
            .setRedirectStrategy(new LaxRedirectStrategy())
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .setSSLContext(SSLContext)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplateBuilder().requestFactory(() -> requestFactory);
    }

    @Bean
    @Primary
    GitClient gitClientMocked() {
        return Mockito.mock(GitClient.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    SSLContext sslContext() throws Exception {
        SSLContextBuilder sslCtxBuilder = new SSLContextBuilder();

        KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
        trustMaterial.load(Resources.getResource("blackbox/security/truststore.jks").openStream(), "truststore".toCharArray());
        sslCtxBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());

        return sslCtxBuilder.build();
    }
}
