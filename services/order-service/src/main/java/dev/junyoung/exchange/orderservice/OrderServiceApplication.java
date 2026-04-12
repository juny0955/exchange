package dev.junyoung.exchange.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@ImportGrpcClients(basePackages = "dev.junyoung.exchange.proto.account.v1")
@EnableResilientMethods
public class OrderServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
