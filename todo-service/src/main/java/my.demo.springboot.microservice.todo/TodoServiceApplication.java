package my.demo.springboot.microservice.todo;

import my.demo.springboot.microservice.todo.client.AccountConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableDiscoveryClient
@RibbonClient(name = "account", configuration = AccountConfiguration.class)
public class TodoServiceApplication {
    public static void main(final String[] args) {
        run(TodoServiceApplication.class, args);
    }
}
