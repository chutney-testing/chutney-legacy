package com.chutneytesting;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.server.core.domain.security.Authorization;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfiguration {

    @Bean("unsecureFilterChain")
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        UserDto defaultUser = getDefaultUser();

        http
            .csrf(AbstractHttpConfigurer::disable)
            .anonymous(anonymousConfigurer -> anonymousConfigurer
                .principal(defaultUser)
                .authorities(new ArrayList<>(defaultUser.getAuthorities())))
            .authorizeHttpRequests(httpRequest -> httpRequest.anyRequest().permitAll());

        http
            .requiresChannel(channelRequestMatcherRegistry -> channelRequestMatcherRegistry.anyRequest().requiresInsecure());

        return http.build();
    }

    protected UserDto getDefaultUser() {
        UserDto defaultUser = new UserDto();
        defaultUser.setName("ChutneyPluginUser");
        defaultUser.addRole("ADMIN");
        Arrays.stream(Authorization.values()).map(Enum::name).forEach(defaultUser::grantAuthority);
        return defaultUser;
    }
}
