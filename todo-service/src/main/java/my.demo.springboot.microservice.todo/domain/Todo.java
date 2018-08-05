package my.demo.springboot.microservice.todo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Todo {
    private Long id;
    private Long accountId;
    private String email;
    private String description;
    boolean completed;
}
