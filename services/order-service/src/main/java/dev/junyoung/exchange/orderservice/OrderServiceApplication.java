package dev.junyoung.exchange.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication(scanBasePackages = "dev.junyoung.exchange")
@EnableResilientMethods
public class OrderServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
