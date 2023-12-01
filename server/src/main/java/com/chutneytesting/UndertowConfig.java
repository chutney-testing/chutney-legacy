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

package com.chutneytesting;

import static com.chutneytesting.ServerConfigurationValues.SERVER_HTTP_INTERFACE_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SERVER_HTTP_PORT_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SERVER_PORT_SPRING_VALUE;

import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.TransportGuaranteeType;
import io.undertow.servlet.api.WebResourceCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"undertow-https-redirect"})
public class UndertowConfig {

    @Value(SERVER_PORT_SPRING_VALUE)
    private int securePort;

    @Value(SERVER_HTTP_PORT_SPRING_VALUE)
    private int httpPort;

    @Value(SERVER_HTTP_INTERFACE_SPRING_VALUE)
    private String httpInterface;

    @Bean
    public UndertowServletWebServerFactory servletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        // Add http listener
        factory.getBuilderCustomizers().add(builder -> builder.addHttpListener(httpPort, httpInterface));
        // Redirect rule to secure port
        factory.getDeploymentInfoCustomizers().add(deploymentInfo ->
            deploymentInfo.addSecurityConstraint(
                new SecurityConstraint()
                    .addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*"))
                    .setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
                    .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
                .setConfidentialPortManager(
                    exchange -> securePort
                ));
        return factory;
    }
}
