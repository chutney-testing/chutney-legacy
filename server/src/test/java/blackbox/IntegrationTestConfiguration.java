package blackbox;

import com.chutneytesting.ServerConfiguration;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ServerConfiguration.class, DBConfiguration.class})
public class IntegrationTestConfiguration {


    @Bean
    SSLContext sslContext() throws Exception {
        SSLContextBuilder sslCtxBuilder = new SSLContextBuilder();

        KeyStore trustMaterial = KeyStore.getInstance(KeyStore.getDefaultType());
        trustMaterial.load(IntegrationTestConfiguration.class.getResource("/blackbox/keystores/truststore.jks").openStream(), "truststore".toCharArray());
        sslCtxBuilder.loadTrustMaterial(trustMaterial, new TrustSelfSignedStrategy());

        return sslCtxBuilder.build();
    }
}
