package my.demo.springboot.microservice.todo.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;


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