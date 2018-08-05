package my.demo.springboot.microservice.todo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableDiscoveryClient
public class TodoServiceApplication {

    public static void main(String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
