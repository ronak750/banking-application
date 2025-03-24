package com.transactions.api.gateway.config;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(LoadBalancedExchangeFilterFunction lbFunction) {
        return WebClient.builder().filter(lbFunction);
    }
}