package com.chutneytesting;

import com.chutneytesting.security.ChutneySecurityConfig;
import com.chutneytesting.security.api.UserDto;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@Profile("dev-auth")
@Order(1)
public class SecSecurityMemoryConfig extends ChutneySecurityConfig {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        configureBaseHttpSecurity(http);
        http
            .anonymous()
                .principal(UserDto.ANONYMOUS)
            .and()
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
            .httpBasic();
    }
}
