package my.demo.springboot.microservice.todo;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TodoServiceApplication {
    public static void main(final String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
