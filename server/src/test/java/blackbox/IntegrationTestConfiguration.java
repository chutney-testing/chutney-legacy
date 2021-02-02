package blackbox;

import com.chutneytesting.ServerConfiguration;
import com.chutneytesting.design.infra.storage.scenario.git.GitClient;
import com.google.common.io.Resources;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
@Import({ServerConfiguration.class, DBConfiguration.class})
public class IntegrationTestConfiguration {

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
