package dev.junyoung.exchange.orderservice.adapter.out.grpc.account.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import dev.junyoung.exchange.orderservice.adapter.out.grpc.account.exception.RetryableAccountGrpcException;

@Configuration
public class AccountGrpcConfig {

	@Bean
	RetryTemplate accountGrpcRetryTemplate() {
		RetryPolicy retryPolicy = RetryPolicy.builder()
			.includes(RetryableAccountGrpcException.class)
			.maxRetries(2)
			.delay(Duration.ofMillis(100))
			.multiplier(2)
			.jitter(Duration.ofMillis(50))
			.maxDelay(Duration.ofMillis(1000))
			.build();

		return new RetryTemplate(retryPolicy);
	}
}
