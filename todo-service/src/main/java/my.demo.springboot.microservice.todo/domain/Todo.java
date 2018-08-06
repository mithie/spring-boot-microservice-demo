package my.demo.springboot.microservice.todo.domain;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import my.demo.springboot.microservice.todo.api.TodoController;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class Todo {
    private UUID todoId;

    @NonNull
    private UUID accountId;

    @NonNull
    private String email;

    @NonNull
    private String description;

    @NonNull
    boolean completed;
}
