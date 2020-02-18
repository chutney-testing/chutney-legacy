package com.chutneytesting;

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
@Profile({"http-ssl"})
public class UndertowConfig {

    @Value("${server.port}")
    private int securePort;

    @Value("${server.http.port}")
    private int httpPort;

    @Value("${server.http.interface}")
    private String httpInterface;

    @Bean
    public UndertowServletWebServerFactory servletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        // Add http listener
        factory.getBuilderCustomizers().add(builder -> {
            builder.addHttpListener(httpPort, httpInterface);
        });
        // Redirect rule to secure port
        factory.getDeploymentInfoCustomizers().add(deploymentInfo -> {
            deploymentInfo.addSecurityConstraint(
                new SecurityConstraint()
                    .addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*"))
                    .setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
                    .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
                .setConfidentialPortManager(
                    exchange -> securePort
                );
        });
        return factory;
    }
}
