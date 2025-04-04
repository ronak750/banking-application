package com.transactions.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@SpringBootApplication
@EnableFeignClients
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator bankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/users/**")
						.filters( f -> f.rewritePath("/users/(?<segment>.*)","/${segment}")
								.circuitBreaker(config -> config.setName("usersCircuitBreaker")
										.setFallbackUri("forward:/contactSupport"))
								.retry(retryConfig -> retryConfig.setRetries(3)
										.setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true))
						)
						.uri("lb://USERS"))
				.route(p -> p
						.path("/transactions/**")
						.filters( f -> f.rewritePath("/transactions/(?<segment>.*)","/${segment}")
								.circuitBreaker(config -> config.setName("transactionsCircuitBreaker")
										.setFallbackUri("forward:/contactSupport"))
								.retry(retryConfig -> retryConfig.setRetries(3).setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
										.setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true))
						)
						.uri("lb://TRANSACTIONS"))
				.build();
	}
}
