package my.demo.springboot.microservice.account;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
@EnableHystrixDashboard
public class AccountServiceApplication {

	@Bean
	AccountConfiguration accountConfiguration() {
		return new AccountConfiguration();
	}

	public static void main(final String[] args) {
		run(AccountServiceApplication.class, args);
	}
}
