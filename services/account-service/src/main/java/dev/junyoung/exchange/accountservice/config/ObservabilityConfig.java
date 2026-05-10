package dev.junyoung.exchange.accountservice.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

@Configuration
public class ObservabilityConfig {

	@Bean
	ApplicationRunner openTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
		return _ -> OpenTelemetryAppender.install(openTelemetry);
	}
}
