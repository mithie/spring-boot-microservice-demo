package my.demo.springboot.microservice.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class AccountServiceApplication {

	@RequestMapping(value = "/accounts")
	public String accounts() {
		return "service has yet to be implemented";
	}

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}
}
