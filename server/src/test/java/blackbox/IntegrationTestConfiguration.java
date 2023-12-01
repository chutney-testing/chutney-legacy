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
@Import({ServerConfiguration.class})
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
