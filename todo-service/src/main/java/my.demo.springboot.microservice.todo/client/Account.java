package my.demo.springboot.microservice.todo.client;

import java.util.UUID;

import org.springframework.hateoas.ResourceSupport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Account {
    private UUID accountId;
    private String firstName;
    private String lastName;
    private String email;
}