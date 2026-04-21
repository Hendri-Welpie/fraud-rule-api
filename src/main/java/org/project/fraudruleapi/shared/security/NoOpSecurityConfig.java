package org.project.fraudruleapi.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSecurityConfig {

    @Bean
    public SecurityWebFilterChain noOpSecurityWebFilterChain(ServerHttpSecurity http) {
        log.info("Security is DISABLED - all endpoints are accessible without authentication");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.mode(
                                org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                        .cache(org.springframework.security.config.Customizer.withDefaults())
                        .hsts(hsts -> hsts
                                .includeSubdomains(true)
                                .maxAge(java.time.Duration.ofDays(365)))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                )
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}

