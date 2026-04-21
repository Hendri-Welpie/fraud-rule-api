package org.project.fraudruleapi.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Security is ENABLED with RBAC - configuring HTTP Basic authentication");
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.mode(
                                org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .contentTypeOptions(Customizer.withDefaults())
                        .cache(Customizer.withDefaults())
                        .hsts(hsts -> hsts
                                .includeSubdomains(true)
                                .maxAge(java.time.Duration.ofDays(365)))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                )
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(
                                "/actuator/health/**",
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        // Rules management: ADMIN only
                        .pathMatchers(HttpMethod.POST, "/api/v1/rules/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/rules/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/rules/**").hasRole("ADMIN")
                        // Rules read: ADMIN or ANALYST
                        .pathMatchers(HttpMethod.GET, "/api/v1/rules/**").hasAnyRole("ADMIN", "ANALYST")
                        // Fraud transaction submission: ADMIN or ANALYST
                        .pathMatchers(HttpMethod.POST, "/v1/api/fraud/transactions").hasAnyRole("ADMIN", "ANALYST")
                        // Fraud read: ADMIN or ANALYST
                        .pathMatchers(HttpMethod.GET, "/v1/api/fraud/**").hasAnyRole("ADMIN", "ANALYST")
                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        List<UserDetails> userDetailsList = securityProperties.getUsers().stream()
                .map(userConfig -> User.builder()
                        .username(userConfig.getUsername())
                        .password(passwordEncoder.encode(userConfig.getPassword()))
                        .roles(userConfig.getRole())
                        .build())
                .map(UserDetails.class::cast)
                .toList();

        if (userDetailsList.isEmpty()) {
            log.warn("No users configured - creating default admin user");
            userDetailsList = List.of(
                    User.builder()
                            .username("admin")
                            .password(passwordEncoder.encode("admin"))
                            .roles("ADMIN")
                            .build()
            );
        }

        log.info("Configured {} users with RBAC roles", userDetailsList.size());
        return new MapReactiveUserDetailsService(userDetailsList);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

