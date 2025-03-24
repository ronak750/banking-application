package com.transactions.api.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfigFluxWeb {

    @Bean
    public SecurityWebFilterChain buildSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for API Gateway
//                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers(HttpMethod.POST,"/auth/login", "/users/api/v1/").permitAll()
//                        .pathMatchers(HttpMethod.GET, "/contactSupport").permitAll()// Allow public routes
//                        .anyExchange().authenticated() // Require authentication for other routes
//                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt()) // Enable JWT authentication
                .build();
    }
}
