package com.chutneytesting;

import com.chutneytesting.security.ChutneyHttpSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@Profile("dev-auth")
@Order(1)
public class SecSecurityMemoryConfig extends ChutneyHttpSecurityConfig {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        configureBaseHttpSecurity(http);
        http
            .anonymous()
                .principal(anonymous())
            .and()
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            .httpBasic();
    }
}
