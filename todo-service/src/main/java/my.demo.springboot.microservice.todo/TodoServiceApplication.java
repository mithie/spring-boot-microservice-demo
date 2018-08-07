package my.demo.springboot.microservice.todo;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
public class TodoServiceApplication {

    @Bean
    TodoConfiguration todoConfiguration() {
        return new TodoConfiguration();
    }

    public static void main(final String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
