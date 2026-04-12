package dev.junyoung.exchange.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@ImportGrpcClients(basePackages = "dev.junyoung.exchange.proto.account.v1")
public class OrderServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
