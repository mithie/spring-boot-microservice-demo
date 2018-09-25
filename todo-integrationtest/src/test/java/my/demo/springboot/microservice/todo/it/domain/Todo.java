package my.demo.springboot.microservice.todo.it.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.core.Relation;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Relation(collectionRelation="todos")
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
