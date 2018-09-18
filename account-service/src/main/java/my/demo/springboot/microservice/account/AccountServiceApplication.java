package my.demo.springboot.microservice.account;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class AccountServiceApplication {

	public static void main(final String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
