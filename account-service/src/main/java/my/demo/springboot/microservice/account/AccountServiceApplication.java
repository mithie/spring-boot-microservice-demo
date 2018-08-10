package my.demo.springboot.microservice.account;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class AccountServiceApplication {

	@Bean
	AccountConfiguration accountConfiguration() {
		return new AccountConfiguration();
	}

	public static void main(final String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
