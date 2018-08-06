package my.demo.springboot.microservice.account;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {
	public static void main(final String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
