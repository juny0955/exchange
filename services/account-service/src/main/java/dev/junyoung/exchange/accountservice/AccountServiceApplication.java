package dev.junyoung.exchange.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.junyoung.exchange")
public class AccountServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}

}
