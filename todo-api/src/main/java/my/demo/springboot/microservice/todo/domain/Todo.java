package my.demo.springboot.microservice.todo.domain;

import lombok.*;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "todoId")
@ToString(exclude = "todoId")
@RequiredArgsConstructor
@Relation(collectionRelation="todos")
public class Todo extends ResourceSupport {
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
